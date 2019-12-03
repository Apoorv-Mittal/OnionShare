package com.example.onionshare.ui.download

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
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
import java.io.File
import java.net.InetSocketAddress
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.protocol.HttpClientContext

import org.jsoup.Jsoup

class DownloadFragment : Fragment() {

    private lateinit var downloadViewModel: DownloadViewModel

    var port = 0
    
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
        port = (activity as MainActivity).get_port()

        val textView: TextView = root.findViewById(R.id.text_download)
        val pasteButton: Button = root.findViewById(R.id.paste_botton)

        val connectButton = root.findViewById(R.id.button_connect) as Button  //Button to connect to url

        // List view and array for files, based off firebase project (how they display authors),
        // for displaying list of files
        var listViewFiles = root.findViewById(R.id.filesList) as ListView
        var filesList: MutableList<FileClass>
        filesList = ArrayList()

        connectButton.setOnClickListener {
            connecttask().execute(Pair(textView.text.toString(), filesList)).get()
            val fileAdapter = FileList(getActivity()!!, filesList)
            listViewFiles.adapter = fileAdapter
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
                        Toast.makeText(getActivity()?.applicationContext, "Permission to write not granted",Toast.LENGTH_LONG).show()

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


    private inner class connecttask(): AsyncTask<Pair<String,MutableList<FileClass>>,Void, String>(){
        override fun doInBackground(vararg url: Pair<String,MutableList<FileClass>>?): String? {
            //val onionAddress = onionProxyManager.publishHiddenService(hiddenServicePort, localPort)
            port = (activity as MainActivity).get_port()
            val httpClient = getNewHttpClient()
            val socksaddr = InetSocketAddress("127.0.0.1", port)
            val httpcontext = HttpClientContext.create()
            httpcontext.setAttribute("socks.address", socksaddr)

            val tor_url = url[0]!!.first
            var list = url[0]!!.second

            val httpGet = HttpGet(tor_url)
            val httpResponse = httpClient.execute(httpGet, httpcontext)
            val httpEntity = httpResponse.entity
            val httpResponseStream = httpEntity.content

            // Reading the html of the url to get urls of files using regex?
            // Not sure if correct implementation, should look more into this after figuring out how html works with url
            val doc = Jsoup.parse(httpResponseStream,null,tor_url)

            doc.select("a").forEach { e ->
                   var filename = e.attr("href")
                   var url = e.attr("abs:href")
                   list.add(FileClass(filename,url,false))
            }

            return ""
        }
    }


    // Download task
    private inner class downloadtask(): AsyncTask<FileClass,Void,String>(){

        override fun doInBackground(vararg p0: FileClass?): String {
            // val onionAddress = onionProxyManager.publishHiddenService(hiddenServicePort, localPort)
            val httpClient = getNewHttpClient()
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

            //val path = Environment.getExternalStorageDirectory().toString()
            val path2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            val downloadFile: File
            downloadFile = File(path2, thisFile?.filename)
            downloadFile.writeBytes(bytes)
            httpResponseStream.close()
            //view?.status?.setVisibility(View.VISIBLE)  //Sets "Downloaded" text to visible of file_list item


            return ""
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Toast.makeText(getActivity()?.applicationContext, "File downloaded",Toast.LENGTH_LONG).show()
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
        }

    }


}