package com.appdev.yummly2

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
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

    private val homeContext = foodBinding.root.context

    private val name = foodBinding.foodName
    private val card = foodBinding.foodCard
    private val image = foodBinding.foodImg

    private val foodImage: Uri? = null

    fun bind(food: Food) {

      val foodHashMap = HashMap<String, Any>()
      food.b_name?.let { foodHashMap.put("b_name", it) }
      food.c_ingredients?.let { foodHashMap.put("c_ingredients", it) }
      food.d_instructions?.let { foodHashMap.put("d_instructions", it) }
      food.e_imageFileName?.let { foodHashMap.put("e_imageFileName", it) }

      name.text = food.b_name
      food.e_imageFileName?.let {
        adapter.loadImageFromFirebase(it, image)
      }

      card.setOnClickListener {
        val intent = Intent(homeContext, ViewRecipe::class.java)
        intent.putExtra("foodID", food.a_idno)
        intent.putExtra("foodName", food.b_name)
        homeContext.startActivity(intent)
      }

      card.setOnLongClickListener {
        Toast.makeText(homeContext, "Hold item ${food.b_name}", Toast.LENGTH_SHORT).show()
        val udAlert = AlertDialog.Builder(homeContext)
          .setTitle("Edit / Delete Recipe")
          .setMessage("Would you like to edit or delete the recipe for ${food.b_name}?")
          .setPositiveButton("Delete") { dialog, _ ->

          val confirmDelDialog = AlertDialog.Builder(homeContext)
          confirmDelDialog
            .setTitle("Confirm deletion?")
            .setMessage("Are you sure you'd want to delete the recipe for ${food.b_name}?\n" +
                "This action cannot be reversed!")
            .setPositiveButton("Confirm") { _, _ ->

              FirebaseDatabase.getInstance().reference.child("foods")
                .child(adapter.getRef(position).key!!).removeValue()

              FirebaseStorage.getInstance().reference.child("${food.e_imageFileName}")
                .delete()

              Toast.makeText(homeContext, "${food.b_name} Deleted!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { delDialog, _ ->
              Toast.makeText(homeContext, "Deletion Cancelled.", Toast.LENGTH_SHORT).show()
              delDialog.cancel()
            }
            .show()

          dialog.cancel()
        }
          .setNegativeButton("Edit") { dialog, _ ->
          Toast.makeText(homeContext, "Editing ${food.b_name}", Toast.LENGTH_SHORT).show()

          val editDialog = DialogPlus.newDialog(homeContext)
            .setContentHolder(ViewHolder(R.layout.show_update))
            .setExpanded(false)
            .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
            .setContentHeight(800)
            .create()

          val view = editDialog.holderView
          val name: EditText = view.findViewById(R.id.updateNameTxt)
          val ingredients:EditText = view.findViewById(R.id.updateIngredientsTxt)
          val instructions:EditText = view.findViewById(R.id.updateInstructionsTxt)
          val btnUpdate:Button = view.findViewById(R.id.updateFoodBtn)
          val btnCancel:Button = view.findViewById(R.id.cancelUpdateBtn)

          val spinner = view.findViewById<Spinner>(R.id.id_list_origin)
          val listAdapter = ArrayAdapter.createFromResource(
            foodBinding.root.context,
            R.array.recipeOrigin,
            android.R.layout.simple_spinner_item
          )

          listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
          spinner.adapter = listAdapter

          name.setText(food.b_name)
          ingredients.setText(food.c_ingredients)
          instructions.setText(food.d_instructions)

          editDialog.show()
          btnUpdate.setOnClickListener {
            if(foodImage == null) {
              Toast.makeText(homeContext, "You haven\'t selected an image yet!", Toast.LENGTH_SHORT).show()
            }
            else if(name.text.isEmpty()) {
              validationPrompt("name", homeContext)
              return@setOnClickListener
            }
            else if(ingredients.text.isEmpty()) {
              validationPrompt("ingredients", homeContext)
              return@setOnClickListener
            }
            else if(instructions.text.isEmpty()) {
              validationPrompt("name", homeContext)
              return@setOnClickListener
            }

            FirebaseDatabase.getInstance().reference.child("foods")
              .child(adapter.getRef(position).key!!).updateChildren(foodHashMap)
              .addOnSuccessListener {
                Toast.makeText(homeContext, "Recipe Edited Successfully", Toast.LENGTH_SHORT).show()
                editDialog.dismiss()
              }
              .addOnFailureListener {
                Toast.makeText(homeContext, "Recipe Editing Failed!", Toast.LENGTH_SHORT).show()
                editDialog.dismiss()
              }
          }
          dialog.cancel()
        }
        udAlert.show()
        true
      }
    }

    private fun validationPrompt(field: String, context: Context?) {
      Toast.makeText(context, "Please enter the food\'s $field.", Toast.LENGTH_SHORT).show()
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