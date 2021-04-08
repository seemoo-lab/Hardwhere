package de.tu_darmstadt.seemoo.LARS

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import de.tu_darmstadt.seemoo.LARS.Utils.Companion.PREFS_APP
import de.tu_darmstadt.seemoo.LARS.Utils.Companion.PREFS_KEY_BACKEND
import de.tu_darmstadt.seemoo.LARS.Utils.Companion.PREFS_KEY_FIRST_RUN
import de.tu_darmstadt.seemoo.LARS.Utils.Companion.PREFS_KEY_TOKEN
import de.tu_darmstadt.seemoo.LARS.Utils.Companion.PREFS_KEY_UID
import de.tu_darmstadt.seemoo.LARS.Utils.Companion.logResponseVerbose
import de.tu_darmstadt.seemoo.LARS.data.APIClient
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable.User
import de.tu_darmstadt.seemoo.LARS.data.model.UserData
import de.tu_darmstadt.seemoo.LARS.ui.login.LoginActivity
import io.sentry.core.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mainViewModel: MainViewModel
    private val assetPattern: Regex = Regex("^http.*/([0-9]+)$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sentry.setTag("commit", BuildConfig.GitHash)
        Sentry.setTag("buildtype", BuildConfig.BUILD_TYPE)

        checkLogin()

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        savedInstanceState?.run {
            mainViewModel.userData.value = this.getParcelable(S_USERDATA)
            mainViewModel.loginData.value = this.getParcelable(S_LOGINDATA)
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_own, R.id.nav_scan,R.id.nav_about,R.id.nav_logout, R.id.nav_dev
//                R.id.nav_slideshow, R.id.nav_tools, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_logout) {
                Log.d(this@MainActivity::class.java.name, "logout")
                resetLogin()
                showLogin()
                true
            } else if (it.itemId == R.id.nav_dev) {
                Log.d(this@MainActivity::class.java.name, "play beep")
                Utils.playErrorBeep()
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

        Log.d(this::class.java.name, "Initialized")

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val navTitle: TextView = headerView.findViewById(R.id.nav_header)
        val navSubtitle: TextView = headerView.findViewById(R.id.nav_subheader)


        mainViewModel.userData.observe(this, Observer {
            if (it != null) {
                Log.d(this::class.java.name, "Has userdata: ${it.email}")
                navTitle.text = it.name
                navSubtitle.text = it.email
            } else {
                Log.d(this::class.java.name, "No userdata")
                val data = mainViewModel.getLoginData(this)
                if (data == null) {
                    showLogin()
                } else {
                    val client = APIClient.getClient(data.apiBackend, data.apiToken)
                    val api = client.create(APIInterface::class.java)
                    api.getUserInfo(data.userID).enqueue(object : Callback<User> {
                        override fun onFailure(call: Call<User>?, t: Throwable?) {
                            Log.w(this@MainActivity::class.java.name, "Unable to fetch user $t")
                            Toast.makeText(
                                this@MainActivity,
                                R.string.failure_userinfo,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onResponse(call: Call<User>?, response: Response<User>?) {
                            response?.body()?.run {
                                mainViewModel.userData.value = UserData(this.name, this.email)
                            } ?: logResponseVerbose(this@MainActivity::class.java, response).also {
                                Toast.makeText(
                                    this@MainActivity,
                                    R.string.failure_userinfo,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.run {
            putParcelable(S_LOGINDATA, mainViewModel.loginData.value)
            putParcelable(S_USERDATA, mainViewModel.userData.value)
        }
    }

    private fun checkLogin() {
        val prefs = getSharedPreferences(PREFS_APP, 0)
        val firstRun = prefs.getBoolean(PREFS_KEY_FIRST_RUN, true)
        if (firstRun) {
            showLogin()
        }
    }

    private fun resetLogin() {
        val prefs = getSharedPreferences(PREFS_APP, 0).edit()
        prefs.putBoolean(PREFS_KEY_FIRST_RUN, true)
        prefs.remove(PREFS_KEY_BACKEND)
        prefs.remove(PREFS_KEY_TOKEN)
        prefs.remove(PREFS_KEY_UID)
        prefs.apply()
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

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        const val S_USERDATA: String = "userdata"
        const val S_LOGINDATA: String = "logindata"
    }
}
