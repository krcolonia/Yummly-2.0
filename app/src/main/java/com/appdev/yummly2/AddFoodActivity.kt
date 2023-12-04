package com.appdev.yummly2

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.appdev.yummly2.databinding.ActivityAddFoodBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException


class AddFoodActivity : AppCompatActivity() {

  private lateinit var addBinding: ActivityAddFoodBinding

  private lateinit var mAuth: FirebaseAuth

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

    FirebaseApp.initializeApp(this)

    /* Retrieving instance of Firebase Authentication from YummlyDK Firebase project */
    mAuth = FirebaseAuth.getInstance()

    /* Start of Image Selection */

    imagePreview = addBinding.foodImg // Initializes the ImageView for preview of selected image

    val selectImage =
      addBinding.selectImageCard // Initializes the button used to open image selection

    selectImage.setOnClickListener {
      val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
      galleryIntent.type = "image/*" // Launches photo selector

      imagePickerActivityResult.launch(galleryIntent)
    }
    /* End of Image Selection */

    foodName = addBinding.foodNameTxt
    foodIngredients = addBinding.foodIngredientsTxt
    foodInstructions = addBinding.foodInstructionsTxt

    val spinner = addBinding.foodOrigin
    val listAdapter = ArrayAdapter.createFromResource(
      applicationContext,
      com.appdev.yummly2.R.array.recipeOrigin,
      R.layout.simple_spinner_item
    )

    listAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

    spinner.adapter = listAdapter

    saveBtn = addBinding.saveFoodBtn

    saveBtn.setOnClickListener {

      var foodImage_up: Uri? = foodImage
      if (foodImage_up == null) {
        Toast.makeText(
          applicationContext,
          "You haven\'t selected an image yet!",
          Toast.LENGTH_SHORT
        ).show()
      }

      var foodName_up = foodName.text.toString()
      if (foodName_up.trim().isEmpty()) {
        validateInput("name")
        return@setOnClickListener
      }

      var foodOrigin_up = spinner.selectedItem.toString()
      if(spinner.selectedItemPosition == 0) {
        validateInput("country of origin")
        return@setOnClickListener
      }

      var foodIngredients_up = foodIngredients.text.toString()
      if (foodIngredients_up.trim().isEmpty()) {
        validateInput("ingredients")
        return@setOnClickListener
      }

      var foodInstructions_up = foodInstructions.text.toString()
      if (foodInstructions_up.trim().isEmpty()) {
        validateInput("instructions")
        return@setOnClickListener
      }

      if (foodImage_up != null) {
        insertRecord(foodImage_up, foodName_up, foodOrigin_up, foodIngredients_up, foodInstructions_up)
      }
    }

    cancelBtn = addBinding.cancelAddBtn

    cancelBtn.setOnClickListener {
      Toast.makeText(applicationContext, "Returning to Home Page", Toast.LENGTH_SHORT).show()
      this.finish()
    }

  }

  override fun onStart() {
    super.onStart()
    val currentUser: FirebaseUser? = mAuth.currentUser
    if(currentUser == null) {
      startActivity(Intent(this, MainActivity::class.java))
      this.finish()
    }
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

  private fun insertRecord(image: Uri, name: String, origin: String, ingredients: String, instructions: String) {
    val sd = getFileName(applicationContext, image!!)
    val userId = mAuth.currentUser!!.uid

    val foodData_hashmap = HashMap<String, Any>()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dbRef = database.getReference("foods")

    val idKey = dbRef.push().key
    foodData_hashmap.put("a_idno", idKey!!)
    foodData_hashmap.put("b_authid", userId)
    foodData_hashmap.put("c_name", name)
    foodData_hashmap.put("d_origin", origin)
    foodData_hashmap.put("e_ingredients", ingredients)
    foodData_hashmap.put("f_instructions", instructions)
    if (sd != null) {
      foodData_hashmap.put("g_imageFileName", "file/${idKey}_${sd}")
    }


    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageRef = storage.reference

    val uploadTask = storageRef.child("file/${idKey}_${sd}").putFile(image)

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
    Toast.makeText(applicationContext, "Please enter the food's $info.", Toast.LENGTH_SHORT).show()
  }

}