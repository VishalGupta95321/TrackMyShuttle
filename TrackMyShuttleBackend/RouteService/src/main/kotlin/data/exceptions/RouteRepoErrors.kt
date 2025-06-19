package data.exceptions

sealed interface RouteRepoErrors {
    data object RouteDoesNotExist: RouteRepoErrors
    data object RouteAlreadyExists: RouteRepoErrors
    data object SomethingWentWrong: RouteRepoErrors
}