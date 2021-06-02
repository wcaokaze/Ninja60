package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.foundation.*

/**
 * 指定された座標を通りX軸と平行な直線を軸として回転する。
 * @param axis 軸の座標。[Point3d.x]は無視される
 */
fun Point3d.rotateX(axis: Point3d, angle: Angle) = Point3d(
   x,
   axis.y + (y - axis.y) * cos(angle) - (z - axis.z) * sin(angle),
   axis.z + (y - axis.y) * sin(angle) + (z - axis.z) * cos(angle)
)

/**
 * 指定された座標を通りY軸と平行な直線を軸として回転する。
 * @param axis 軸の座標。[Point3d.y]は無視される
 */
fun Point3d.rotateY(axis: Point3d, angle: Angle) = Point3d(
   axis.x + (x - axis.x) * cos(-angle) - (z - axis.z) * sin(-angle),
   y,
   axis.z + (x - axis.x) * sin(-angle) + (z - axis.z) * cos(-angle)
)

/**
 * 指定された座標を通りZ軸と平行な直線を軸として回転する。
 * @param axis 軸の座標。[Point3d.z]は無視される
 */
fun Point3d.rotateZ(axis: Point3d, angle: Angle) = Point3d(
   axis.x + (x - axis.x) * cos(angle) - (y - axis.y) * sin(angle),
   axis.y + (x - axis.x) * sin(angle) + (y - axis.y) * cos(angle),
   z
)
