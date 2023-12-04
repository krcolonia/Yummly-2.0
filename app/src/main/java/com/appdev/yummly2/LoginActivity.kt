package com.appdev.yummly2

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.appdev.yummly2.databinding.ActivityLoginBinding

import android.content.Intent;
import android.util.Patterns
import android.view.KeyEvent
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

class LoginActivity : AppCompatActivity() {

  private lateinit var logBinding: ActivityLoginBinding
  private lateinit var mAuth: FirebaseAuth

  private lateinit var emailTxt: EditText
  private lateinit var passwordTxt: EditText

  private lateinit var progressDialog: ProgressDialog

  private lateinit var loginBtn: Button

  private lateinit var registerText: TextView
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    logBinding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(logBinding.root)

    mAuth = FirebaseAuth.getInstance()

    emailTxt = logBinding.emailLoginTxt
    passwordTxt = logBinding.passwordTxt

    loginBtn = logBinding.loginBtn
    loginBtn.setOnClickListener {
      if (!isFinishing) {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Logging in Account...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val email = emailTxt.text.toString()
        if(email.trim().isEmpty()) {
          validation("email address")
          return@setOnClickListener
        }
        if(!isValidEmail(email.trim())) {
          Toast.makeText(this, "Please enter a valid Email Address", Toast.LENGTH_SHORT).show()
          return@setOnClickListener
        }

        val pass = passwordTxt.text.toString()
        if(pass.trim().isEmpty()) {
          validation("password")
          return@setOnClickListener
        }

        mAuth.signInWithEmailAndPassword(email, pass)
          .addOnCompleteListener { task ->
            progressDialog.dismiss()
            if(task.isSuccessful) {
              Toast.makeText(applicationContext, "Logged In Successfully!", Toast.LENGTH_SHORT).show()
              val mainIntent = Intent(applicationContext, MainActivity::class.java)
              startActivity(mainIntent)
              finish()
            }
            else {
              Toast.makeText(applicationContext, "Authentication Failed", Toast.LENGTH_SHORT).show()
            }
          }
      }
    }

    registerText = logBinding.registerTxt
    registerText.setOnClickListener {
      val regIntent = Intent(applicationContext, RegisterActivity::class.java)
      startActivity(regIntent)
      finish()
    }

  }

  override fun onStart() {
    super.onStart()
    val currentUser: FirebaseUser? = mAuth.currentUser
    if(currentUser != null) {
      val mainIntent = Intent(applicationContext, MainActivity::class.java)
      startActivity(mainIntent)
      finish()
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?) : Boolean {
    if(keyCode == KeyEvent.KEYCODE_BACK) {
      startActivity(Intent(applicationContext, MainActivity::class.java))
      this.finish()
      return true
    }
    return super.onKeyDown(keyCode, event)
  }

  private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
  }

  private fun validation(info: String) {
    if (!isFinishing){
      progressDialog.dismiss()
      Toast.makeText(applicationContext, "Please enter your $info.", Toast.LENGTH_SHORT).show()
    }
  }
}