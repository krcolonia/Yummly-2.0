package com.appdev.yummly2

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.appdev.yummly2.databinding.FoodItemBinding
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

class FoodAdapter(options: FirebaseRecyclerOptions<Food>) : FirebaseRecyclerAdapter<Food, FoodAdapter.FoodViewHolder>(options) {

  override fun onBindViewHolder(holder: FoodViewHolder, position: Int, food: Food) {
    if (position != RecyclerView.NO_POSITION) {
      holder.bind(food)
    }
  }
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = FoodItemBinding.inflate(inflater, parent, false)

    return FoodViewHolder(binding, this)
  }

  class FoodViewHolder(private val foodBinding: FoodItemBinding, private val adapter: FoodAdapter) : RecyclerView.ViewHolder(foodBinding.root) {
    fun bind(food: Food) {
      val foodId = food.a_idno

      foodBinding.foodName.text = food.b_name
      food.e_imageFileName?.let {
        adapter.loadImageFromFirebase(it, foodBinding.foodImg)
      }

      foodBinding.foodCard.setOnClickListener {
        Toast.makeText(foodBinding.root.context, foodId, Toast.LENGTH_SHORT).show()
        val intent = Intent(foodBinding.root.context, ViewRecipe::class.java)
        intent.putExtra("foodID", foodId)
        foodBinding.root.context.startActivity(intent)
      }
    }
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

  private fun validationPrompt(field: String, context: Context?) {
    Toast.makeText(context, "Please enter the food\'s $field.", Toast.LENGTH_SHORT).show()
  }
}