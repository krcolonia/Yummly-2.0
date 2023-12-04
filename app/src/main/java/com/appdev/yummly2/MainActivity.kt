package com.appdev.yummly2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.yummly2.databinding.ActivityMainBinding
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

  // Variable initialization
  private lateinit var mainBinding: ActivityMainBinding

  private lateinit var drawerLayout: DrawerLayout
  private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
  private lateinit var navView: NavigationView

  private lateinit var mAuth: FirebaseAuth
  private lateinit var navAcc: MenuItem
  private lateinit var navLog: MenuItem
  private lateinit var navReg: MenuItem
  private lateinit var navMyRec: MenuItem

  private lateinit var addBtn: FloatingActionButton
  private var currentFragment: Fragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mainBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(mainBinding.root)

    mAuth = FirebaseAuth.getInstance()

    /*
      Drawer layout instance to toggle the menu icon to open
      Drawer and back button to close drawer
     */
    drawerLayout = mainBinding.yummlyDrawer
    actionBarDrawerToggle = ActionBarDrawerToggle(
      this,
      drawerLayout,
      R.string.nav_open,
      R.string.nav_close
    )
    navView = mainBinding.navigationView

    /*
      Pass the Open and Close toggle for the drawer layout listener
      to toggle the button
     */
    drawerLayout.addDrawerListener(actionBarDrawerToggle)
    actionBarDrawerToggle.syncState()

    /*
      To make the Nav drawer icon always appear on the action bar
     */
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /*
      Initializes and adds function to the floatingActionButton
      that adds a new recipe
     */
    addBtn = mainBinding.floatingAddBtn
    addBtn.setOnClickListener {
      Toast.makeText(applicationContext, "Add new Recipe", Toast.LENGTH_SHORT).show()
      startActivity(Intent(applicationContext, AddFoodActivity::class.java))
    }

    // Initialization of account management settings in Nav Drawer
    navAcc = navView.menu.findItem(R.id.nav_acc)
    navLog = navView.menu.findItem(R.id.nav_log)
    navReg = navView.menu.findItem(R.id.nav_reg)
    navMyRec = navView.menu.findItem(R.id.nav_userRecipe)

    val currentUser: FirebaseUser? = mAuth.currentUser

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dbRef = database.getReference("users")

    if (currentUser != null) {
      val usernameRef = dbRef.child(mAuth.currentUser!!.uid).child("username")

      usernameRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          val username = dataSnapshot.value.toString()

          // Update UI with the retrieved username
          navAcc.title = "Hello, $username!"
          navLog.title = "Logout"
          navReg.isVisible = false
          navMyRec.isVisible = true
          addBtn.visibility = View.VISIBLE
        }

        override fun onCancelled(databaseError: DatabaseError) {
          // Handle error
        }
      })
    }
    else {
      navAcc.title = "You're logged out."
      navLog.title = "Login"
      navReg.isVisible = true
      navMyRec.isVisible = false
      addBtn.visibility = View.GONE
    }

    navView.setNavigationItemSelectedListener {
      when(it.itemId) {
        R.id.nav_log -> {
          val currentUser: FirebaseUser? = mAuth.currentUser
          if(currentUser != null) {
            mAuth.signOut()
            startActivity(Intent(applicationContext, MainActivity::class.java))
            this.finish()
          }
          else {
            val loginIntent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(loginIntent)
            this.finish()
          }
        }
        R.id.nav_reg -> {
          val regIntent = Intent(applicationContext, RegisterActivity::class.java)
          startActivity(regIntent)
          this.finish()
        }
        R.id.nav_allRecipe -> {
          Toast.makeText(this, "Showing all recipes...", Toast.LENGTH_SHORT).show()
          replaceFragment(FoodListFragment())
          if(currentUser != null) {
            addBtn.visibility = View.VISIBLE
          }
          else {
            addBtn.visibility = View.GONE
          }
        }
        R.id.nav_userRecipe -> {
          Toast.makeText(this, "Showing your recipes...", Toast.LENGTH_SHORT).show()
          userRecipeFragment()
          if(currentUser != null) {
            addBtn.visibility = View.VISIBLE
          }
          else {
            addBtn.visibility = View.GONE
          }
        }
        R.id.nav_help -> {
          replaceFragment(HelpFragment())
          addBtn.visibility = View.GONE
        }
        R.id.nav_about -> {
          replaceFragment(AboutUsFragment())
          addBtn.visibility = View.GONE
        }
      }
      true
    }

    if(savedInstanceState == null) {
      loadDefaultFragment() // Launches the default fragment, which is the Home page
    }
  }


  override fun onCreateOptionsMenu(menu: Menu?) : Boolean {
    menuInflater.inflate(R.menu.search, menu)
    val item = menu?.findItem(R.id.search)
    val searchView = item?.actionView as SearchView

    searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String?): Boolean {
        (currentFragment as? FoodListFragment)?.searchFood(query)
        return false
      }

      override fun onQueryTextChange(query: String?): Boolean {
        (currentFragment as? FoodListFragment)?.searchFood(query)
        return false
      }
    })
    return super.onCreateOptionsMenu(menu)
  }

  /*
    Override the onOptionsItemSelected() function to implement the
    item click listener callback to open and close the nav drawer
    when the icon is clicked.
   */
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
      true
    }
    else super.onOptionsItemSelected(item)
  }

  /*
    Overrides the back button in android devices so that if the
    navigation drawer is opened, pressing back closes the drawer
    instead of the app.
   */
  override fun onBackPressed() {
    if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START)
    }
    else {
      super.onBackPressed()
    }
  }

  // method used upon pressing of an option in the nav drawer, replaces current fragment
  private fun replaceFragment(fragment: Fragment) {
    if (!isFinishing && !isDestroyed) {
      currentFragment = fragment
      supportFragmentManager.beginTransaction()
        .replace(R.id.AppFrameLayout, fragment)
        .commit()

      drawerLayout.closeDrawer(GravityCompat.START)
    }
  }

  // Loads the default fragment upon app launch
  private fun loadDefaultFragment() {
    if(!isFinishing && !isDestroyed) {
      val homeFragment = FoodListFragment()
      currentFragment = homeFragment
      supportFragmentManager.beginTransaction()
        .replace(R.id.AppFrameLayout, homeFragment)
        .commit()
    }
  }

  // method used to filter user recipes so that it only shows recipes made in the logged in account
  private fun userRecipeFragment() {
    if(!isFinishing && !isDestroyed) {
      val currentFragment = supportFragmentManager.findFragmentById(R.id.AppFrameLayout)

      if(currentFragment is FoodListFragment) {
        currentFragment.filterUserRecipe(mAuth.currentUser!!.uid)
      }

      drawerLayout.closeDrawer(GravityCompat.START)
    }
  }
}