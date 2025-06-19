package data.model

import data.util.CustomBusStatusSerializer
import kotlinx.serialization.Serializable

@Serializable(with = CustomBusStatusSerializer::class)
sealed interface BusStatus{
     object Active: BusStatus
     object InActive: BusStatus
     object NotInService: BusStatus
     object InMaintenance: BusStatus
}


