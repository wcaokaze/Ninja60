package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

/**
 * OpenSCADのコードとして出力される物体。
 *
 * いわゆるCompositeパターンになっていて
 * [Cube]などの[ScadPrimitiveObject]と[Translate]などの[ScadPrimitiveObject]に分かれる。
 *
 * [ScadParentObject]には対応するDSL(e.g. [Translate]には[translate]など)を利用することで
 * 子を追加できるが、少し癖があり、
 *
 * [cube]など各種ScadObject生成関数を呼び出した時点ですでに一旦親にScadObjectが追加されていて、
 * 生成した子に対して[+演算][ScadParentObject.plus]などを行うと、
 * オペランドとなるScadObject2つが親から除去されたのちに
 * 演算の結果が改めて親に追加されるような動きになる。
 */
sealed class ScadObject : ScadValue()

abstract class ScadPrimitiveObject : ScadObject()

abstract class ScadParentObject : ScadObject() {
   private val children = ArrayList<ScadObject>()

   protected abstract val parent: ScadParentObject

   open val propagatedValues: PropagatedValues
      get() = parent.propagatedValues

   val <T> PropagatedValue<T>.value: T
      get() = propagatedValues[this]

   private class PropagatedValueProviderScadObject(
      override val parent: ScadParentObject,
      override val propagatedValues: PropagatedValues
   ) : ScadParentObject() {
      override fun toScadRepresentation() = ""
   }

   fun provideValue(vararg value: ProvidingPropagatedValue<*>,
                    children: ScadParentObject.() -> Unit): ScadParentObject
   {
      val propagatedValueProviderScadObject
            = PropagatedValueProviderScadObject(this, propagatedValues + value)
      addChild(propagatedValueProviderScadObject)
      propagatedValueProviderScadObject.children()
      return propagatedValueProviderScadObject
   }

   /**
    * [addChild]と違いこのScadParentObjectではなくファイルの先頭に追加する。
    * [use]とかそういうやつですよね
    */
   open fun addHeader(headerObject: ScadObject) {
      parent.addHeader(headerObject)
   }

   fun addChild(child: ScadObject) {
      children += child
   }

   protected fun buildChildrenScad(scad: String): String {
      return "$scad ${buildScadBlock(children)}"
   }

   /** [union]の略記。 */
   operator fun ScadObject.plus(another: ScadObject): Union {
      children -= this
      children -= another

      return union {
         addChild(this@plus)
         addChild(another)
      }
   }

   /** [difference]の略記。 */
   operator fun ScadObject.minus(another: ScadObject): Difference {
      children -= this
      children -= another

      return difference {
         addChild(this@minus)
         addChild(another)
      }
   }

   infix fun ScadObject.hull(another: ScadObject): Hull {
      children -= this
      children -= another

      return hull {
         addChild(this@hull)
         addChild(another)
      }
   }

   infix fun ScadObject.intersection(another: ScadObject): Intersection {
      children -= this
      children -= another

      return intersection {
         addChild(this@intersection)
         addChild(another)
      }
   }

   fun ScadObject.rotate(
      x: Angle = 0.0.rad,
      y: Angle = 0.0.rad,
      z: Angle = 0.0.rad
   ): Rotate {
      children -= this

      return rotate(x, y, z) {
         addChild(this@rotate)
      }
   }

   fun ScadObject.rotate(a: Angle, v: Point3d): RotateWithAxis {
      children -= this

      return rotate(a, v) {
         addChild(this@rotate)
      }
   }

   fun ScadObject.translate(
      x: Size = 0.mm,
      y: Size = 0.mm,
      z: Size = 0.mm
   ): ScadObject {
      children -= this

      return translate(x, y, z) {
         addChild(this@translate)
      }
   }

   fun ScadObject.translate(distance: Size3d): Translate {
      children -= this

      return translate(distance) {
         addChild(this@translate)
      }
   }

   fun ScadObject.locale(
      x: Point = Point.ORIGIN,
      y: Point = Point.ORIGIN,
      z: Point = Point.ORIGIN
   ): Translate {
      children -= this

      return locale(x, y, z) {
         addChild(this@locale)
      }
   }

   fun ScadObject.locale(point: Point3d): Translate {
      children -= this

      return locale(point) {
         addChild(this@locale)
      }
   }
}
