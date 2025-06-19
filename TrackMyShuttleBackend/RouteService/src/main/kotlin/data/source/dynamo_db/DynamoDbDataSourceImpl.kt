package org.example.data.source.dynamo_db

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.*
import aws.sdk.kotlin.services.dynamodb.waiters.waitUntilTableExists
import aws.smithy.kotlin.runtime.ExperimentalApi
import data.db_converters.DbItemConverter
import data.entity.DynamoDbModel
import data.entity.RouteEntity
import data.exceptions.*
import data.model.DynamoDbTransactWriteItem
import data.util.*
import kotlinx.coroutines.delay
import kotlin.Throws
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


    override suspend fun getItem(key: String): DynamoDbResult<T> {
        val itemKey = convertToItemKey(key)
        val getRequest = GetItemRequest.Companion {
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
        return BatchGetItemRequest.Companion {
            requestItems = mapOf<String, KeysAndAttributes>(
                currentTableName to KeysAndAttributes.Companion {
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
                    TransactWriteItem.Companion {
                        put = item.putItem?.let { item ->

                            Put.Companion {
                                tableName = currentTableName
                                this.item = itemConverter.serializeToAttrValue(item)
                                conditionExpression = "attribute_not_exists($primaryKey)"
                            }
                        }

                        delete = item.deleteItemKey?.let { deleteKey ->
                            Delete.Companion {
                                tableName = currentTableName
                                key = convertToItemKey(deleteKey)
                                conditionExpression = "attribute_exists($primaryKey)"
                            }
                        }

                        update = item.updateItem?.let { updateItem ->

                            val attrName = convertToAttrValUpdate(updateItem.attrToUpdate).keys.first()
                            val attrVal = convertToAttrValUpdate(updateItem.attrToUpdate).values.first().value
                            val updateAction = convertToAttrValUpdate(updateItem.attrToUpdate).values.first().action

                            if (attrVal != null && updateAction != null) {
                                Update.Companion {
                                    key = mapOf(primaryKey to AttributeValue.S(updateItem.key))
                                    tableName = currentTableName
                                    conditionExpression = "attribute_exists(${primaryKey})"
                                    expressionAttributeNames = mapOf("#attribute" to attrName)
                                    expressionAttributeValues = mapOf(":value" to attrVal)
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
    ): TransactWriteItemsRequest {
        return TransactWriteItemsRequest.Companion { transactItems = items }
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
        val putRequest = PutItemRequest.Companion {
            tableName = currentTableName
            this.item = itemValues
            conditionExpression = if (isUpsert) "attribute_exists($primaryKey)" else "attribute_not_exists($primaryKey)"
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
        val deleteRequest = DeleteItemRequest.Companion {
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
                RouteEntity::class -> convertToAttrValUpdate(update)
                else -> throw UnsupportedEntityClass()
            }

            val updateRequest = UpdateItemRequest.Companion {
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
            attrName to AttributeValueUpdate.Companion {
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
            is RouteEntityAttrUpdate ->{
                throw UnsupportedUpdateType()
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
        sortKey: String,
    ) {
        val attDef = listOf(
            AttributeDefinition.Companion {
                attributeName = partitionKey
                attributeType = ScalarAttributeType.S
            },
            AttributeDefinition.Companion {
                attributeName = sortKey
                attributeType = ScalarAttributeType.S
            }
        )


        val keySchemaVal = KeySchemaElement.Companion {
            attributeName = partitionKey
            keyType = KeyType.Hash
        }

        val sortKeySchemaVal = KeySchemaElement.Companion {
            attributeName = sortKey
            keyType = KeyType.Range
        }

        val provisionedVal = ProvisionedThroughput.Companion {
            readCapacityUnits = 10
            writeCapacityUnits = 10
        }

        val createTableRequest = CreateTableRequest.Companion {
            attributeDefinitions = attDef
            keySchema = listOf(keySchemaVal, sortKeySchemaVal)
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