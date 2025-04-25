package data.model

sealed class BusStatus(
    val value: Int
){
     object Active: BusStatus(0)
     object InActive: BusStatus(1)
     object NotInService: BusStatus(2)
     object InMaintenance: BusStatus(3)

    companion object {
        fun fromValue(value: Int): BusStatus {
            return when (value) {
                0 -> BusStatus.Active
                1 -> BusStatus.InActive
                2 -> BusStatus.NotInService
                3 -> BusStatus.InMaintenance
                else -> BusStatus.InActive
            }
        }

    }
}


