package com.lovelycafe.casheirpos.admin

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.Constants
import com.lovelycafe.casheirpos.authority.LoginActivity

class AdminMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("LovelyCafePrefs", MODE_PRIVATE)
        val userType = sharedPreferences.getString(Constants.USER_TYPE_KEY, null)

        if (userType != "admin") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_admin_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.admin_toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_admin_main) as NavHostFragment
        val navController = navHostFragment.navController

        val navView: BottomNavigationView = findViewById(R.id.admin_nav_view)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_orders, R.id.navigation_dashboard,
                R.id.navigation_bills, R.id.navigation_users
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_admin_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}