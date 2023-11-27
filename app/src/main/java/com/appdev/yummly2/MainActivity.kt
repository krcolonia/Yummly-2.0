package com.appdev.yummly2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
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
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

  private lateinit var mainBinding: ActivityMainBinding
  private lateinit var drawerLayout: DrawerLayout
  private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
  private lateinit var navView: NavigationView

  private lateinit var addBtn: FloatingActionButton
  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: FoodAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mainBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(mainBinding.root)

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
      Pass the Open and Closwe toggle for the drawer layout listener
      to toggle the button
     */
    drawerLayout.addDrawerListener(actionBarDrawerToggle)
    actionBarDrawerToggle.syncState()

    /*
      To make the Nav drawer icon always appear on the action bar
     */
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    addBtn = mainBinding.floatingAddBtn

    addBtn.setOnClickListener {
      Toast.makeText(applicationContext, "Add new Recipe", Toast.LENGTH_SHORT).show()
      startActivity(Intent(applicationContext, AddFoodActivity::class.java))
    }

    navView.setNavigationItemSelectedListener {
      when(it.itemId) {
        R.id.nav_allRecipe -> {
          replaceFragment(FoodListFragment(), "all")
        }
        R.id.nav_filterRecipe -> {
          replaceFragment(FoodListFragment(), "filtered")
        }
        R.id.nav_userRecipe -> {
          replaceFragment(FoodListFragment(), "your")
        }
      }
      true
    }

  }

  private fun replaceFragment(fragment: Fragment, title: String) {
    val fragmentManager = supportFragmentManager
    val fragmentTransaction = fragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.AppFrameLayout, fragment)
    fragmentTransaction.commit()

    drawerLayout.closeDrawer(GravityCompat.START)
    Toast.makeText(applicationContext, "Showing $title Recipes", Toast.LENGTH_SHORT).show()
  }

  override fun onStart() {
    super.onStart()
//    adapter.startListening()
  }

  override fun onStop() {
    super.onStop()
//    adapter.stopListening()
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
}