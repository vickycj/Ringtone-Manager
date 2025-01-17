package com.vicky.apps.datapoints.ui

import android.content.Context
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vicky.apps.datapoints.base.AppConstants
import com.vicky.apps.datapoints.base.AppConstants.URLFILE
import java.io.BufferedInputStream
import java.io.File
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

            val inputVal = inputData.getInt(AppConstants.INPUT_KEY, 0)

            val downloadOnly = inputData.getBoolean(AppConstants.DOWNLOAD_REQUIRED, false)

            val path = Environment.getExternalStorageDirectory().absolutePath + "/Ringtones"

            val name = "songringtone$inputVal.mp3"

            val file = File(path, name)

            if(file.exists()){
                file.delete()
            }

            var url = URL(URLFILE)
            if(inputVal > 0){
                when (inputVal){
                    AppConstants.SECTION_1 -> { url = URL(AppConstants.SUBSCRIBTION_1_URL)}
                    AppConstants.SECTION_2 -> { url = URL(AppConstants.SUBSCRIBTION_2_URL)}
                    AppConstants.SECTION_3 -> { url = URL(AppConstants.SUBSCRIBTION_3_URL)}
                    AppConstants.CALLERTUNE -> { url = URL(AppConstants.CALLER_TUNE_URL)}
                }
            }



            val conection = url.openConnection()
            conection.connect()
            // getting file length

            // input stream to read file - with 8k buffer
            val input = BufferedInputStream(url.openStream(), 8192)

            // Output stream to write file
            val output = FileOutputStream("$path/$name")

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

            if(!downloadOnly) {
                ringtoneSetter.setRingtone(applicationContext,
                    path,name)
            }


        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }
}