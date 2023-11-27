package com.appdev.yummly2

import android.net.Uri

data class Food(
  val a_idno: String? = null,
  val b_name:String? = null,
  val c_ingredients:String? = null,
  val d_instructions:String? = null,
  val e_imageFileName: String? = null,
  val image:Uri? = null
) {
  // Default constructor required for calls to DataSnapshot.getValue(Food.class)
  constructor() : this("", "")
}
