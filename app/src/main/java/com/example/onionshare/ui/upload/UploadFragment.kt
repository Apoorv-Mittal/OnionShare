package com.example.onionshare.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.onionshare.R

class UploadFragment : Fragment() {

    private lateinit var uploadViewModel: UploadViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uploadViewModel =
            ViewModelProviders.of(this).get(UploadViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_upload, container, false)
        val textView: TextView = root.findViewById(R.id.text_upload)
        uploadViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}