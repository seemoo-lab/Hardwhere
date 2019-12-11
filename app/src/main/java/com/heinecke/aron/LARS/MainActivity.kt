package com.heinecke.aron.LARS

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.heinecke.aron.LARS.Utils.Companion.PREFS_APP
import com.heinecke.aron.LARS.Utils.Companion.PREFS_KEY_FIRST_RUN
import com.heinecke.aron.LARS.ui.login.LoginActivity


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLogin()

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send, R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_logout) {
                Log.d(this@MainActivity::class.java.name, "logout")
                val prefs = getSharedPreferences(PREFS_APP, 0).edit()
                prefs.putBoolean(PREFS_KEY_FIRST_RUN,true)
                prefs.apply()
                showLogin()
                true
            } else {
                val handled = NavigationUI.onNavDestinationSelected(it, navController)

                // This is usually done by the default ItemSelectedListener.
                // But there can only be one! Unfortunately.
                if (handled) drawerLayout.closeDrawer(navView)

                // return the result of NavigationUI call
                handled
            }
        }
    }

    private fun checkLogin() {
        val prefs = getSharedPreferences(PREFS_APP, 0)
        val firstRun = prefs.getBoolean(PREFS_KEY_FIRST_RUN, true)
        if(firstRun){
            showLogin()
        }
    }

    private fun showLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
