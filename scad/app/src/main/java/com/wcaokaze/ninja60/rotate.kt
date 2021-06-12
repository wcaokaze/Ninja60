package com.wcaokaze.ninja60

import com.wcaokaze.linearalgebra.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 指定された座標を通りX軸と平行な直線を軸として回転する。
 * @param axis 軸の座標。[Point3d.x]は無視される
 */
fun Point3d.rotateX(axis: Point3d, angle: Angle): Point3d
      = rotate(Line3d(axis, Vector3d.X_UNIT_VECTOR), angle)

/**
 * 指定された座標を通りY軸と平行な直線を軸として回転する。
 * @param axis 軸の座標。[Point3d.y]は無視される
 */
fun Point3d.rotateY(axis: Point3d, angle: Angle): Point3d
      = rotate(Line3d(axis, Vector3d.Y_UNIT_VECTOR), angle)

/**
 * 指定された座標を通りZ軸と平行な直線を軸として回転する。
 * @param axis 軸の座標。[Point3d.z]は無視される
 */
fun Point3d.rotateZ(axis: Point3d, angle: Angle): Point3d
      = rotate(Line3d(axis, Vector3d.Z_UNIT_VECTOR), angle)
