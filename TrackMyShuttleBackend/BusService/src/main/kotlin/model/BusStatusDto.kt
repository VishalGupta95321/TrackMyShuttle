package model

sealed interface BusStatusDto{
    data object Active: BusStatusDto
    data object InActive: BusStatusDto
    data object NotInService: BusStatusDto
    data object InMaintenance: BusStatusDto
}

