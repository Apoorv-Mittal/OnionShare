package com.example.onionshare.ui.download

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DownloadViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Onionsite goes here"
    }
    val text: LiveData<String> = _text
}