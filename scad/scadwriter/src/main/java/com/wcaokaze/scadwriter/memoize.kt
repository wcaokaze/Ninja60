package com.wcaokaze.scadwriter

class Module
   private constructor(
      override val parent: ScadParentObject,
      private val moduleName: String
   )
   : ScadParentObject()
{
   companion object {
      private var moduleCount = 0
   }

   constructor(parent: ScadParentObject) : this(parent, "module${moduleCount++}")

   override fun toScadRepresentation()= buildChildrenScad("module $moduleName()")

   val call get() = ModuleCall(moduleName)
}

class ModuleCall
   internal constructor(private val moduleName: String)
   : ScadObject()
{
   override fun toScadRepresentation() = "$moduleName();"
}

/**
 * メモ化。
 *
 * ```kotlin
 * fun ScadParentObject.multiCube(size: Size, distance: Size) {
 *    union {
 *       for (xTranslate in listOf(0, distance)) {
 *          translate(x = xTranslate) {
 *             cube(x = size, y = size, z = size)
 *          }
 *       }
 *    }
 * }
 * ```
 * などとすることで物体の生成を関数にし、ある程度再利用可能にできるが、
 * 実際に再利用する場合、
 * ```kotlin
 * multiCube(1.mm, distance = 5.mm)
 * multiCube(1.mm, distance = 5.mm)
 * ```
 * ```openscad
 * union() {
 *    translate([0, 0, 0]) {
 *       cube([1, 1, 1]);
 *    }
 *    translate([5, 0, 0]) {
 *       cube([1, 1, 1]);
 *    }
 * }
 * union() {
 *    translate([0, 0, 0]) {
 *       cube([1, 1, 1]);
 *    }
 *    translate([5, 0, 0]) {
 *       cube([1, 1, 1]);
 *    }
 * }
 * ```
 * という具合に同じSCADコードが二度出力される。
 *
 * memoizeを使い、
 * ```kotlin
 * val memoizedMultiCube = memoize { multiCube(1.mm, distance = 5.mm) }
 * memoizedMultiCube()
 * memoizedMultiCube()
 * ```
 * とすることでmoduleが生成され、
 * ```openscad
 * module module1() {
 *    union() {
 *       translate([0, 0, 0]) {
 *          cube([1, 1, 1]);
 *       }
 *       translate([5, 0, 0]) {
 *          cube([1, 1, 1]);
 *       }
 *    }
 * }
 * module1();
 * module1();
 * ```
 * コードの重複を回避し、レンダリング時にもメモ化によって二度目以降の処理の
 * 高速化を期待できる。
 */
fun ScadParentObject.memoize(children: Module.() -> Unit)
   : ScadParentObject.() -> ModuleCall
{
   val module = Module(this)
   addHeader(module)
   module.children()

   return fun ScadParentObject.(): ModuleCall {
      val moduleCall = module.call
      addChild(moduleCall)
      return moduleCall
   }
}
