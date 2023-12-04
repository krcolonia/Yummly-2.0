package com.appdev.yummly2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import com.appdev.yummly2.databinding.ActivityEditFoodBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.lang.ref.WeakReference

class EditFoodActivity : AppCompatActivity() {

  private lateinit var editBinding: ActivityEditFoodBinding

  
  private lateinit var name: EditText
  private lateinit var originSpinner: Spinner
  private lateinit var ingredients: EditText
  private lateinit var instructions: EditText

  private var foodImage: Uri? = null
  private var oldImage = ""
  private lateinit var imagePreview: ImageView
  private lateinit var selectImage: CardView

  private lateinit var btnUpdate: Button
  private lateinit var btnCancel: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    editBinding = ActivityEditFoodBinding.inflate(layoutInflater)
    setContentView(editBinding.root)

    val retrieveID = intent.getStringExtra("foodID")

    name = editBinding.updateNameTxt

    originSpinner = editBinding.updateOrigin
    val listAdapter = ArrayAdapter.createFromResource(
      this,
      R.array.recipeOrigin,
      android.R.layout.simple_spinner_item
    )

    listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    originSpinner.adapter = listAdapter

    ingredients = editBinding.updateIngredientsTxt
    instructions = editBinding.updateInstructionsTxt

    imagePreview = editBinding.foodImg

    val dbRef =
      retrieveID?.let { FirebaseDatabase.getInstance().reference.child("foods").child(it) }

    if (dbRef != null) {
      dbRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          val food = dataSnapshot.getValue(Food::class.java)

          if (food != null) {
            food.g_imageFileName?.let { loadImageFromFirebase(it, imagePreview) }

            oldImage = food.g_imageFileName!!

            name.setText(food.c_name)
            if(food.d_origin != null) {
              val position = listAdapter.getPosition(food.d_origin)
              originSpinner.setSelection(position)
            }
            ingredients.setText(food.e_ingredients)
            instructions.setText(food.f_instructions)
          }
        }

        override fun onCancelled(dbError: DatabaseError) {
          Log.e("ViewRecipe", "Error getting data from Firebase: $(databaseError.message)")
        }
      })


      selectImage = editBinding.selectImageCard
      selectImage.setOnClickListener {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*" // Launches photo selector

        imagePickerActivityResult.launch(galleryIntent)
      }

      btnCancel = editBinding.cancelUpdateBtn
      btnCancel.setOnClickListener {
        Toast.makeText(applicationContext, "Cancelling editing...", Toast.LENGTH_SHORT).show()
        this.finish()
      }

      btnUpdate = editBinding.updateFoodBtn
      btnUpdate.setOnClickListener {
        var foodImage_up: Uri? = foodImage

        var foodName_up = name.text.toString()
        if (foodName_up.trim().isEmpty()) {
          validationPrompt("name")
          return@setOnClickListener
        }

        var foodOrigin_up = originSpinner.selectedItem.toString()
        if(originSpinner.selectedItemPosition == 0) {
          validationPrompt("country of origin")
          return@setOnClickListener
        }

        var foodIngredients_up = ingredients.text.toString()
        if (foodIngredients_up.trim().isEmpty()) {
          validationPrompt("ingredients")
          return@setOnClickListener
        }

        var foodInstructions_up = instructions.text.toString()
        if (foodInstructions_up.trim().isEmpty()) {
          validationPrompt("instructions")
          return@setOnClickListener
        }

        updateRecord(retrieveID, oldImage, foodImage_up, foodName_up, foodOrigin_up, foodIngredients_up, foodInstructions_up)
      }
    }
  }

  private fun updateRecord(id: String, oldImage: String, image: Uri?, name: String, origin: String, ingredients: String, instructions: String) {
    val foodData_hashmap = HashMap<String, Any>()

    foodData_hashmap.put("c_name", name)
    foodData_hashmap.put("d_origin", origin)
    foodData_hashmap.put("e_ingredients", ingredients)
    foodData_hashmap.put("f_instructions", instructions)

    if (image != null){
      val sd = getFileName(applicationContext, image!!)
      if (sd != null) {
        foodData_hashmap.put("g_imageFileName", "file/${id}_${sd}")
      }

      FirebaseStorage.getInstance().reference.child(oldImage).delete()

      FirebaseStorage.getInstance().reference.child("file/${id}_${sd}").putFile(image)
    }

    FirebaseDatabase.getInstance().reference.child("foods")
      .child(id).updateChildren(foodData_hashmap)
      .addOnSuccessListener {
        Toast.makeText(applicationContext, "Recipe Edited Successfully", Toast.LENGTH_SHORT).show()
        this.finish()
      }
      .addOnFailureListener {
        Toast.makeText(applicationContext, "Recipe Editing Failed!", Toast.LENGTH_SHORT).show()
      }

  }

  private fun getFileName(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
      val cursor = context.contentResolver.query(uri, null, null, null, null)
      cursor?.use {
        if (it.moveToFirst()) {
          val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (displayNameIndex != -1) {
            return it.getString(displayNameIndex)
          } else {
            return "Unknown File"
          }
        }
      }
    }
    return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
  }

  private val imagePickerActivityResult: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        if (result.data != null && result.data?.data != null) {
          foodImage = result.data?.data

          var foodImageBitmap: Bitmap? = null
          try {
            foodImageBitmap = MediaStore.Images.Media.getBitmap(
              this.contentResolver,
              foodImage
            )
          } catch (e: IOException) {
            e.printStackTrace()
          }
          imagePreview.setImageBitmap(foodImageBitmap)
        }
      }
    }

  private fun loadImageFromFirebase(imageFileName: String, imageView: ImageView) {
    val activityRef = WeakReference(imageView.context as? AppCompatActivity)
    val storageRef = FirebaseStorage.getInstance().reference.child(imageFileName)

    storageRef.downloadUrl.addOnSuccessListener { uri ->
      activityRef.get()?.let { activity ->
        if (!activity.isDestroyed && !activity.isFinishing) {
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

  private fun validationPrompt(field: String) {
    Toast.makeText(applicationContext, "Please enter the food\'s $field.", Toast.LENGTH_SHORT).show()
  }

}