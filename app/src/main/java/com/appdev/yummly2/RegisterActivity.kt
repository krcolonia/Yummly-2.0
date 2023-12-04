package com.appdev.yummly2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.appdev.yummly2.databinding.ActivityRegisterBinding

import android.app.ProgressDialog
import android.content.Intent;
import android.util.Patterns
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.Button;
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.DialogPlusBuilder
import com.orhanobut.dialogplus.ViewHolder

class RegisterActivity : AppCompatActivity() {

  private lateinit var regBinding: ActivityRegisterBinding
  private lateinit var mAuth: FirebaseAuth

  private lateinit var usernameTxt: EditText
  private lateinit var emailTxt: EditText
  private lateinit var passwordTxt: EditText
  private lateinit var confirmPassTxt: EditText

  private lateinit var progressDialog: ProgressDialog

  private lateinit var privacyCheck: CheckBox
  private lateinit var tosText: TextView
  private lateinit var privPolText: TextView
  private var currentDialog: DialogPlus? = null

  private lateinit var registerBtn: Button

  private lateinit var loginText: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    regBinding = ActivityRegisterBinding.inflate(layoutInflater)
    setContentView(regBinding.root)

    mAuth = FirebaseAuth.getInstance()

    usernameTxt = regBinding.usernameRegisterTxt
    emailTxt = regBinding.emailRegisterTxt
    passwordTxt = regBinding.passwordRegisterTxt
    confirmPassTxt = regBinding.confirmPasswordRegisterTxt

    privacyCheck = regBinding.policyCheck

    tosText = regBinding.termsTxt
    tosText.setOnClickListener {
      val termsView = layoutInflater.inflate(R.layout.terms_of_service, null)
      val termsContent = termsView.findViewById<TextView>(R.id.tosContent)

      termsContent.text = "\t\tWelcome to Yummly, the Recipe App for culinary enthusiasts! By accessing or using our app, you agree to comply with and be bound by the following Terms of Service. Please read these terms carefully before using Yummly.\n" +
          "\n" +
          "        1. Acceptance of Terms: By using Yummly, you agree to these Terms of Service. If you do not agree, please do not use the app.\n" +
          "\n" +
          "        2. Use of the App: Yummly is intended for personal, non-commercial use. You may not use it for any illegal or unauthorized purpose. You agree to comply with all applicable laws.\n" +
          "\n" +
          "        3. User Accounts: To access certain features, you may need to create a user account. You are responsible for maintaining the confidentiality of your account information.\n" +
          "\n" +
          "        4. Content: Yummly allows users to share and customize recipes. You are solely responsible for the content you post. We reserve the right to remove any content that violates these terms.\n" +
          "\n" +
          "        5. Intellectual Property: All content and materials on Yummly are protected by intellectual property laws. You may not use, reproduce, or distribute any content without permission.\n" +
          "\n" +
          "        6. Privacy: Please review our Privacy Policy to understand how we collect, use, and protect your personal information.\n" +
          "\n" +
          "        7. Modification of Terms: We reserve the right to update or modify these terms at any time. Your continued use of Yummly after changes constitutes acceptance of the modified terms.\n" +
          "\n" +
          "        8. Termination: We may terminate or suspend your account if you violate these terms. You may also terminate your account at any time.\n" +
          "\n" +
          "        9. Disclaimer: Yummly is provided \"as is\" without any warranties. We are not responsible for the accuracy or reliability of content posted by users.\n" +
          "\n" +
          "        10. Contact Us: If you have any questions about these terms, please contact us at [contact.yummly@kurtic.com]."

      val termsDialog = DialogPlus.newDialog(this)
        .setContentHolder(ViewHolder(termsView))
        .setExpanded(false)
        .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
        .setContentHeight(800)
        .create()

      termsDialog.show()

      currentDialog = termsDialog
    }

