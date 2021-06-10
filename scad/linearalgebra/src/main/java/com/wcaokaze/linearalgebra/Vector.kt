package com.wcaokaze.linearalgebra

import com.wcaokaze.scadwriter.foundation.*
import kotlin.math.*

/**
 * 二次元平面でのベクトル。
 *
 * 概念的には[Size2d]にかなり近いが、こちらは線形代数学に従ったベクトルの演算が使用可能。
 */
data class Vector2d(val x: Size, val y: Size) {
   companion object {
      /** X軸と平行で長さが1mmのベクトル */
      val X_UNIT_VECTOR = Vector2d(1.mm, 0.mm)
      /** Y軸と平行で長さが1mmのベクトル */
      val Y_UNIT_VECTOR = Vector2d(0.mm, 1.mm)
   }

   constructor(size2d: Size2d) : this(size2d.x, size2d.y)
   constructor(from: Point2d, to: Point2d) : this(to - from)

   override fun toString() = "Vector(${x.numberAsMilliMeter}mm, ${y.numberAsMilliMeter}mm)"

   operator fun plus (vector: Vector2d) = Vector2d(x + vector.x, y + vector.y)
   operator fun minus(vector: Vector2d) = Vector2d(x - vector.x, y - vector.y)

   operator fun times(n: Int)    = Vector2d(x * n, y * n)
   operator fun times(n: Double) = Vector2d(x * n, y * n)
   operator fun div  (n: Int)    = Vector2d(x / n, y / n)
   operator fun div  (n: Double) = Vector2d(x / n, y / n)

   operator fun unaryMinus() = Vector2d(-x, -y)
   operator fun unaryPlus () = Vector2d(+x, +y)

   /** このベクトルの長さ */
   val norm get() = Size(
      sqrt(x.numberAsMilliMeter * x.numberAsMilliMeter
         + y.numberAsMilliMeter * y.numberAsMilliMeter)
   )
}

/**
 * 三次元空間でのベクトル。
 *
 * 概念的には[Size3d]にかなり近いが、こちらは線形代数学に従ったベクトルの演算が使用可能。
 */
data class Vector3d(val x: Size, val y: Size, val z: Size) {
   companion object {
      /** X軸と平行で長さが1mmのベクトル */
      val X_UNIT_VECTOR = Vector3d(1.mm, 0.mm, 0.mm)
      /** Y軸と平行で長さが1mmのベクトル */
      val Y_UNIT_VECTOR = Vector3d(0.mm, 1.mm, 0.mm)
      /** Z軸と平行で長さが1mmのベクトル */
      val Z_UNIT_VECTOR = Vector3d(0.mm, 0.mm, 1.mm)
   }

   constructor(size3d: Size3d) : this(size3d.x, size3d.y, size3d.z)
   constructor(from: Point3d, to: Point3d) : this(to - from)

   override fun toString() = "Vector(${x.numberAsMilliMeter}mm, ${y.numberAsMilliMeter}mm, ${z.numberAsMilliMeter}mm)"

   operator fun plus (vector: Vector3d) = Vector3d(x + vector.x, y + vector.y, z + vector.z)
   operator fun minus(vector: Vector3d) = Vector3d(x - vector.x, y - vector.y, z - vector.z)

   operator fun times(n: Int)    = Vector3d(x * n, y * n, z * n)
   operator fun times(n: Double) = Vector3d(x * n, y * n, z * n)
   operator fun div  (n: Int)    = Vector3d(x / n, y / n, z / n)
   operator fun div  (n: Double) = Vector3d(x / n, y / n, z / n)

   operator fun unaryMinus() = Vector3d(-x, -y, -z)
   operator fun unaryPlus () = Vector3d(+x, +y, +z)

   /** このベクトルの長さ */
   val norm get() = Size(
      sqrt(x.numberAsMilliMeter * x.numberAsMilliMeter
         + y.numberAsMilliMeter * y.numberAsMilliMeter
         + z.numberAsMilliMeter * z.numberAsMilliMeter)
   )
}

fun Iterable<Vector2d>.sum(): Vector2d {
   var sum = Vector2d(0.mm, 0.mm)
   for (v in this) {
      sum += v
   }
   return sum
}

fun Iterable<Vector3d>.sum(): Vector3d {
   var sum = Vector3d(0.mm, 0.mm, 0.mm)
   for (v in this) {
      sum += v
   }
   return sum
}
