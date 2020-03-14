package com.vicky.apps.datapoints.ui.view

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.vicky.apps.datapoints.R
import com.vicky.apps.datapoints.base.AppConstants
import com.vicky.apps.datapoints.ui.RunTimePermission
import com.vicky.apps.datapoints.ui.SettingsActivity

class DashboardActivity : AppCompatActivity() {

    var runtimePermission: RunTimePermission = RunTimePermission(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }

    override fun onResume() {
        super.onResume()
        filePermission()
    }

    private fun filePermission(){
        runtimePermission.requestPermission(listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            object : RunTimePermission.PermissionCallback {
                override fun onGranted() {
                    checkPermissionAndLaunch()
                }

                override fun onDenied() {
                    finish()
                }
            })
    }

    private fun checkPermissionAndLaunch() {
        @RequiresApi(Build.VERSION_CODES.M)
        if(!checkSystemWritePermission()) {
            openAndroidPermissionsMenu()
            return
        }
    }

    /**
     * Request permission result pass to RuntimePermission.kt
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == AppConstants.PERMISION_REQUEST)
            runtimePermission.onRequestPermissionsResult(grantResults)

    }

    private fun checkSystemWritePermission(): Boolean {
        var retVal = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this)
            Log.d("name", "Can Write Settings: $retVal")
            if (retVal) {
                // Toast.makeText(this, "Write allowed :-)", Toast.LENGTH_LONG).show()
            } else {
                // Toast.makeText(this, "Write not allowed :-)", Toast.LENGTH_LONG).show()
            }
        }
        return retVal
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.home_menu, menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return true
    }

}
