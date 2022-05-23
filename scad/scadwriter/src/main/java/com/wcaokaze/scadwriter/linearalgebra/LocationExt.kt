package com.wcaokaze.scadwriter.linearalgebra

import com.wcaokaze.scadwriter.Location

val Location.bottomVectorLine get() = Line3d(point, bottomVector)
val Location.topVectorLine    get() = Line3d(point, topVector)
val Location.frontVectorLine  get() = Line3d(point, frontVector)
val Location.backVectorLine   get() = Line3d(point, backVector)
val Location.rightVectorLine  get() = Line3d(point, rightVector)
val Location.leftVectorLine   get() = Line3d(point, leftVector)
