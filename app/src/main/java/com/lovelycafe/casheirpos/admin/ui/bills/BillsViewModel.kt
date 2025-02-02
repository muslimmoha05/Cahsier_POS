package com.lovelycafe.casheirpos.admin.ui.bills

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BillsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Bills Fragment"
    }
    val text: LiveData<String> = _text
}