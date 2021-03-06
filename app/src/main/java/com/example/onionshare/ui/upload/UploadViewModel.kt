package com.example.onionshare.ui.upload

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class UploadViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Onion site goes here"
    }
    val text: LiveData<String> = _text

    fun change(t: String) {
        _text.value = t
    }

}