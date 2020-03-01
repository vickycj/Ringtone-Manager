package com.vicky.apps.datapoints.ui

import android.content.ContentValues
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import java.io.File


class RingtoneSetter {

    fun setRingtone (context: Context, path: String, name: String) {

        try {


        val k = File(path, name) // path is a file to /sdcard/media/ringtone


        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DATA, k.absolutePath)
        values.put(MediaStore.MediaColumns.TITLE, k.name)
        values.put(MediaStore.MediaColumns.SIZE, k.length())
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
        values.put(MediaStore.Audio.Media.ARTIST, "Madonna")
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
        values.put(MediaStore.Audio.Media.IS_ALARM, false)
        values.put(MediaStore.Audio.Media.IS_MUSIC, true)


        val uri: Uri = MediaStore.Audio.Media.getContentUriForPath(k.absolutePath)


        val newUri = context.contentResolver.insert(uri, values)

        RingtoneManager.setActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_RINGTONE,
            newUri
        )

        }catch (e: Exception) {
            Toast.makeText(context,"Song ringtone Failed",Toast.LENGTH_LONG).show()
        }

    }

}