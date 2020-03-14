package com.vicky.apps.datapoints.ui

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.vicky.apps.datapoints.R
import com.vicky.apps.datapoints.base.AppConstants

import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.content_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->

            saveDetails(AppConstants.DEVICE_NAME,deviceName.text.toString())
            saveDetails(AppConstants.CONTACT_1,contact1.text.toString())
            saveDetails(AppConstants.CONTACT_2,contact2.text.toString())
            saveDetails(AppConstants.CONTACT_3,contact3.text.toString())

            Snackbar.make(view, "Data Saved", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun saveDetails(key: String, value: String) {
        with (applicationContext?.getSharedPreferences(AppConstants.NAME,0)?.edit()) {
            this?.putString(key, value)
            this?.commit()
        }
    }

}
