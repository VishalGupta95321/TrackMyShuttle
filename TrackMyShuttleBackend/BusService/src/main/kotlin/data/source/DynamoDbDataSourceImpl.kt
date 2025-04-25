package data.source

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.*
import aws.sdk.kotlin.services.dynamodb.waiters.waitUntilTableExists
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.db_converters.BusStatusValueConverter
import data.db_converters.DbItemConverter
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
import kotlin.reflect.KClass

@OptIn(ExperimentalApi::class)
class DynamoDbDataSourceImpl<T : DynamoDbEntity>(
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

        while (unprocessedKeysBatch != null) {
            val response = databaseClient.batchGetItem(batchRequest)

            response.responses?.get(currentTableName)?.let { items ->
                processedItems.addAll( items.map { item ->
                    itemConverter.deserializeToObject(item) //as T
                })
            }

            unprocessedKeysBatch =
                response.unprocessedKeys?.get(currentTableName)?.keys?.let { keys ->
                    createBatchGetItemRequest(keys)
                }

            delay(BATCH_REQUEST_RETRY_INTERVAL)
        }

        return processedItems
    }


    override suspend fun transactWriteItems(
        items: List<DynamoDbTransactWriteItem<T>>
    ): BasicDynamoDbResult {

        if (items.size >= MAX_TRANS_WRITE_ITEMS_LIMIT) return GetBack.Error(DynamoDbErrors.MaxTransWriteItemsExceeded)

        val transactionWriteItems = items.map { item ->
            TransactWriteItem {
                put = item.putItem?.let {
                    Put {
                        tableName = currentTableName
                        this.item = itemConverter.serializeToAttrValue(it)
                    }
                }
                delete = item.deleteItemKey?.let {
                    Delete {
                        tableName = currentTableName
                        key = convertToItemKey(item.deleteItemKey)
                    }
                }
            }
        }

        val transactionWriteRequest = TransactWriteItemsRequest {
            transactItems = transactionWriteItems
        }

        try {
            databaseClient.transactWriteItems(transactionWriteRequest)
            return GetBack.Success()
        } catch (e: DynamoDbException){
            e.printStackTrace()
            return GetBack.Error(DynamoDbErrors.UndefinedError)
        } catch (e: Exception) {
            e.printStackTrace()
            return GetBack.Error()
        }
    }


    override suspend fun putItem(
        item: T,
        isUpsert: Boolean,
    ): BasicDynamoDbResult {
        val itemValues = itemConverter.serializeToAttrValue(item)
        val putRequest = PutItemRequest {
            tableName = currentTableName
            this.item = itemValues
            conditionExpression = if(!isUpsert) "attribute_not_exists($primaryKey)" else null
        }
        try {
            databaseClient.putItem(putRequest)
            return GetBack.Success()
        } catch (e: ConditionalCheckFailedException){
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

        try {
            val attrUpdates = when (clazz) {
                BusEntity::class -> convertBusDataToAttrValUpdate(update)
                else -> throw UnsupportedEntityClass()
            }

            val updateRequest = UpdateItemRequest {
                tableName = currentTableName
                key = itemKey
                attributeUpdates = attrUpdates
                conditionExpression = "attribute_exists($primaryKey)"
            }

            databaseClient.updateItem(updateRequest)
            return GetBack.Success()

        }catch (e: ConditionalCheckFailedException){
            return GetBack.Error(DynamoDbErrors.ItemDoesNotExists)
        }catch (e: DynamoDbException){
            e.printStackTrace()
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
    private fun convertBusDataToAttrValUpdate(
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
                        if(update.value!=null) AttributeValue.S("") else AttributeValue.Null(true),
                        update.action
                    )

                    is UpdateNextStop -> convertToAttrValUpdate(
                        BusEntityAttributes.NEXT_STOP,
                        if(update.value!=null) AttributeValue.S("") else AttributeValue.Null(true),
                        update.action
                    )

                    is UpdateStopIds -> convertToAttrValUpdate(
                        BusEntityAttributes.STOP_IDS,
                        AttributeValue.Ss(update.value),
                        update.action
                    )
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
        return introspector.getAttrNameByAnnotation<TableName>()
            ?: throw NoTableNameFound()
    }


    companion object {
        const val BATCH_REQUEST_RETRY_INTERVAL = 300L
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

