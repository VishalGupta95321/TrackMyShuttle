package exceptions


sealed class BusStopControllerExceptions(
    val message: String? = null
){
    data object SomethingWentWrong : BusStopControllerExceptions("Something went wrong")
    data object InvalidInput : BusStopControllerExceptions("Invalid input")
    data object ItemNotFound : BusStopControllerExceptions("Bus stop Not found")
    data object ItemAlreadyExists : BusStopControllerExceptions(" Bus stop already exists")
}
