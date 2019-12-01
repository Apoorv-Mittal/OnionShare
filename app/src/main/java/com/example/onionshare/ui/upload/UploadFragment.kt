package com.example.onionshare.ui.upload

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.onionshare.MainActivity
import com.example.onionshare.R
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.R.attr.label
import android.app.Activity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.app.ActivityCompat.startActivityForResult
import android.content.Intent


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
        uploadViewModel.change((activity as MainActivity).getUrl())


        val pasteButton: Button = root.findViewById(R.id.copy_button)

        pasteButton.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            try {
                val clip = android.content.ClipData.newPlainText("Copied Text", textView.text)
                clipboard?.primaryClip = clip

                //Log.i("test", text)
            } catch (e: Exception) {

            }
        }

        val getfiles: Button = root.findViewById(R.id.file_selction_button)

        getfiles.setOnClickListener{
            val chooseFile: Intent
            val intent: Intent
            chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
            chooseFile.type = "*/*"
            chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent = Intent.createChooser(chooseFile, "Choose a file")
            startActivityForResult(intent, 5)
        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 5) {
            if (resultCode == Activity.RESULT_OK){
                if (null != data) {
                    if (null !=data.clipData) {
                        for (i in 0 until data.clipData.itemCount) {
                            val uri = data.clipData.getItemAt(i).uri
                            print(uri)
                        }
                    } else {
                        val uri = data.data.path
                        print(uri)
                    }
                }
            }
        }
    }

}