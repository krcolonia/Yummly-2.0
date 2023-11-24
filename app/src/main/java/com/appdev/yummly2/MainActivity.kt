package com.appdev.yummly2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.appdev.yummly2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var mainBinding: ActivityMainBinding
  private lateinit var drawerLayout: DrawerLayout
  private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

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

  override fun onBackPressed() {
    if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START)
    }
    else {
      super.onBackPressed()
    }
  }
}