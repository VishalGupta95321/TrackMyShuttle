package exceptions

import data.exceptions.BusRepoErrors

sealed class BusControllerExceptions(
    val message: String? = null
){
    data object SomethingWentWrong : BusControllerExceptions("Something went wrong")
    data object InvalidInput : BusControllerExceptions("Invalid input")
    data object ItemNotFound : BusControllerExceptions("Bus Not found")
    data object ItemAlreadyExists : BusControllerExceptions(" Bua already exists")
    data object RegistrationError  : BusControllerExceptions("Registration failed, our bad! Try again later.")
}
