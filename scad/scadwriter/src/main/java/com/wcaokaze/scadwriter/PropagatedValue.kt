package com.wcaokaze.scadwriter

/**
 * Jetpack ComposeのCompositionLocalとだいたい同じ。
 *
 * 説明が難しいからCompositionLocal見てくれ
 *
 * @see ScadParentObject.provideValue
 */
class PropagatedValue<T>(val default: (() -> T)?) {
   constructor() : this(null)

   infix fun provides(value: T) = ProvidingPropagatedValue(this, value)
}

data class ProvidingPropagatedValue<T>(val key: PropagatedValue<T>, val value: T)

class PropagatedValues
   private constructor(private val values: List<ProvidingPropagatedValue<*>>)
{
   constructor() : this(emptyList())

   operator fun <T> get(key: PropagatedValue<T>): T {
      val p = values.firstOrNull { it.key == key }

      if (p != null) {
         @Suppress("UNCHECKED_CAST")
         return p.value as T
      }

      val defValProvider = key.default ?: throw NoSuchElementException()
      return defValProvider()
   }

   operator fun plus(value: ProvidingPropagatedValue<*>)
         = PropagatedValues(values + value)

   operator fun plus(values: List<ProvidingPropagatedValue<*>>)
         = PropagatedValues(this.values + values)

   operator fun plus(values: Array<out ProvidingPropagatedValue<*>>)
         = PropagatedValues(this.values + values)
}

interface PropagatedValueProvider {
   val <T> PropagatedValue<T>.value: T
}
