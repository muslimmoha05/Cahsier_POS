package com.lovelycafe.casheirpos.admin.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to Dashboard"
    }
    val text: LiveData<String> = _text
}