    privPolText = regBinding.privacyTxt
    privPolText.setOnClickListener {
      val privacyView = layoutInflater.inflate(R.layout.privacy_policy, null)
      val privacyContent = privacyView.findViewById<TextView>(R.id.privPolContent)

      privacyContent.text = "\t\tWelcome to Yummly, the Recipe App committed to your privacy! This Privacy Policy explains how we collect, use, and protect your personal information.\n" +
          "\n" +
          "        1. Information We Collect:\n" +
          "- User Account Information: When you create an account, we collect your username and email address.\n" +
          "- Usage Data: We collect information about how you use Yummly, such as recipes viewed and customized.\n" +
          "\n" +
          "        2. How We Use Your Information:\n" +
          "- Personalization: We use your information to personalize your Yummly experience, including recipe recommendations.\n" +
          "- Communication: We may contact you with app updates, newsletters, or important notices.\n" +
          "\n" +
          "        3. Sharing of Information:\n" +
          "- Public Content: Content you share on Yummly may be visible to other users.\n" +
          "- Third-Party Services: We may share non-personal information with third-party service providers.\n" +
          "\n" +
          "        4. Your Choices:\n" +
          "- Account Settings: You can customize your privacy settings within the app.\n" +
          "- Communications: You can opt-out of promotional emails.\n" +
          "\n" +
          "        5. Security:\n" +
          "- We use industry-standard measures to protect your information, but no method is 100% secure.\n" +
          "\n" +
          "        6. Children’s Privacy:\n" +
          "- Yummly is not intended for children under 13. We do not knowingly collect information from children.\n" +
          "\n" +
          "        7. Changes to the Privacy Policy:\n" +
          "- We may update this Privacy Policy. Check the effective date for the latest version.\n" +
          "\n" +
          "        8. Contact Us:\n" +
          "- If you have questions or concerns, contact us at [contact.yummly@kurtic.com].\n" +
          "\n" +
          "By using Yummly, you agree to this Privacy Policy."

      val privacyDialog = DialogPlus.newDialog(this)
        .setContentHolder(ViewHolder(privacyView))
        .setExpanded(false)
        .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
        .setContentHeight(800)
        .create()

      privacyDialog.show()

      currentDialog = privacyDialog
    }

    registerBtn = regBinding.registerBtn
    registerBtn.setOnClickListener {
      if (!isFinishing) {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creating Account...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val username = usernameTxt.text.toString()
        if (username.trim().isEmpty()) {
          validation("username")
          return@setOnClickListener
        }
        val email = emailTxt.text.toString()
        if (email.trim().isEmpty()) {
          validation("email address")
          return@setOnClickListener
        }
        if(!isValidEmail(email.trim())) {
          Toast.makeText(this, "Please enter a valid Email Address", Toast.LENGTH_SHORT).show()
          return@setOnClickListener
        }

        val pass = passwordTxt.text.toString()
        if (pass.trim().isEmpty()) {
          validation("password")
          return@setOnClickListener
        }

        val confirmPass = confirmPassTxt.text.toString()
        if (confirmPass.trim().isEmpty()) {
          progressDialog.dismiss()
          Toast.makeText(this, "Please confirm your password.", Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }

        if (pass != confirmPass) {
          progressDialog.dismiss()
          Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
          return@setOnClickListener
        }

        if (!privacyCheck.isChecked) {
          progressDialog.dismiss()
          Toast.makeText(
            applicationContext,
            "Please accept our terms of service and privacy policy before continuing.",
            Toast.LENGTH_LONG
          ).show()
          return@setOnClickListener
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
          .addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
              val userHashmap = HashMap<String, Any>()
              val database: FirebaseDatabase = FirebaseDatabase.getInstance()
              val dbRef = database.getReference("users")

              userHashmap.put("username", username)

              dbRef.child(mAuth.currentUser!!.uid).setValue(userHashmap).addOnCompleteListener {
                Toast.makeText(
                  this,
                  "Account Created Successfully!",
                  Toast.LENGTH_SHORT
                )
                  .show()
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                finish()
              }
            } else {
              Toast.makeText(this, "Register Failed!", Toast.LENGTH_SHORT).show()
            }
          }
      }
    }

    loginText = regBinding.loginTxt
    loginText.setOnClickListener {
      val loginIntent = Intent(this, LoginActivity::class.java)
      startActivity(loginIntent)
      finish()
    }
  }

  override fun onStart() {
    super.onStart()
    val currentUser: FirebaseUser? = mAuth.currentUser
    if (currentUser != null) {
      val mainIntent = Intent(this, MainActivity::class.java)
      startActivity(mainIntent)
      finish()
    }
  }

  // Override onBackPressed
  override fun onBackPressed() {
    if (currentDialog != null && currentDialog!!.isShowing) {
      // Dismiss the dialog if it is showing
      currentDialog!!.dismiss()
      currentDialog = null
    } else {
      startActivity(Intent(applicationContext, MainActivity::class.java))
      this.finish()
    }
  }

  private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
  }

  private fun validation(info: String) {
    if (!isFinishing) {
      progressDialog.dismiss()
      Toast.makeText(this, "Please enter your $info.", Toast.LENGTH_SHORT).show()
    }
  }
}