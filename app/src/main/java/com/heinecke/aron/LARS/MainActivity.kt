package com.heinecke.aron.LARS

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.zxing.integration.android.IntentIntegrator
import com.heinecke.aron.LARS.Utils.Companion.PREFS_APP
import com.heinecke.aron.LARS.Utils.Companion.PREFS_KEY_FIRST_RUN
import com.heinecke.aron.LARS.Utils.Companion.logResponseVerbose
import com.heinecke.aron.LARS.data.APIClient
import com.heinecke.aron.LARS.data.APIInterface
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.User
import com.heinecke.aron.LARS.data.model.UserData
import com.heinecke.aron.LARS.ui.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_scan_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mainViewModel: MainViewModel
    private val assetPattern: Regex = Regex("^http.*/([0-9]+)$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLogin()

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mainViewModel = ViewModelProviders.of(this)[MainViewModel::class.java]

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Scan Asset QR Code")
            integrator.setBeepEnabled(true)
            integrator.setBarcodeImageEnabled(false)
            integrator.initiateScan()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_scan, R.id.nav_slideshow,
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
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            mainViewModel.scanData.value = null
            Log.d(this::class.java.name,"Dest: $destination")
            //TODO: don't show scan icon always
        }

        val navHeader = findViewById<TextView>(R.id.nav_header)
        val navSubheader = findViewById<TextView>(R.id.nav_subheader)
        Log.d(this::class.java.name, "Initialized")

        mainViewModel.userData.observe(this, Observer {
            if (it != null){
                Log.d(this::class.java.name, "Has userdata: ${it.email}")
                navHeader.text = it.name
                navSubheader.text = it.email
            } else {
                Log.d(this::class.java.name, "No userdata")
                val data = mainViewModel.getLoginData(this)
                val client = APIClient.getClient(data.apiBackend,data.apiToken)
                val api = client.create(APIInterface::class.java)
                api.getUserInfo(data.userID).enqueue(object: Callback<User> {
                    override fun onFailure(call: Call<User>?, t: Throwable?) {
                        Log.w(this@MainActivity::class.java.name,"Unable to fetch user $t")
                        Toast.makeText(this@MainActivity,R.string.failure_userinfo,Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<User>?, response: Response<User>?) {
                        response?.body()?.run {
                            mainViewModel.userData.value = UserData(this.name,this.email)
                        } ?: logResponseVerbose(this@MainActivity::class.java,response).also {
                            Toast.makeText(this@MainActivity,R.string.failure_userinfo,Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                Log.d(this::class.java.name,"Scanned: ${result.contents}")
                assetPattern.find(result.contents,0)?.groupValues?.run {
                    this.forEach {item -> Log.d(this::class.java.name,"Item: $item")}
                    mainViewModel.scanData.value = Integer.valueOf(this[1])
                } ?: Toast.makeText(this,R.string.invalid_asset_code,Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
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

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
