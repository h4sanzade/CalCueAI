package com.hasanzade.calcueai

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)

            setupNavigation()
            setupWindowInsets()

            Log.d("MainActivity", "MainActivity created successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
            // Fallback basic setup
            setContentView(R.layout.activity_main)
        }
    }

    private fun setupNavigation() {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            if (navHostFragment != null) {
                navController = navHostFragment.navController
                Log.d("MainActivity", "Navigation setup successful")
            } else {
                Log.e("MainActivity", "NavHostFragment not found")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up navigation", e)
        }
    }

    private fun setupWindowInsets() {
        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up window insets", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            if (::navController.isInitialized) {
                navController.navigateUp() || super.onSupportNavigateUp()
            } else {
                super.onSupportNavigateUp()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onSupportNavigateUp", e)
            super.onSupportNavigateUp()
        }
    }
}