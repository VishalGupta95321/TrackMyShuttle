package data.source

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.*
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue.*
import aws.sdk.kotlin.services.dynamodb.waiters.waitUntilTableExists
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.db_converters.BusStatusValueConverter
import data.db_converters.DbItemConverter
import data.db_converters.StopIdsWithWaitTimeValueConverter
import data.entity.*
import data.exceptions.DynamoDbErrors
import data.exceptions.NoPartitionKeyFound
import data.exceptions.NoTableNameFound
import data.exceptions.UnsupportedEntityClass
import data.exceptions.UnsupportedUpdateType
import data.model.DynamoDbTransactWriteItem
import data.util.*
import data.util.BusEntityAttrUpdate.UpdateBusStatus
import data.util.BusEntityAttrUpdate.UpdateCurrentStop
import data.util.BusEntityAttrUpdate.UpdateNextStop
import data.util.BusEntityAttrUpdate.UpdateStopIds
import kotlinx.coroutines.delay
import kotlin.collections.chunked
import kotlin.reflect.KClass

@OptIn(ExperimentalApi::class)
class DynamoDbDataSourceImpl<T : DynamoDbModel>(
    private val clazz: KClass<T>,
    private val databaseClient: DynamoDbClient,
    private val introspector: ClassIntrospector<T>,
    private val itemConverter: DbItemConverter<T>,
) : DynamoDbDataSource<T> {

    private var currentTableName: String
    private var primaryKey: String
    //private val itemConverter = itemConverter as DbItemConverter<T>

    // if the item is not available then its just returning null data TODO
    // if item is null then error
    override suspend fun getItem(key: String): DynamoDbResult<T> {
        val itemKey = convertToItemKey(key)
        val getRequest = GetItemRequest {
            this.key = itemKey
            tableName = currentTableName
        }

        try {
            val response = databaseClient.getItem(getRequest)
            response.item?.let { item ->
                return GetBack.Success(itemConverter.deserializeToObject(item))
            }
            return GetBack.Error(DynamoDbErrors.ItemDoesNotExists)
        }catch (e: DynamoDbException){
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.UndefinedError)
        }catch (e: Exception) {
            e.printStackTrace()
            return GetBack.Error()
        }
    }

    // I tried With one unknown key thought it will create infinite loop but did not
    override suspend fun getItemsInBatch(keys: List<String>): DynamoDbResult<List<T>> {

        val itemKeys = keys.map { convertToItemKey(it) }
        val itemKeyChunks = itemKeys.chunked(MAX_GET_ITEM_BATCH_LIMIT)
        val processedItems: MutableList<T> = mutableListOf()

        try {
            itemKeyChunks.forEach { chunks ->
                println("chunks: $chunks")
                processedItems.addAll(processGetItemBatchRequest(createBatchGetItemRequest(chunks)))
            }
        }catch (e: DynamoDbException){
            println("From db exec")
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.UndefinedError)
        }
        catch (e: Exception) {
            println("From exec")
            e.printStackTrace()
            return GetBack.Error()
        }
        return GetBack.Success(processedItems)
    }

    private fun createBatchGetItemRequest( /// TODO this is throwing the error TODO *Fixed
        itemKeys: List<Map<String, AttributeValue>>,
    ): BatchGetItemRequest {
        return BatchGetItemRequest {
            requestItems = mapOf<String, KeysAndAttributes>(
                currentTableName to KeysAndAttributes {
                    keys = itemKeys
                })
        }
    }


    private suspend fun processGetItemBatchRequest(
        batchRequest: BatchGetItemRequest,
    ): List<T> {

        var unprocessedKeysBatch: BatchGetItemRequest? = batchRequest
        val processedItems: MutableList<T> = mutableListOf()
        var maxRetryAttempts = MAX_RETRY_ATTEMPTS
        var retryInterval = BATCH_REQUEST_RETRY_INTERVAL

        while (unprocessedKeysBatch != null && maxRetryAttempts > 0)  {
            val response = databaseClient.batchGetItem(batchRequest)

            response.responses?.get(currentTableName)?.let { items ->
                processedItems.addAll(items.map { item ->
                    itemConverter.deserializeToObject(item) //as T
                })
            }

            unprocessedKeysBatch =
                response.unprocessedKeys?.get(currentTableName)?.keys?.let { keys ->
                    createBatchGetItemRequest(keys)
                }

            delay(retryInterval)
            retryInterval += retryInterval
            maxRetryAttempts--
        }

        return processedItems
    }


    override suspend fun transactWriteItems(
        items: List<DynamoDbTransactWriteItem<T>>
    ): BasicDynamoDbResult {

        val itemChunks = items.chunked(MAX_TRANS_WRITE_ITEMS_LIMIT)

        try {
            itemChunks.forEach { chunk ->
                val transactionWriteItems = chunk.map { item ->
                    TransactWriteItem {
                        put = item.putItem?.let { item ->

                            Put {
                                tableName = currentTableName
                                this.item = itemConverter.serializeToAttrValue(item)
                                conditionExpression = "attribute_not_exists($primaryKey)"
                            }
                        }

                        delete = item.deleteItemKey?.let { deleteKey ->
                            Delete {
                                tableName = currentTableName
                                key = convertToItemKey(deleteKey)
                                conditionExpression = "attribute_exists($primaryKey)"
                            }
                        }

                        // one attr at time
                        update = item.updateItem?.let { updateItem ->

                            val attrName = convertToAttrValUpdate(updateItem.attrToUpdate).keys.first()
                            val attrVal = convertToAttrValUpdate(updateItem.attrToUpdate).values.first().value
                            val updateAction = convertToAttrValUpdate(updateItem.attrToUpdate).values.first().action

                            if (attrVal != null && updateAction != null) {
                                Update {
                                    key =  mapOf(primaryKey to AttributeValue.S(updateItem.key))
                                    tableName = currentTableName
                                    conditionExpression = "attribute_exists(${primaryKey})"
                                    expressionAttributeNames = mapOf("#attribute" to attrName)
                                    expressionAttributeValues =  mapOf(":value" to attrVal)
                                    updateExpression = when (updateAction) {
                                        AttributeAction.Add -> "ADD #attribute :value"
                                        AttributeAction.Delete -> "DELETE #attribute :value"
                                        AttributeAction.Put -> "set #attribute = :value"
                                        else -> throw Exception()
                                    }
                                }
                            } else null
                        }
                    }
                }
                processTransactWriteItemsRequest(createTransactWriteItemsRequest(transactionWriteItems))
            }
            return GetBack.Success()
        } catch (e: ConditionalCheckFailedException) {
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.ConditionCheckFailed)
        }catch (e: UnsupportedUpdateType) {
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.UnsupportedUpdateType)
        } catch (e: DynamoDbException) {
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.UndefinedError)
        } catch (e: Exception) {
            e.printStackTrace()
            return GetBack.Error()
        }
    }

    private fun createTransactWriteItemsRequest(
        items: List<TransactWriteItem>
    ):TransactWriteItemsRequest{
        return TransactWriteItemsRequest { transactItems = items}
    }

    private suspend fun processTransactWriteItemsRequest(
        transactRequest: TransactWriteItemsRequest,
    ){
        databaseClient.transactWriteItems(transactRequest)
    }


    override suspend fun putItem(
        item: T,
        isUpsert: Boolean,
    ): BasicDynamoDbResult {
        val itemValues = itemConverter.serializeToAttrValue(item)
        val putRequest = PutItemRequest {
            tableName = currentTableName
            this.item = itemValues
            conditionExpression = if(isUpsert) "attribute_exists($primaryKey)" else "attribute_not_exists($primaryKey)"
        }
        try {
            databaseClient.putItem(putRequest)
            return GetBack.Success()
        } catch (e: ConditionalCheckFailedException){
            e.printStackTrace()
            if(isUpsert) return GetBack.Error(DynamoDbErrors.ItemDoesNotExists)
            return GetBack.Error(DynamoDbErrors.ItemAlreadyExists)
        } catch (e: DynamoDbException){
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.UndefinedError)
        }catch (e: Exception) {
            e.printStackTrace()
            return GetBack.Error()
        }
    }

    override suspend fun deleteItem(key: String): BasicDynamoDbResult {
        val itemKey = convertToItemKey(key)
        val deleteRequest = DeleteItemRequest {
            tableName = currentTableName
            this.key = itemKey
            conditionExpression = "attribute_exists($primaryKey)"
        }
        try {
            databaseClient.deleteItem(deleteRequest)
            return GetBack.Success()
        }catch (e: ConditionalCheckFailedException){
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.ItemDoesNotExists)
        }catch (e: DynamoDbException){
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.UndefinedError)
        }catch (e: Exception) {
            e.printStackTrace()
            return GetBack.Error()
        }
    }


    @OptIn(ExperimentalApi::class)
    override suspend fun updateItemAttr(
        update: DynamoDbAttrUpdate,
        keyVal: String
    ): BasicDynamoDbResult {

        val itemKey = convertToItemKey(keyVal)

        val isBusExists = getItem(keyVal).let { result ->
            result is GetBack.Success
        }

        if (!isBusExists) return GetBack.Error(DynamoDbErrors.ItemDoesNotExists)

        try {
            val attrUpdates = when (clazz) {
                BusEntity::class -> convertToAttrValUpdate(update)
                else -> throw UnsupportedEntityClass()
            }

            val updateRequest = UpdateItemRequest {
                tableName = currentTableName
                key = itemKey
                //conditionExpression = "attribute_exists($primaryKey)" TODO("")
                attributeUpdates = attrUpdates
            }

            databaseClient.updateItem(updateRequest)
            return GetBack.Success()

        }catch (e: DynamoDbException){
            e.printStackTrace()
            if(e.localizedMessage == "Type mismatch for attribute to update")
                return GetBack.Error(DynamoDbErrors.TypeMismatchForAttribute)

            return GetBack.Error(DynamoDbErrors.UndefinedError)
        }catch (e: UnsupportedEntityClass){
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.UnsupportedEntityClass)
        }catch (e: UnsupportedUpdateType){
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.UnsupportedUpdateType)
        }catch (e: Exception) {
            return GetBack.Error(DynamoDbErrors.UnsupportedUpdateType)
        }
    }

    private fun <T : AttributeValue> convertToAttrValUpdate(
        attrName: String,
        attrValue: T,
        updateAction: AttributeAction
    ): Map<String, AttributeValueUpdate> {

        return mapOf(
            attrName to AttributeValueUpdate {
                value = attrValue
                action = updateAction
            }
        )
    }

    @Throws(UnsupportedUpdateType::class)
    private fun convertToAttrValUpdate(
        update: DynamoDbAttrUpdate,
    ): Map<String, AttributeValueUpdate> {
        return when (update) {
            is BusEntityAttrUpdate ->{
                when(update){
                    is UpdateBusStatus -> convertToAttrValUpdate(
                        BusEntityAttributes.BUS_STATUS,
                        BusStatusValueConverter.convertTo(update.value),
                        update.action
                    )

                    is UpdateCurrentStop -> convertToAttrValUpdate(
                        BusEntityAttributes.CURRENT_STOP,
                        if(update.value!=null) S(update.value) else Null(true),
                        update.action
                    )

                    is UpdateNextStop -> convertToAttrValUpdate(
                        BusEntityAttributes.NEXT_STOP,
                        if(update.value!=null) S(update.value) else Null(true),
                        update.action
                    )

                    is UpdateStopIds -> convertToAttrValUpdate(
                        BusEntityAttributes.STOP_IDS,
                        StopIdsWithWaitTimeValueConverter.convertTo(update.value),
                        update.action
                    )

                    is BusEntityAttrUpdate.UpdateBasicBusDetails -> {
                        val attrUpdates = mutableMapOf<String, AttributeValueUpdate>()
                        convertToAttrValUpdate(
                            BusEntityAttributes.ACTIVE_DAYS,
                            S(update.value.activeDays),
                            update.action
                        ).let { attrUpdates.putAll(it) } //// FIXME Maybe if required
                        convertToAttrValUpdate(
                            BusEntityAttributes.DRIVER_NAME,
                            S(update.value.driverName),
                            update.action
                        ).let { attrUpdates.putAll(it) } //// FIXME
                        convertToAttrValUpdate(
                            BusEntityAttributes.ACTIVE_HOURS,
                            S(update.value.activeHours),
                            update.action
                        ).let { attrUpdates.putAll(it) } //// FIXME

                        attrUpdates
                    }
                }
            }
            else -> throw UnsupportedUpdateType()
        }
    }



    private fun convertToItemKey(
        keyVal: String,
    ): Map<String, AttributeValue> {
        return mapOf(primaryKey to AttributeValue.S(keyVal))
    }


    private suspend fun createTable(
        name: String,
        partitionKey: String,
    ) {
        val attDef = AttributeDefinition {
            attributeName = partitionKey
            attributeType = ScalarAttributeType.S
        }

        val keySchemaVal = KeySchemaElement {
            attributeName = partitionKey
            keyType = KeyType.Hash
        }

        val provisionedVal = ProvisionedThroughput {
            readCapacityUnits = 10
            writeCapacityUnits = 10
        }  /// Fixme: May cause some problems later

        val createTableRequest = CreateTableRequest {
            attributeDefinitions = listOf(attDef)
            keySchema = listOf(keySchemaVal)
            provisionedThroughput = provisionedVal
            tableName = name
        }

        try {
            databaseClient.let { client ->
                client.createTable(createTableRequest)
                client.waitUntilTableExists {
                    tableName = name
                }
            }
        } catch (e: ResourceInUseException) {
            e.printStackTrace() // Table already exist.
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun checkIfTableExists(tableName: String): Boolean {
        try {
            val response = databaseClient.listTables()
            response.tableNames?.forEach {
                return it == tableName
            }
            return false
        }catch (e: Exception) {
            return false
        }
    }

    @OptIn(ExperimentalApi::class)
    private fun validateAttributeType(
        attrName: String,
        attrValue: Any,
    ): Boolean {
        val attrType = introspector.inspectAttrAndReturnType(attrName)
        return attrType != null && attrType::class == attrValue::class
    }

    private fun getTablePartitionKey(): String {
        return introspector.getAttrNameByAnnotation<DynamoDbPartitionKey>()
            ?: throw NoPartitionKeyFound()
    }

    private fun getTableName(): String {
        return introspector.getValueByAnnotation<TableName>()
            ?: throw NoTableNameFound()
    }


    companion object {
        const val BATCH_REQUEST_RETRY_INTERVAL = 300L
        const val MAX_RETRY_ATTEMPTS = 3
        const val MAX_GET_ITEM_BATCH_LIMIT = 80
        const val MAX_TRANS_WRITE_ITEMS_LIMIT = 80
    }

    init {
        currentTableName = getTableName()
        primaryKey = getTablePartitionKey()
    }
}


/// Use annotations to prepare schema for dynamo db
// TODO: Use indices

