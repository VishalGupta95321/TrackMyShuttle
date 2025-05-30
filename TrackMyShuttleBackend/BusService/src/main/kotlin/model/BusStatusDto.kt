package model

import data.model.BusStatus
import kotlinx.serialization.Serializable
import util.CustomBusStatusDtoSerializer

@Serializable(with = CustomBusStatusDtoSerializer::class)
sealed interface BusStatusDto{
    data object Active: BusStatusDto
    data object InActive: BusStatusDto
    data object NotInService: BusStatusDto
    data object InMaintenance: BusStatusDto


    fun toBusStatus() = when(this){
        Active -> BusStatus.Active
        InActive -> BusStatus.InActive
        InMaintenance -> BusStatus.InMaintenance
        NotInService -> BusStatus.NotInService
    }

    companion object{
        fun fromBusStatus(busStatus: BusStatus) = when(busStatus){
            BusStatus.Active -> Active
            BusStatus.InMaintenance -> InMaintenance
            BusStatus.NotInService -> NotInService
            BusStatus.InActive -> InActive
        }
    }
}


