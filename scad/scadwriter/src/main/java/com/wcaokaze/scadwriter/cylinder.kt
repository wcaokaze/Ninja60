package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

sealed class Cylinder : ScadPrimitiveObject()

data class Pillar(
   override val parent: ScadParentObject,
   val height: Size,
   val radius: Size,
   val center: Boolean
) : Cylinder() {
   constructor(
      parent: ScadParentObject,
      height: Size,
      radius: Size
   ) : this(parent, height, radius, center = false)

   override fun toScadRepresentation(): String
         = "cylinder(h = ${height.scad}, r = ${radius.scad}, center = $center, \$fa = ${fa.value.scad});"
}

data class Cone(
   override val parent: ScadParentObject,
   val height: Size,
   val bottomRadius: Size,
   val topRadius: Size,
   val center: Boolean = false
) : Cylinder() {
   constructor(
      parent: ScadParentObject,
      height: Size,
      bottomRadius: Size,
      topRadius: Size
   ) : this(parent, height, bottomRadius, topRadius, center = false)

   override fun toScadRepresentation(): String
         = "cylinder(h = ${height.scad}, r1 = ${bottomRadius.scad}, r2 = ${topRadius.scad}, center = $center, \$fa = ${fa.value.scad});"
}

fun ScadParentObject.cylinder(
   height: Size,
   radius: Size
): Pillar = cylinder(height, radius, center = false)

fun ScadParentObject.cylinder(
   height: Size,
   bottomRadius: Size,
   topRadius: Size
): Cone = cylinder(height, bottomRadius, topRadius, center = false)

fun ScadParentObject.cylinder(
   height: Size,
   radius: Size,
   center: Boolean
): Pillar {
   val pillar = Pillar(this, height, radius, center)
   addChild(pillar)
   return pillar
}

fun ScadParentObject.cylinder(
   height: Size,
   bottomRadius: Size, topRadius: Size,
   center: Boolean
): Cone {
   val cone = Cone(this, height, bottomRadius, topRadius, center)
   addChild(cone)
   return cone
}
