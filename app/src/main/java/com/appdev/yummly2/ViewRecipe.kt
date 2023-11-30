package com.appdev.yummly2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import com.appdev.yummly2.databinding.ActivityViewRecipeBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.lang.ref.WeakReference

class ViewRecipe : AppCompatActivity() {

  private lateinit var recipeBinding: ActivityViewRecipeBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    recipeBinding = ActivityViewRecipeBinding.inflate(layoutInflater)
    setContentView(recipeBinding.root)

    val retrieveID = intent.getStringExtra("foodID")
    val foodName = intent.getStringExtra("foodName")

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

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_arrowback)
    supportActionBar?.title = "$foodName Recipe"
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun updateUI(food: Food) {
    food.e_imageFileName?.let { loadImageFromFirebase(it, recipeBinding.foodImg) }

    recipeBinding.foodName.text = food.b_name
    recipeBinding.ingredientsTxt.text = food.c_ingredients?.let { addBullets(it) }
    recipeBinding.instructionsTxt.text = food.d_instructions?.let{ addNumbers(it) }
  }

  private fun loadImageFromFirebase(imageFileName: String, imageView: ImageView) {
    val activityRef = WeakReference(imageView.context as? AppCompatActivity)
    val storageRef = FirebaseStorage.getInstance().reference.child(imageFileName)

    storageRef.downloadUrl.addOnSuccessListener { uri ->
      activityRef.get()?.let { activity ->
        if(!activity.isDestroyed && !activity.isFinishing) {
          Glide.with(imageView.context)
            .load(uri)
            .placeholder(R.drawable.food_placeholder)
            .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
            .into(imageView)
        }
      }
    }.addOnFailureListener { exception ->
      Log.e("FirebaseImageLoad", "Error loading image: $exception")
    }
  }

  private fun addBullets(text: String): SpannableStringBuilder {
    val lines = text.split(Regex("\n+"))

    val builder = SpannableStringBuilder()
    for ((index, line) in lines.withIndex()) {
      if (index > 0) {
        builder.append("\n")
      }

      builder.append("\u2022 ") // Unicode character for bullet point
      builder.append(line.trim()) // Trim leading and trailing whitespaces
      builder.setSpan(
        BulletSpan(16),
        builder.length - line.length,
        builder.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }

    return builder
  }

  private fun addNumbers(text: String): SpannableStringBuilder {
    val lines = text.split(Regex("\n+"))

    val builder = SpannableStringBuilder()
    for ((index, line) in lines.withIndex()) {
      if (index > 0) {
        builder.append("\n\n")
      }

      builder.append("${index + 1}. ") // Numbering starts from 1
      builder.append(line.trim()) // Trim leading and trailing whitespaces
      builder.setSpan(
        LeadingMarginSpan.Standard(0, 40),
        builder.length - line.length,
        builder.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }

    return builder
  }
}