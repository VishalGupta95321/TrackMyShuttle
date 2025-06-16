package util

import models.BusData
import models.RawBusStop
import models.Route
import kotlin.time.Duration

typealias Index = Int
typealias TimeStamp = Long
typealias isReturning = Boolean
typealias WaitTime = Duration
typealias StopId = String
typealias CombinedStream = EitherOfThree<RawBusStop, BusData, Route>
