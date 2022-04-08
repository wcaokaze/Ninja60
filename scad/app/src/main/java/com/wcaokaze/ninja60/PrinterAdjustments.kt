package com.wcaokaze.ninja60

import com.wcaokaze.scadwriter.*
import com.wcaokaze.scadwriter.foundation.*

/**
 * 3Dプリンタの特性によって変化する値など
 */
object PrinterAdjustments {
   /**
    * 壁を正常に出力できる最小の厚さ。
    */
   val minWallThickness = PropagatedValue { 2.mm }

   /**
    * 突起物を正常に出力できる最小の大きさ。
    */
   val minProtuberanceSize = PropagatedValue { Size3d(1.mm, 1.mm, 1.mm) }

   /**
    * 凹みを正確に出力できる最小の大きさ。
    */
   val minHollowSize = PropagatedValue { Size3d(1.mm, 1.mm, 1.mm) }

   /**
    * 可動パーツにおいて他のパーツとの接触を回避するために開けるべき最小の空間。
    */
   val movableMargin = PropagatedValue { 0.25.mm }

   /**
    * フィラメント特性によって本来のサイズより大きめに出る誤差。
    * 小さめに出る場合負値
    */
   val errorSize = PropagatedValue { 0.1.mm }
}
