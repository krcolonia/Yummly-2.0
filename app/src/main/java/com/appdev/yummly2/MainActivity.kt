package com.appdev.yummly2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

  // Variable initialization
  private lateinit var mainBinding: ActivityMainBinding
  private lateinit var drawerLayout: DrawerLayout
  private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
  private lateinit var navView: NavigationView

  private lateinit var addBtn: FloatingActionButton
  private var currentFragment: Fragment? = null

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

    if(savedInstanceState == null) {
      loadDefaultFragment() // Launches the default fragment, which is the Home page
    }



    navView.setNavigationItemSelectedListener {
      when(it.itemId) {
        R.id.nav_allRecipe,
        R.id.nav_filterRecipe,
        R.id.nav_userRecipe -> {
          replaceFragment(FoodListFragment())
          addBtn.visibility = View.VISIBLE
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

  }
  

  override fun onCreateOptionsMenu(menu: Menu?) : Boolean {
    menuInflater.inflate(R.menu.search, menu)
    var item = menu?.findItem(R.id.search)
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

  private fun replaceFragment(fragment: Fragment) {
    currentFragment = fragment
    supportFragmentManager.beginTransaction()
      .replace(R.id.AppFrameLayout, fragment)
      .commit()

    drawerLayout.closeDrawer(GravityCompat.START)
  }

  private fun loadDefaultFragment() {
    val homeFragment = FoodListFragment() // could change, maybe make home fragment??
    currentFragment = homeFragment
    supportFragmentManager.beginTransaction()
      .replace(R.id.AppFrameLayout, homeFragment)
      .commit()
  }
}