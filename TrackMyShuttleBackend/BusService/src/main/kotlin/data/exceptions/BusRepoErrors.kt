package data.exceptions

sealed interface BusRepoErrors {
    data object BusDoesNotExist: BusRepoErrors
    data object BusAlreadyExists: BusRepoErrors
    data object SomethingWentWrong: BusRepoErrors
    data object PartitionKeyLimitExceeded: BusRepoErrors
}