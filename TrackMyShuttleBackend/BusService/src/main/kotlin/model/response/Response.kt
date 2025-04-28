package model.response

interface Response {
    val status: Status
    val message: String?
}

sealed interface Status{
    data object Success : Status
    data object Failure : Status
    data object SomethingWentWrong : Status
}