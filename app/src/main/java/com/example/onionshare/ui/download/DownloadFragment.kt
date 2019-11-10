package com.example.onionshare.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.onionshare.R
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Button
import android.content.Context.CLIPBOARD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.content.Context



class DownloadFragment : Fragment() {

    private lateinit var downloadViewModel: DownloadViewModel
    private var myClipboard: ClipboardManager? = null
    private var myClip: ClipData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        downloadViewModel =
            ViewModelProviders.of(this).get(DownloadViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_download, container, false)
        /////
        val textView: TextView = root.findViewById(R.id.text_download)


        val pasteButton: Button = root.findViewById(R.id.paste_botton)

        pasteButton.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            try {
                val text = clipboard!!.primaryClip!!.getItemAt(0).text
                downloadViewModel.text.observe(this, Observer {
                    textView.text = text
                })
                //Log.i("test", text)
            } catch (e: Exception) {

            }
        }



        return root
    }



}