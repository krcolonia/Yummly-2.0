package com.appdev.yummly2

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.yummly2.databinding.FoodItemBinding
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import java.io.IOException

class FoodAdapter(
  options: FirebaseRecyclerOptions<Food>
) : FirebaseRecyclerAdapter<Food, FoodAdapter.FoodViewHolder>(options) {

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

  class FoodViewHolder(
    foodBinding: FoodItemBinding,
    private val adapter: FoodAdapter
  ) : RecyclerView.ViewHolder(foodBinding.root) {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var aName: String
    private var currentUserId: String? = null
    private val homeContext = foodBinding.root.context

    private val name = foodBinding.foodName
    private val authorName = foodBinding.authorName
    private val card = foodBinding.foodCard
    private val image = foodBinding.foodImg

    private var foodImage: Uri? = null

    fun bind(food: Food) {
      mAuth = FirebaseAuth.getInstance()

      val currentUser = mAuth.currentUser

      if(currentUser != null) {
        currentUserId = currentUser.uid
      }

      name.text = food.c_name

      val usernameRef = FirebaseDatabase.getInstance().reference.child("users").child(food.b_authid!!).child("username")

      usernameRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          aName = dataSnapshot.value.toString()
          authorName.text = aName
        }

        override fun onCancelled(databaseError: DatabaseError) {
          // Handle error
        }
      })

      food.g_imageFileName?.let {
        foodImage = Uri.parse(it)
        adapter.loadImageFromFirebase(it, image)
      }

      card.setOnClickListener {
        val intent = Intent(homeContext, ViewRecipe::class.java)
        intent.putExtra("foodID", food.a_idno)
        intent.putExtra("foodName", food.c_name)
        intent.putExtra("authName", aName)
        homeContext.startActivity(intent)
      }

      card.setOnLongClickListener {
        if(currentUserId == food.b_authid) {
          val editDialog = DialogPlus.newDialog(homeContext)
            .setContentHolder(ViewHolder(R.layout.show_update))
            .setExpanded(false)
            .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
            .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            .create()

          val view = editDialog.holderView
          val btnEdit: Button = view.findViewById(R.id.editBtn)
          val btnDelete: Button = view.findViewById(R.id.deleteBtn)

          btnEdit.setOnClickListener {
            Toast.makeText(homeContext, "Editing ${food.c_name}", Toast.LENGTH_SHORT).show()
            val intent = Intent(homeContext, EditFoodActivity::class.java)
            intent.putExtra("foodID", food.a_idno)
            homeContext.startActivity(intent)
            editDialog.dismiss()
          }

          btnDelete.setOnClickListener {
            val progressDialog = ProgressDialog(homeContext)

            val confirmDelDialog = AlertDialog.Builder(homeContext)
            confirmDelDialog
              .setTitle("Confirm deletion?")
              .setMessage(
                "Are you sure you'd want to delete the recipe for ${food.c_name}?\n" +
                    "This action cannot be reversed!"
              )
              .setPositiveButton("Confirm") { dialog, _ ->
                progressDialog.setMessage("Deleting ${food.c_name}...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                Handler(homeContext.mainLooper).postDelayed(
                  { progressDialog.dismiss() },
                  1500
                )

                FirebaseDatabase.getInstance().reference.child("foods")
                  .child(adapter.getRef(position).key!!).removeValue()

                FirebaseStorage.getInstance().reference.child("${food.g_imageFileName}")
                  .delete()

                Toast.makeText(homeContext, "${food.c_name} Deleted!", Toast.LENGTH_SHORT).show()

                dialog.cancel()
                editDialog.dismiss()
              }
              .setNegativeButton("Cancel") { dialog, _ ->
                Toast.makeText(homeContext, "Deletion Cancelled.", Toast.LENGTH_SHORT).show()
                dialog.cancel()
              }

            confirmDelDialog.show()

            editDialog.dismiss()
          }
          editDialog.show()
        }
        else {
          Toast.makeText(homeContext, "You cannot edit this recipe.", Toast.LENGTH_SHORT).show()
        }
        true
      }
    }
  }

  private fun loadImageFromFirebase(imageFileName: String, imageView: ImageView) {
    val storageRef = FirebaseStorage.getInstance().reference.child(imageFileName)

    storageRef.downloadUrl.addOnSuccessListener { uri ->
      Glide.with(imageView.context)
        .load(uri)
        .placeholder(R.drawable.food_placeholder)
        .error(R.drawable.food_placeholder)
        .into(imageView)
    }.addOnFailureListener { exception ->
      Log.e("FirebaseImageLoad", "Error loading image: $exception")
    }
  }
}