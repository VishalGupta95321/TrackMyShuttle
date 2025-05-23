package data.exceptions

sealed interface BusStopRepoErrors {
    data object BusStopDoesNotExist: BusStopRepoErrors
    data object BusStopAlreadyExists: BusStopRepoErrors
    data object SomethingWentWrong: BusStopRepoErrors
}