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
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.appdev.yummly2.databinding.ActivityAddFoodBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class AddFoodActivity : AppCompatActivity() {

  private lateinit var addBinding: ActivityAddFoodBinding

  private var foodImage: Uri? = null
  private lateinit var imagePreview: ImageView

  private lateinit var foodName: EditText
  private lateinit var foodIngredients: EditText
  private lateinit var foodInstructions: EditText
  private lateinit var cancelBtn: Button

  private lateinit var saveBtn: Button


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    addBinding = ActivityAddFoodBinding.inflate(layoutInflater)
    setContentView(addBinding.root)

    /* Start of Image Selection */

    imagePreview = addBinding.foodImg // Initializes the ImageView for preview of selected image

    val selectImage = addBinding.selectImageCard // Initializes the button used to open image selection

    selectImage.setOnClickListener {
      val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
      galleryIntent.type = "image/*" // Launches photo selector

      imagePickerActivityResult.launch(galleryIntent)
    }
    /* End of Image Selection */

    foodName = addBinding.foodNameTxt
    foodIngredients = addBinding.foodIngredientsTxt
    foodInstructions = addBinding.foodInstructionsTxt

    saveBtn = addBinding.saveFoodBtn

    saveBtn.setOnClickListener {

      var foodImage_up: Uri? = foodImage
      if(foodImage_up == null) {
        Toast.makeText(applicationContext, "You haven\'t selected an image yet!", Toast.LENGTH_SHORT).show()
      }

      var foodName_up = foodName.text.toString()
      if(foodName_up.isEmpty()) {
        validateInput("name")
        return@setOnClickListener
      }

      var foodIngredients_up = foodIngredients.text.toString()//.replace("\n", "\\n")
      if(foodIngredients_up.isEmpty()) {
        validateInput("ingredients")
        return@setOnClickListener
      }

      var foodInstructions_up = foodInstructions.text.toString()//.replace("\n", "\\n")
      if(foodInstructions_up.isEmpty()) {
        validateInput("instructions")
        return@setOnClickListener
      }

      if (foodImage_up != null) {
        insertRecord(foodImage_up, foodName_up, foodIngredients_up, foodInstructions_up)
      }
    }

    cancelBtn = addBinding.cancelAddBtn

    cancelBtn.setOnClickListener {
      Toast.makeText(applicationContext, "Returning to Home Page", Toast.LENGTH_SHORT).show()
      this.finish()
    }

  }

  private val imagePickerActivityResult: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if(result.resultCode == Activity.RESULT_OK) {
        if(result.data != null && result.data?.data!=null) {
          foodImage = result.data?.data

          var foodImageBitmap: Bitmap? = null
          try {
            foodImageBitmap = MediaStore.Images.Media.getBitmap(
              this.contentResolver,
              foodImage)
          }
          catch (e: IOException) {
            e.printStackTrace()
          }
          imagePreview.setImageBitmap(foodImageBitmap)
        }
      }
    }

  private fun insertRecord(image:Uri, name: String, ingredients:String, instructions:String) {
    val sd = getFileName(applicationContext, image!!)

    val foodData_hashmap = HashMap<String, Any>()
    foodData_hashmap.put("b_name", name)
    foodData_hashmap.put("c_ingredients", ingredients)
    foodData_hashmap.put("d_instructions", instructions)
    if (sd != null) {
      foodData_hashmap.put("e_imageFileName", "file/$sd")
    }

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dbRef = database.getReference("foods")

    val idKey = dbRef.push().key
    foodData_hashmap.put("a_idno", idKey!!)

    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageRef = storage.reference

    val uploadTask = storageRef.child("file/$sd").putFile(image)

    dbRef.child(idKey).setValue(foodData_hashmap).addOnCompleteListener {
      uploadTask.addOnSuccessListener {
        Toast.makeText(applicationContext, "Food Recipe Saved!", Toast.LENGTH_SHORT).show()
        this.finish()
      }
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

  private fun validateInput(info: String) {
    Toast.makeText(applicationContext, "Food\'s $info is empty!", Toast.LENGTH_SHORT).show()
  }

}