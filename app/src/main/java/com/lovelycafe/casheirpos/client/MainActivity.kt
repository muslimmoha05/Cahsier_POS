package com.lovelycafe.casheirpos.client

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.admin.AdminMainActivity
import com.lovelycafe.casheirpos.authority.LoginActivity

class MainActivity : AppCompatActivity() {

    private var isTablet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the device is a tablet (sw600dp or larger)
        isTablet = resources.configuration.smallestScreenWidthDp >= 600

        // Set the appropriate layout based on the device
        setContentView(R.layout.activity_main)

        setupToolbar()
        checkUserAuthentication()

        if (isTablet) {
            setupTabletNavigation()
        } else {
            setupPhoneNavigation()
        }
    }

    /**
     * Sets up the toolbar as the action bar.
     */
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    /**
     * Checks the user authentication and redirects if necessary.
     */
    private fun checkUserAuthentication() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("LovelyCafePrefs", MODE_PRIVATE)
        val userType = sharedPreferences.getString("userType", null)

        when (userType) {
            null -> redirectToLogin()
            "" -> redirectToAdminMain()
        }
    }

    /**
     * Redirects the user to the login activity.
     */
    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /**
     * Redirects the user to the admin main activity.
     */
    private fun redirectToAdminMain() {
        startActivity(Intent(this, AdminMainActivity::class.java))
        finish()
    }

    /**
     * Sets up navigation for phones using BottomNavigationView.
     */
    private fun setupPhoneNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_menu, R.id.navigation_cart,
                R.id.navigation_bill, R.id.navigation_profile
            )
        )

        // Link the navigation controller with the toolbar and bottom navigation
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Handle re-selecting the menu item to refresh MenuFragment
        navView.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.navigation_menu -> {
                    navController.navigate(
                        R.id.navigation_menu,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.navigation_menu, true)
                            .build()
                    )
                }
            }
        }
    }

    /**
     * Sets up navigation for tablets using a persistent NavigationView.
     */
    private fun setupTabletNavigation() {
        // Get references to the drawer layout and navigation view
        val navView: NavigationView = findViewById(R.id.side_nav_view)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Create AppBarConfiguration with the drawer layout
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_menu, R.id.navigation_cart,
                R.id.navigation_bill, R.id.navigation_profile
            )
        )

        // Link the toolbar, navigation controller, and navigation drawer
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    /**
     * Handles the navigation up action for both layouts.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
