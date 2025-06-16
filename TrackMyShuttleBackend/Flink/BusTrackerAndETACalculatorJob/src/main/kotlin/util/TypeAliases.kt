package util

import models.BusData
import models.BusStop
import models.Route

typealias Index = Int
typealias TimeStamp = Long
typealias isReturning = Boolean
typealias CombinedStream = EitherOfThree<BusStop, BusData, Route>
