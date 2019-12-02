package com.example.onionshare.ui.download

import android.Manifest
import android.app.DownloadManager
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.onionshare.*
import cz.msebera.android.httpclient.client.HttpClient
import cz.msebera.android.httpclient.config.RegistryBuilder
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory
import cz.msebera.android.httpclient.impl.client.HttpClients
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager
import cz.msebera.android.httpclient.ssl.SSLContexts
import kotlinx.android.synthetic.main.file_item.view.*
import java.io.File
import java.net.InetSocketAddress

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.protocol.HttpClientContext
import cz.msebera.android.httpclient.conn.DnsResolver
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.UnknownHostException


class DownloadFragment : Fragment() {

    private lateinit var downloadViewModel: DownloadViewModel

    private val STORAGE_PERMISSION_CODE: Int = 1000;

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

        val connectButton = root.findViewById(R.id.buttonConnect) as Button  //Button to connect to url

        // List view and array for files, based off firebase project (how they display authors),
        // for displaying list of files
        var listViewFiles = root.findViewById(R.id.filesList) as ListView
        var filesList: MutableList<FileClass>
        filesList = ArrayList()

        connectButton.setOnClickListener {
            val url = textView.text.toString()   // The pasted url

            val hiddenServicePort = 80
            val localPort = 9343

            val onionProxyManager =
                com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager(
                    getActivity()?.applicationContext, "torfiles"
                )

            //val onionAddress = onionProxyManager.publishHiddenService(hiddenServicePort, localPort)
            val httpClient = getNewHttpClient()
            val port = onionProxyManager.iPv4LocalHostSocksPort
            val socksaddr = InetSocketAddress("127.0.0.1", port)
            val httpcontext = HttpClientContext.create()
            httpcontext.setAttribute("socks.address", socksaddr)

            val httpGet = HttpGet(url)
            val httpResponse = httpClient.execute(httpGet, httpcontext)
            val httpEntity = httpResponse.entity
            val httpResponseStream = httpEntity.content

            // Reading the html of the url to get urls of files using regex?
            // Not sure if correct implementation, should look more into this after figuring out how html works with url
            val reader = httpResponseStream.bufferedReader()
            val iterator = reader.lineSequence().iterator()
            while (iterator.hasNext()) {
                var line = iterator.next()
                val pattern = "/".toRegex()  // Matching all urls with have original url within it?
                var seperated = line.split("=")
                for (word in seperated) {
                    if (pattern.matches(word)) {
                        var newfile: FileClass

                        val newurl = url+word
                        val filename = word.removePrefix("/")

                        newfile = FileClass(filename, newurl, false)
                        filesList.add(newfile)

                    }
                }
                // adapter for displaying files, based off of firebase project (how they display authors)
                val fileAdapter = FileList(getActivity()!!, filesList)
                listViewFiles.adapter = fileAdapter

            }
        }

        // Clicking on fileitem on displayed list of files
        listViewFiles.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val file_item = filesList[i]

                val file_url = file_item.fileurl

                // Permissions
                if (file_item.downloaded == false) {   // If file hasn't been downloaded yet
                                                                    // All FileClass items initiated with
                                                                    // downloaded = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(
                            context!!,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            STORAGE_PERMISSION_CODE
                        )
                    } else {
                        // execute async task to download file
                        val task = downloadtask()
                        task.execute(file_item)
                        filesList[i].downloaded = true  //Set downloaded status = true

                    }
                } else {
                    Toast.makeText(getActivity()?.applicationContext, "File already downloaded",Toast.LENGTH_LONG).show()

                }

            }

        // Paste function implemented by chris
        pasteButton.setOnClickListener {
            val clipboard =
                context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
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

    // Using apoorv's getNewHttpClient
    fun getNewHttpClient(): HttpClient {

        val reg = RegistryBuilder.create<ConnectionSocketFactory>()
            .register("http", MyConnectionSocketFactory())
            .register("https", MySSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
            .build()
        val cm = PoolingHttpClientConnectionManager(reg, SplashScreen.FakeDnsResolver())
        return HttpClients.custom()
            .setConnectionManager(cm)
            .build()
    }

    // Download task
    private inner class downloadtask(): AsyncTask<FileClass,Void,String>(){

        override fun doInBackground(vararg p0: FileClass?): String {

            val hiddenServicePort = 80
            val localPort = 9343

            val onionProxyManager =
                com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager(
                    getActivity()?.applicationContext, "torfiles"
                )

            // val onionAddress = onionProxyManager.publishHiddenService(hiddenServicePort, localPort)
            val httpClient = getNewHttpClient()
            val port = onionProxyManager.iPv4LocalHostSocksPort
            val socksaddr = InetSocketAddress("127.0.0.1", port)
            val context = HttpClientContext.create()
            context.setAttribute("socks.address", socksaddr)


            val thisFile = p0[0]

            val httpGet = HttpGet(thisFile?.fileurl)
            val httpResponse = httpClient.execute(httpGet, context)
            val httpEntity = httpResponse.entity
            val httpResponseStream = httpEntity.content

            // reading bytes from stream, writing them to new file with FileClass's name, and
            // path using getExternalStorageDirectory
            val bytes = httpResponseStream.readBytes()

            val path = Environment.getExternalStorageDirectory().toString()
            val downloadFile: File
            downloadFile = File(path, thisFile?.filename)
            downloadFile.writeBytes(bytes)
            httpResponseStream.close()
            view?.status?.setVisibility(View.VISIBLE)  //Sets "Downloaded" text to visible of file_list item
            Toast.makeText(getActivity()?.applicationContext, "File downloaded",Toast.LENGTH_LONG).show()

            return ""
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
        }

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
                    val task = downloadtask()
                    task.execute()
                }
                else{
                    Toast.makeText(context!!, "Permission Denied", Toast.LENGTH_LONG).show()

                }
            }
        }
    }





}