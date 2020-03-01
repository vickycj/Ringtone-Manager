package com.vicky.apps.datapoints.ui.view

import android.Manifest
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.vicky.apps.datapoints.base.AppConstants.PERIODIC_INTERVAL
import com.vicky.apps.datapoints.base.AppConstants.PERMISION_REQUEST
import com.vicky.apps.datapoints.base.AppConstants.URLFILE
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
        setContentView(com.vicky.apps.datapoints.R.layout.activity_main)
        initializeValues()
        progressBar.hide()

        urlText.setText(URLFILE)

       setRingtone.setOnClickListener {
           checkPermissionAndLaunch()
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
        Toast.makeText(this,"API failed",Toast.LENGTH_LONG).show()
    }


    private fun StartOneTimeWorkManager() {

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val task = OneTimeWorkRequest.Builder(DownLoadSongManager::class.java).setConstraints(constraints).build()
        workManager.enqueue(task)

        workManager.getWorkInfoByIdLiveData(task.id)
            .observe(this@MainActivity, Observer {
                it?.let {

                    if (it.state == WorkInfo.State.RUNNING) {
                        loaderShow(true)

                    }else
                        if (it.state.isFinished) {

                            Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_SHORT).show()
                            loaderShow(false)
                        }
                }
            })
    }

    // Every periodic [PERIODIC_INTERVAL] interval work execute
    private fun StartPeriodicWorkManager() {
        loaderShow(true)
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

                        loaderShow(false)
                        Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    /**
     * Loader visibility
     */
    private fun loaderShow(flag: Boolean) {
        when (flag) {
            true ->{ progressBar.show()
                titleText.text = "Downloading File"
            }
            false ->{ progressBar.hide()
                titleText.text = "Ringtone Set"
            }
        }
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
                Toast.makeText(this, "Write not allowed :-)", Toast.LENGTH_LONG).show()
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
