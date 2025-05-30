package data.model

import kotlinx.serialization.Serializable

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
                0 -> Active
                1 -> InActive
                2 -> NotInService
                3 -> InMaintenance
                else -> InActive
            }
        }

    }
}


