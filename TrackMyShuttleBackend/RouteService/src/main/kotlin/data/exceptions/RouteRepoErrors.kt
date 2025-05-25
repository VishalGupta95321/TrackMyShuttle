package data.exceptions

sealed interface RouteRepoErrors {
    data object BusStopDoesNotExist: RouteRepoErrors
    data object BusStopAlreadyExists: RouteRepoErrors
    data object SomethingWentWrong: RouteRepoErrors
}