package com.lovelycafe.casheirpos.client.ui.bill

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BillViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Stay tuned for the upcoming bill functionality!"
    }
    val text: LiveData<String> = _text
}