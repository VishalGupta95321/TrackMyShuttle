package exceptions


sealed class RouteControllerExceptions(
    val message: String? = null
){
    data object SomethingWentWrong : RouteControllerExceptions("Something went wrong")
    data object InvalidInput : RouteControllerExceptions("Invalid input")
    data object ItemNotFound : RouteControllerExceptions("Bus stop Not found")
    data object ItemAlreadyExists : RouteControllerExceptions(" Bus stop already exists")
}
