package com.vicky.apps.datapoints.ui.view.ui.home

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.*
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.Sinch
import com.sinch.android.rtc.SinchClient
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallClient
import com.sinch.android.rtc.calling.CallClientListener
import com.sinch.android.rtc.calling.CallListener
import com.vicky.apps.datapoints.R
import com.vicky.apps.datapoints.base.AppConstants
import com.vicky.apps.datapoints.ui.DownLoadSongManager
import kotlinx.android.synthetic.main.fragment_home.*
import org.webrtc.ContextUtils.getApplicationContext
import java.io.File


class HomeFragment : Fragment() {

    private val APP_KEY = "d18fb872-e664-4cf6-98c5-7c784b4510cf"
    private val APP_SECRET = "FtApuOdEZ0C9VVWfcel/4g=="
    private val ENVIRONMENT = "sandbox.sinch.com"

    private var call: Call? = null
    private var sinchClient: SinchClient? = null


    private lateinit var homeViewModel: HomeViewModel
    val workManager = WorkManager.getInstance()

    private var contact1 = ""
    private var contact2 = ""
    private var contact3 = ""
    private var deviceName = ""
    var mp = MediaPlayer()

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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

        answerButton.setOnClickListener {
            answerButtonClicked()
        }

        rejectButton.setOnClickListener {
            rejectButtonClicked()
        }
    }

    private fun rejectButtonClicked() {
        call?.hangup()
        call = null
        mp.stop()
        mp.reset()
        showContacts()

    }

    private fun answerButtonClicked() {
        call?.answer()
        mp.stop()
        mp.reset()
    }

    private fun callClicked(i: Int) {
        when (i) {
            1 -> {
                if (call == null) {
                    call = sinchClient?.callClient?.callUser(contact1)
                    call?.addCallListener(SinchCallListener())
                    callerText.text = "Calling $contact1"
                }

            }
            2 -> {
                if (call == null) {
                    call = sinchClient?.callClient?.callUser(contact2)
                    call?.addCallListener(SinchCallListener())
                    callerText.text = "Calling $contact2"
                }
            }
            3 -> {
                if (call == null) {
                    call = sinchClient?.callClient?.callUser(contact3)
                     call?.addCallListener(SinchCallListener())
                    callerText.text = "Calling $contact3"
            }
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

            try {
                initializeSinch()
                showContacts()
            }catch (e: java.lang.Exception) {
                Log.e("Exception","dsds")
            }

        } else {
            showEmptyPage()
        }
    }

    private fun initializeSinch() {

        sinchClient = Sinch.getSinchClientBuilder()
            .context(activity)
            .userId(deviceName)
            .applicationKey(APP_KEY)
            .applicationSecret(APP_SECRET)
            .environmentHost(ENVIRONMENT)
            .build()

        sinchClient?.setSupportCalling(true)
        sinchClient?.startListeningOnActiveConnection()
        sinchClient?.start()

        sinchClient?.callClient?.addCallClientListener(SinchCallClientListener())
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

    private inner class SinchCallListener : CallListener {



        override fun onCallEnded(endedCall: Call) {
            call = null
            mp.stop()
            mp.reset()
            val a = endedCall.details.error
            showContacts()
            activity?.volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
        }

        override fun onCallEstablished(establishedCall: Call) {
            callerText.text = "Connected"
            activity?.volumeControlStream = AudioManager.STREAM_VOICE_CALL
        }

        override fun onCallProgressing(progressingCall: Call) {
            showIncomingCall()
            playAudio(AudioManager.STREAM_VOICE_CALL)

        }

        override fun onShouldSendPushNotification(
            call: Call,
            pushPairs: List<PushPair>
        ) {
        }
    }

    fun playAudio (type: Int) {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/Ringtones"
        var name = "songringtone4.mp3"
        val subscription = readSubsription()
        if (subscription != 0) {
            name = "songringtone$subscription.mp3"
        }
        audioPlayer(path,name, type)
    }

    private fun readSubsription(): Int? {
      return activity?.applicationContext?.getSharedPreferences(AppConstants.NAME,0)
          ?.getInt(AppConstants.SHARED_PREF_SECTION, 0)
    }

    private inner class SinchCallClientListener :
        CallClientListener {
        override fun onIncomingCall(
            callClient: CallClient,
            incomingCall: Call
        ) {
            showIncomingCall()
            playAudio(AudioManager.STREAM_MUSIC)
            callerText.text = "Incoming call"
            call = incomingCall
            call?.addCallListener(SinchCallListener())
            Toast.makeText(activity, "incoming call", Toast.LENGTH_SHORT).show()
        }
    }

    fun playRingtone() {
        try {

            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r: Ringtone = RingtoneManager.getRingtone(activity?.applicationContext, notification)
            r.play()
        }catch (e : Exception) {
            Log.e("ex","exce")
        }


    }
    fun audioPlayer(path: String, fileName: String, type: Int) {
        //set up MediaPlayer
        mp = MediaPlayer()
        try {
            mp.setAudioStreamType(type)
            mp.setDataSource(path + File.separator + fileName)
            mp.setOnCompletionListener { // TODO Auto-generated method stub

            }
            mp.prepare()
            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}