package com.example.onionshare

import android.os.Bundle
import android.os.Build
import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import kotlinx.android.synthetic.main.file_item.view.*


class DownloadActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE: Int = 1000;

    internal lateinit var urlText: EditText
    internal lateinit var connectButton: Button
    internal lateinit var downloadButton: Button
    internal lateinit var listViewFiles: ListView
    internal lateinit var filesList: MutableList<FileClass>

    lateinit var file_to_be_downloaded: FileClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_download)

        urlText = findViewById<View>(R.id.urlText) as EditText
        connectButton = findViewById<View>(R.id.buttonConnect) as Button
        downloadButton = findViewById<View>(R.id.button_connect) as Button
        listViewFiles = findViewById<View>(R.id.filesList) as ListView

        filesList = ArrayList()

        connectButton.setOnClickListener {
            val url = urlText.text.toString()

        }

        listViewFiles.onItemClickListener =
            AdapterView.OnItemClickListener{ adapterView, view, i, l ->
                file_to_be_downloaded = filesList[i]

                if (file_to_be_downloaded.downloaded == false){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED){
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                    }
                    else {
                        downloadFile(file_to_be_downloaded)
                        view.status.setVisibility(View.VISIBLE)
                        file_to_be_downloaded.downloaded = true

                    }
                }

            }


    }
    private fun downloadFile(thisfile: FileClass){
        val url = thisfile.fileurl

        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or
        DownloadManager.Request.NETWORK_MOBILE)

        request.setTitle("Download")
        request.setDescription("File Downloading")

        request.allowScanningByMediaScanner()
        request.setNotificationVisibility((DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED))
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
            "${System.currentTimeMillis()}")

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            STORAGE_PERMISSION_CODE ->{
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    downloadFile(file_to_be_downloaded)
                }
                else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()

                }
            }
        }
    }
}