package com.vicky.apps.datapoints.ui.view.ui.home

import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.room.util.StringUtil
import androidx.work.*
import com.vicky.apps.datapoints.R
import com.vicky.apps.datapoints.base.AppConstants
import com.vicky.apps.datapoints.ui.DownLoadSongManager
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    val workManager = WorkManager.getInstance()

    private var contact1 = ""
    private var contact2 = ""
    private var contact3 = ""
    private var deviceName = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkValues()

        contact1Call.setOnClickListener {
            callClicked(1)
        }
        contact2Call.setOnClickListener {
            callClicked(2)
        }

        contact3Call.setOnClickListener {
            callClicked(3)
        }
    }

    private fun callClicked(i: Int) {
        when (i) {
            1 -> {

            }
            2 -> {

            }
            3 -> {

            }
        }
    }

    fun checkValues () {

        val path = Environment.getExternalStorageDirectory().absolutePath + "/Ringtones"

        val name = "songringtone4.mp3"

        val file = File(path, name)

        if(!file.exists()){
            StartOneTimeWorkManager(AppConstants.CALLERTUNE)
        }


        val con1 = readFromSavedData(AppConstants.CONTACT_1)
        val con2 = readFromSavedData(AppConstants.CONTACT_2)
        val con3 = readFromSavedData(AppConstants.CONTACT_3)
        val devNam = readFromSavedData(AppConstants.DEVICE_NAME)

        if(!TextUtils.isEmpty(con1) && !TextUtils.isEmpty(con2)
            && !TextUtils.isEmpty(con3) && !TextUtils.isEmpty(devNam)) {
            contact1 = con1
            contact2 = con2
            contact3 = con3
            deviceName = devNam

            contact1Name.text = contact1
            contact2Name.text = contact2
            contact3Name.text = contact3

            showContacts()
        } else {
            showEmptyPage()
        }
    }

    fun readFromSavedData(key: String) : String {
        return activity?.applicationContext?.getSharedPreferences(AppConstants.NAME,0)
            ?.getString(key, null) ?: ""
    }

    fun showContacts() {
        contactsParent.visibility = View.VISIBLE
        emptyParent.visibility = View.GONE
        callingParent.visibility = View.GONE
    }

    fun showEmptyPage() {

        contactsParent.visibility = View.GONE
        emptyParent.visibility = View.VISIBLE
        callingParent.visibility = View.GONE
    }

    fun showIncomingCall() {

        contactsParent.visibility = View.GONE
        emptyParent.visibility = View.GONE
        callingParent.visibility = View.VISIBLE
    }

    private fun StartOneTimeWorkManager(i : Int) {

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val task = OneTimeWorkRequest.Builder(DownLoadSongManager::class.java).setConstraints(constraints).
            setInputData(createInputData(i))
            .build()
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

    fun createInputData(i :Int): Data {
        return Data.Builder()
            .putInt(AppConstants.INPUT_KEY, i)
            .putBoolean(AppConstants.DOWNLOAD_REQUIRED, true)
            .build()
    }


}