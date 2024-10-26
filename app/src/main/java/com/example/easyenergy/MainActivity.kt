package com.example.easyenergy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.easyenergy.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       setSupportActionBar(binding.appBarMain.toolBar)


        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.mainFragment, R.id.dataReadFragment, R.id.settingsFragment))

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.app_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val navController = this.findNavController(R.id.nav_host_fragment_content_main)
        return when (item.itemId)
        {
            R.id.mainFragment -> {
                navController.navigate(R.id.mainFragment)
                true
            }
            R.id.infoFragment -> {
                navController.navigate(R.id.infoFragment)
                true
            }
            R.id.settingsFragment -> {
                navController.navigate(R.id.settingsFragment)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }


    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}