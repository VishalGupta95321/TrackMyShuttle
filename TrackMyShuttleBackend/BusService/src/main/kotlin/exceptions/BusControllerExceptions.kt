package exceptions

sealed class BusControllerExceptions(
    val message: String? = null
){
    data object SomethingWentWrong : BusControllerExceptions("Something went wrong")
}
