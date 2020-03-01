package com.vicky.apps.datapoints.ui

import android.content.Context
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vicky.apps.datapoints.base.AppConstants.URLFILE
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL

class DownLoadSongManager(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

     private var liveDataHelper: LiveDataHelper? = null

    private val ringtoneSetter = RingtoneSetter()

    init {
        liveDataHelper = LiveDataHelper.instance
    }


    override fun doWork(): Result {
        try {
            val url = URL(URLFILE)
            val conection = url.openConnection()
            conection.connect()
            // getting file length

            // input stream to read file - with 8k buffer
            val input = BufferedInputStream(url.openStream(), 8192)

            // Output stream to write file
            val output = FileOutputStream(Environment.getExternalStorageDirectory().absolutePath+"/Ringtones"+"/songringtone.mp3")

            val data = ByteArray(1024)

            var count: Int? = 0

            while ({ count = input.read(data);count }() != -1) {
                output.write(data, 0, count!!)
            }



            // flushing output
            output.flush()

            // closing streams
            output.close()
            input.close()

            ringtoneSetter.setRingtone(applicationContext,
                Environment.getExternalStorageDirectory().absolutePath+"/Ringtones","songringtone.mp3")

        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }
}