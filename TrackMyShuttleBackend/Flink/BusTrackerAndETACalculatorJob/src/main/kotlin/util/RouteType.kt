package util


sealed interface RouteType{
    data object OutAndBack : RouteType
    data object Loop : RouteType
}