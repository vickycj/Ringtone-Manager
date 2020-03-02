package com.vicky.apps.datapoints.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vicky.apps.datapoints.data.remote.Repository
import javax.inject.Inject


class ViewModelProviderFactory @Inject constructor( var repository: Repository,  var schedulerProvider: SchedulerProvider) :
    ViewModelProvider.NewInstanceFactory() {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        throw  IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName())
    }
}