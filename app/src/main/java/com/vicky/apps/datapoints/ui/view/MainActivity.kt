package com.vicky.apps.datapoints.ui.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.vicky.apps.datapoints.R
import com.vicky.apps.datapoints.base.AppConstants
import com.vicky.apps.datapoints.base.AppConstants.PERIODIC_INTERVAL
import com.vicky.apps.datapoints.base.AppConstants.PERMISION_REQUEST
import com.vicky.apps.datapoints.base.BaseActivity
import com.vicky.apps.datapoints.common.ViewModelProviderFactory
import com.vicky.apps.datapoints.ui.DownLoadSongManager
import com.vicky.apps.datapoints.ui.RunTimePermission
import com.vicky.apps.datapoints.ui.adapter.DataAdapter
import com.vicky.apps.datapoints.ui.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MainActivity : BaseActivity() {



    @Inject
    lateinit var factory: ViewModelProviderFactory

    private lateinit var viewModel:MainViewModel

    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: DataAdapter

    private var downloadId = 0

    /**
     * Runtime permissions object init to check storage persmissions
     */
    var runtimePermission: RunTimePermission = RunTimePermission(this)

    /**
     *  Workmanager global instance to enqueue tasks & get update
     */
    val workManager = WorkManager.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeValues()
        checkSavedValues()
        section1Subscription.setOnClickListener {
           subscribeSection1()
        }
        section1Subscription.setOnClickListener {
            subscribeSection2()
        }
        section1Subscription.setOnClickListener {
            subscribeSection3()
        }

    }



    private fun checkSavedValues(){

        val value = applicationContext.getSharedPreferences(AppConstants.NAME,0)
            .getInt(AppConstants.SHARED_PREF_SECTION, 0)
        if(value > 0){
            when (value){
                AppConstants.SECTION_1 -> { subscribeSection1()}
                AppConstants.SECTION_2 -> { subscribeSection2()}
                AppConstants.SECTION_3 -> { subscribeSection3()}
            }
        }
    }

    private fun subscribeSection1(){
        saveInSharedPref(AppConstants.SECTION_1)
        section1Subscription.setImageResource(R.drawable.ic_check_white_24dp)
        section2Subscription.setImageResource(R.drawable.ic_add_white_24dp)
        section3Subscription.setImageResource(R.drawable.ic_add_white_24dp)

        checkPermissionAndLaunch()
    }

    private fun subscribeSection2(){
        saveInSharedPref(AppConstants.SECTION_2)
        section2Subscription.setImageResource(R.drawable.ic_check_white_24dp)
        section1Subscription.setImageResource(R.drawable.ic_add_white_24dp)
        section3Subscription.setImageResource(R.drawable.ic_add_white_24dp)

        checkPermissionAndLaunch()
    }

    private fun subscribeSection3(){
        saveInSharedPref(AppConstants.SECTION_3)
        section3Subscription.setImageResource(R.drawable.ic_check_white_24dp)
        section1Subscription.setImageResource(R.drawable.ic_add_white_24dp)
        section2Subscription.setImageResource(R.drawable.ic_add_white_24dp)

        checkPermissionAndLaunch()
    }

    private fun saveInSharedPref(i: Int) {
        with (applicationContext.getSharedPreferences(AppConstants.NAME,0).edit()) {
            putInt(AppConstants.SHARED_PREF_SECTION, i)
            commit()
        }
    }

    private fun checkPermissionAndLaunch() {
        @RequiresApi(Build.VERSION_CODES.M)
        if(!checkSystemWritePermission()) {
            openAndroidPermissionsMenu()
            return
        }

        runtimePermission.requestPermission(listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            object : RunTimePermission.PermissionCallback {
                override fun onGranted() {

                    StartOneTimeWorkManager()
                }

                override fun onDenied() {
                    //show message if not allow storage permission
                }
            })
    }


    private fun initializeValues() {

        viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)

        viewModel.setCompositeData(compositeDisposable)

        viewModel.getSubscription().observe(this, Observer {
            if(it){
                successCallback()
            }else{
                failureCallback()
            }
        })
    }


    private fun successCallback(){
        updateData()
    }

    private fun updateData(){
        adapter.updateData()
    }


    private fun failureCallback(){
      //  Toast.makeText(this,"API failed",Toast.LENGTH_LONG).show()
    }


    private fun StartOneTimeWorkManager() {

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val task = OneTimeWorkRequest.Builder(DownLoadSongManager::class.java).setConstraints(constraints).build()
        workManager.enqueue(task)

        workManager.getWorkInfoByIdLiveData(task.id)
            .observe(this@MainActivity, Observer {
                it?.let {

                    if (it.state == WorkInfo.State.RUNNING) {


                    }else
                        if (it.state.isFinished) {

                           // Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_SHORT).show()

                        }
                }
            })
    }

    // Every periodic [PERIODIC_INTERVAL] interval work execute
    private fun StartPeriodicWorkManager() {

        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            DownLoadSongManager::class.java,
            PERIODIC_INTERVAL,
            TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        workManager.enqueue(periodicWorkRequest)


        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
            .observe(this@MainActivity, Observer {
                it?.let {
                    if (it.state == WorkInfo.State.ENQUEUED) {
                        //Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }


    /**
     * Request permission result pass to RuntimePermission.kt
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISION_REQUEST)
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




}
