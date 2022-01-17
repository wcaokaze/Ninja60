package com.wcaokaze.scadwriter

import com.wcaokaze.scadwriter.foundation.*

sealed class Cylinder : ScadPrimitiveObject()

data class Pillar(
   val height: Size,
   val radius: Size,
   val center: Boolean,
   val fa: Angle
) : Cylinder() {
   constructor(
      height: Size,
      radius: Size,
      fa: Angle
   ) : this(height, radius, center = false, fa)

   override fun toScadRepresentation(): String
         = "cylinder(h = ${height.scad}, r = ${radius.scad}, center = $center, \$fa = ${fa.scad});"
}

data class Cone(
   val height: Size,
   val bottomRadius: Size,
   val topRadius: Size,
   val center: Boolean = false,
   val fa: Angle
) : Cylinder() {
   constructor(
      height: Size,
      bottomRadius: Size,
      topRadius: Size,
      fa: Angle
   ) : this(height, bottomRadius, topRadius, center = false, fa)

   override fun toScadRepresentation(): String
         = "cylinder(h = ${height.scad}, ${bottomRadius.scad}, ${topRadius.scad}, center = $center, \$fa = ${fa.scad});"
}

fun ScadParentObject.cylinder(
   height: Size,
   radius: Size,
   fa: Angle
): Pillar = cylinder(height, radius, center = false, fa)

fun ScadParentObject.cylinder(
   height: Size,
   bottomRadius: Size,
   topRadius: Size,
   fa: Angle
): Cone = cylinder(height, bottomRadius, topRadius, center = false, fa)

fun ScadParentObject.cylinder(
   height: Size,
   radius: Size,
   center: Boolean,
   fa: Angle
): Pillar {
   val pillar = Pillar(height, radius, center, fa)
   addChild(pillar)
   return pillar
}

fun ScadParentObject.cylinder(
   height: Size,
   bottomRadius: Size, topRadius: Size,
   center: Boolean,
   fa: Angle
): Cone {
   val cone = Cone(height, bottomRadius, topRadius, center, fa)
   addChild(cone)
   return cone
}
