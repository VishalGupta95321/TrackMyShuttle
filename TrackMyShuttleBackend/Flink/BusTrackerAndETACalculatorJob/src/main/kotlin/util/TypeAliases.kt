package util

import models.BusData
import models.BusLocationData
import models.Route

typealias Index = Int
typealias TimeStamp = Long
typealias isReturning = Boolean
typealias BusUnion = EitherOfThree<BusLocationData, BusData, Route>
