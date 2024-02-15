package com.example.happybirthday.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.happybirthday.model.MyEvent

open class MainActivityViewModel : ViewModel() {
    private val _dataEvents: MutableLiveData<List<MyEvent>> by lazy {
        MutableLiveData<List<MyEvent>>()
    }
    val data: LiveData<List<MyEvent>> get() = _dataEvents

    fun setData(events: List<MyEvent>) {
        _dataEvents.value = events
    }

//    fun getEvent(position: Int): MyEvent? {
//        val events = _dataEvents.value
//        return events?.get(position)
//    }
}