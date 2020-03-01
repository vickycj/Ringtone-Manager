package com.vicky.apps.datapoints.ui

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class DownLoadSongManager(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

     private var liveDataHelper: LiveDataHelper? = null
        private var downloadFile: DownloadFile = DownloadFile()

    init {
        liveDataHelper = LiveDataHelper.instance
    }


    override fun doWork(): Result {
        downloadFile.DownloadData(applicationContext,
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            liveDataHelper)

        return Result.success()
    }
}