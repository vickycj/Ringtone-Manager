package com.vicky.apps.datapoints.ui

import androidx.lifecycle.LiveData

import androidx.lifecycle.MediatorLiveData


class LiveDataHelper private constructor() {
    private val _percent = MediatorLiveData<Int>()
    fun updatePercentage(percentage: Int) {
        _percent.postValue(percentage)
    }

    fun observePercentage(): LiveData<Int> {
        return _percent
    }

    companion object {
        private var liveDataHelper: LiveDataHelper? = null
        @get:Synchronized
        val instance: LiveDataHelper?
            get() {
                if (liveDataHelper == null) liveDataHelper =
                    LiveDataHelper()
                return liveDataHelper
            }
    }
}
