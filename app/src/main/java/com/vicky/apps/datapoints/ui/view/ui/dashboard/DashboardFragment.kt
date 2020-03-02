package com.vicky.apps.datapoints.ui.view.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.*
import com.vicky.apps.datapoints.R
import com.vicky.apps.datapoints.base.AppConstants
import com.vicky.apps.datapoints.ui.DownLoadSongManager
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.util.concurrent.TimeUnit

class DashboardFragment : Fragment() {


    /**
     * Runtime permissions object init to check storage persmissions
     */


    /**
     *  Workmanager global instance to enqueue tasks & get update
     */
    val workManager = WorkManager.getInstance()

    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initiliaze()
    }

    private fun initiliaze() {
        checkSavedValues()
        section1Subscription.setOnClickListener {
            subscribeSection1()
        }
        section2Subscription.setOnClickListener {
            subscribeSection2()
        }
        section3Subscription.setOnClickListener {
            subscribeSection3()
        }
    }

    private fun checkSavedValues(){

        val value = activity?.applicationContext?.getSharedPreferences(AppConstants.NAME,0)
            ?.getInt(AppConstants.SHARED_PREF_SECTION, 0)
        if (value != null) {
            if(value > 0){
                when (value){
                    AppConstants.SECTION_1 -> { subscribeSection1()}
                    AppConstants.SECTION_2 -> { subscribeSection2()}
                    AppConstants.SECTION_3 -> { subscribeSection3()}
                }
            }
        }
    }

    private fun subscribeSection1(){
        saveInSharedPref(AppConstants.SECTION_1)
        section1Subscription.setImageResource(R.drawable.ic_check_white_24dp)
        section2Subscription.setImageResource(R.drawable.ic_add_white_24dp)
        section3Subscription.setImageResource(R.drawable.ic_add_white_24dp)

        StartOneTimeWorkManager()
    }

    private fun subscribeSection2(){
        saveInSharedPref(AppConstants.SECTION_2)
        section2Subscription.setImageResource(R.drawable.ic_check_white_24dp)
        section1Subscription.setImageResource(R.drawable.ic_add_white_24dp)
        section3Subscription.setImageResource(R.drawable.ic_add_white_24dp)

        StartOneTimeWorkManager()
    }

    private fun subscribeSection3(){
        saveInSharedPref(AppConstants.SECTION_3)
        section3Subscription.setImageResource(R.drawable.ic_check_white_24dp)
        section1Subscription.setImageResource(R.drawable.ic_add_white_24dp)
        section2Subscription.setImageResource(R.drawable.ic_add_white_24dp)

        StartOneTimeWorkManager()
    }

    private fun saveInSharedPref(i: Int) {
        with (activity?.applicationContext?.getSharedPreferences(AppConstants.NAME,0)?.edit()) {
            this?.putInt(AppConstants.SHARED_PREF_SECTION, i)
            this?.commit()
        }
    }

    private fun StartOneTimeWorkManager() {

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val task = OneTimeWorkRequest.Builder(DownLoadSongManager::class.java).setConstraints(constraints).build()
        workManager.enqueue(task)

        workManager.getWorkInfoByIdLiveData(task.id)
            .observe(this, Observer {
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
            AppConstants.PERIODIC_INTERVAL,
            TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        workManager.enqueue(periodicWorkRequest)


        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
            .observe(this, Observer {
                it?.let {
                    if (it.state == WorkInfo.State.ENQUEUED) {
                        //Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
}