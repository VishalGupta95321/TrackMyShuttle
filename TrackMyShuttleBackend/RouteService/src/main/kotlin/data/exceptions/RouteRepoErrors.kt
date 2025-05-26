package data.exceptions

sealed interface RouteRepoErrors {
    data object ItemNotExist: RouteRepoErrors
    data object ItemAlreadyExists: RouteRepoErrors
    data object SomethingWentWrong: RouteRepoErrors
}