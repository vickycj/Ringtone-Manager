package com.vicky.apps.datapoints.ui

import android.content.ContentValues
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import java.io.File


class RingtoneSetter {

    fun setRingtone (context: Context, path: String) {

        val k = File(path, "mysong.mp3") // path is a file to /sdcard/media/ringtone


        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DATA, k.absolutePath)
        values.put(MediaStore.MediaColumns.TITLE, "My Song title")
        values.put(MediaStore.MediaColumns.SIZE, 215454)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
        values.put(MediaStore.Audio.Media.ARTIST, "Madonna")
        values.put(MediaStore.Audio.Media.DURATION, 230)
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
        values.put(MediaStore.Audio.Media.IS_ALARM, false)
        values.put(MediaStore.Audio.Media.IS_MUSIC, false)

        //Insert it into the database
        //Insert it into the database
        val uri: Uri = MediaStore.Audio.Media.getContentUriForPath(k.absolutePath)
        val newUri: Uri = context.contentResolver.insert(uri, values)

        RingtoneManager.setActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_RINGTONE,
            newUri
        )
    }

}