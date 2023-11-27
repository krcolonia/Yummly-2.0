package com.appdev.yummly2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.util.Log
import android.widget.ImageView
import com.appdev.yummly2.databinding.ActivityViewRecipeBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ViewRecipe : AppCompatActivity() {

  private lateinit var recipeBinding: ActivityViewRecipeBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    recipeBinding = ActivityViewRecipeBinding.inflate(layoutInflater)
    setContentView(recipeBinding.root)

    val retrieveID = intent.getStringExtra("foodID")

    val dbRef = retrieveID?.let { FirebaseDatabase.getInstance().reference.child("foods").child(it) }

    if (dbRef != null) {
      dbRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          val food = dataSnapshot.getValue(Food::class.java)

          if (food != null) {
            updateUI(food)
          }
        }
        override fun onCancelled(dbError: DatabaseError) {
          Log.e("ViewRecipe", "Error getting data from Firebase: $(databaseError.message)")
        }
      })
    }
  }

  private fun updateUI(food: Food) {
    food.e_imageFileName?.let { loadImageFromFirebase(it, recipeBinding.foodImg) }

    recipeBinding.foodName.text = food.b_name
    recipeBinding.ingredientsTxt.text = food.c_ingredients?.let { addBullets(it) }
    recipeBinding.instructionsTxt.text = food.d_instructions?.let{ addNumbers(it) }
  }

  private fun loadImageFromFirebase(imageFileName: String, imageView: ImageView) {
    val storageRef = FirebaseStorage.getInstance().reference.child(imageFileName)

    storageRef.downloadUrl.addOnSuccessListener { uri ->
      Glide.with(imageView.context)
        .load(uri)
        .placeholder(R.drawable.food_placeholder)
        .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
        .into(imageView)
    }.addOnFailureListener { exception ->
      Log.e("FirebaseImageLoad", "Error loading image: $exception")
    }
  }

  private fun addBullets(text: String): SpannableStringBuilder {
    val lines = text.split("\n")

    val builder = SpannableStringBuilder()
    for (line in lines) {
      builder.append("\u2022 ") // Unicode character for bullet point
      builder.append(line)
      builder.setSpan(BulletSpan(16), builder.length - line.length, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      builder.append("\n")
    }

    return builder
  }

  private fun addNumbers(text: String): SpannableStringBuilder {
    val lines = text.split("\n")

    val builder = SpannableStringBuilder()
    for ((index, line) in lines.withIndex()) {
      builder.append("${index + 1}. ") // Numbering starts from 1
      builder.append(line)
      builder.setSpan(LeadingMarginSpan.Standard(0, 40), builder.length - line.length, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      builder.append("\n\n")
    }

    return builder
  }
}