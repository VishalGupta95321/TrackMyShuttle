package data.model

data class Bus(
    val busId:String,
    val driverName: String,
    val activeHours: String,
    val activeDays: String,
    val busStatus: BusStatus?,
    val currentStop: String?,  // these two fields will be added by server or wherever
    val nextStop: String?,   //
)