package models


data class EnrichedLocationData(
    val locationData: BusLocationData,
    val busData: BusData,
    val busStopsData: List<BusStop>,
    val routesData: List<Route>
)