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
sealed class ScadObject : ScadValue(), PropagatedValueProvider {
   protected abstract val parent: ScadParentObject

   open val propagatedValues: PropagatedValues
      get() = parent.propagatedValues

   override val <T> PropagatedValue<T>.value: T
      get() = propagatedValues[this]
}

abstract class ScadPrimitiveObject : ScadObject()

abstract class ScadParentObject : ScadObject() {
   private val _children = ArrayList<ScadObject>()
   protected val children: List<ScadObject> get() = _children

   private class PropagatedValueProviderScadObject(
      override val parent: ScadParentObject,
      override val propagatedValues: PropagatedValues
   ) : ScadParentObject() {
      override fun toScadRepresentation()
            = children.joinToString("\n") { it.toScadRepresentation() }
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
      _children += child
   }

   protected fun buildChildrenScad(scad: String): String {
      return "$scad ${buildScadBlock(children)}"
   }

   /** [union]の略記。 */
   operator fun ScadObject.plus(another: ScadObject): Union {
      _children -= this
      _children -= another

      return union {
         addChild(this@plus)
         addChild(another)
      }
   }

   /** [difference]の略記。 */
   operator fun ScadObject.minus(another: ScadObject): Difference {
      _children -= this
      _children -= another

      return difference {
         addChild(this@minus)
         addChild(another)
      }
   }

   infix fun ScadObject.hull(another: ScadObject): Hull {
      _children -= this
      _children -= another

      return hull {
         addChild(this@hull)
         addChild(another)
      }
   }

   infix fun ScadObject.intersection(another: ScadObject): Intersection {
      _children -= this
      _children -= another

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
      _children -= this

      return rotate(x, y, z) {
         addChild(this@rotate)
      }
   }

   fun ScadObject.rotate(a: Angle, v: Point3d): RotateWithAxis {
      _children -= this

      return rotate(a, v) {
         addChild(this@rotate)
      }
   }

   fun ScadObject.translate(
      x: Size = 0.mm,
      y: Size = 0.mm,
      z: Size = 0.mm
   ): ScadObject {
      _children -= this

      return translate(x, y, z) {
         addChild(this@translate)
      }
   }

   fun ScadObject.translate(distance: Size3d): Translate {
      _children -= this

      return translate(distance) {
         addChild(this@translate)
      }
   }

   fun ScadObject.locale(
      x: Point = Point.ORIGIN,
      y: Point = Point.ORIGIN,
      z: Point = Point.ORIGIN
   ): Translate {
      _children -= this

      return locale(x, y, z) {
         addChild(this@locale)
      }
   }

   fun ScadObject.locale(point: Point3d): Translate {
      _children -= this

      return locale(point) {
         addChild(this@locale)
      }
   }
}
