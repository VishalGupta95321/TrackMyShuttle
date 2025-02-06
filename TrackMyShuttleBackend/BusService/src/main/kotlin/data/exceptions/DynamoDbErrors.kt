package data.exceptions

sealed interface DynamoDbErrors {
    data object TableDoesNotExists : DynamoDbErrors
    data object UndefinedError : DynamoDbErrors
    data object MaxTransWriteItemsExceeded: DynamoDbErrors
    data object UnsupportedUpdateType: DynamoDbErrors
    data object UnsupportedEntityClass: DynamoDbErrors
    data object UnsupportedAttribute: DynamoDbErrors
    data object ItemDoesNotExist: DynamoDbErrors
}
