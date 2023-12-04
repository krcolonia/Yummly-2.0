package com.appdev.yummly2

import android.net.Uri

data class Food(
  val a_idno: String? = null,
  val b_authid: String? = null,
  val c_name:String? = null,
  val d_origin:String? = null,
  val e_ingredients:String? = null,
  val f_instructions:String? = null,
  val g_imageFileName: String? = null,
  val image:Uri? = null
) {
  // Default constructor required for calls to DataSnapshot.getValue(Food.class)
  constructor() : this("", "")
}
