package com.vicky.apps.datapoints.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import com.vicky.apps.datapoints.base.AppConstants
import java.lang.Appendable
import java.util.*


class DownloadFile {

     fun DownloadData(context: Context, path: String, liveDataHelper: LiveDataHelper?) : String {
         val DownloadUrl: String = path
         val request1 = DownloadManager.Request(Uri.parse(DownloadUrl))
         request1.setDescription("Sample Music File") //appears the same in Notification bar while downloading

         request1.setTitle("File1.mp3")
         request1.setVisibleInDownloadsUi(false)


         request1.allowScanningByMediaScanner()
         request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)

         request1.setDestinationInExternalFilesDir(
             context.applicationContext,
             "/Ringtonefile",
             "song.mp3"
         )

         val manager1 =
             context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
         Objects.requireNonNull(manager1)?.enqueue(request1)
         return AppConstants.SUCCESS
     }
}