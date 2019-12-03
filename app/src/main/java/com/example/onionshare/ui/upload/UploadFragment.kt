package com.example.onionshare.ui.upload

import android.R.attr.*
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
import android.app.Activity
import android.app.Application
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.app.ActivityCompat.startActivityForResult
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log

import android.view.Menu
import android.widget.Toast
import androidx.core.net.toFile

import com.sun.net.httpserver.*

import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.URI
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UploadFragment : Fragment() {

    private lateinit var uploadViewModel: UploadViewModel


    companion object{
        private var serverUp = false

        private var selected: HashMap<String, Uri> = HashMap<String,Uri>()
        private val HEADER = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <body>\n"

        private val FOOTER = "  </body>\n" +
                "</html>"

        private val FILE_CHOOSER = 5
    }


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
            chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
            chooseFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooseFile.type = "*/*"
            chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent = Intent.createChooser(chooseFile, "Choose a file")
            startActivityForResult(intent, FILE_CHOOSER)

        }


        //create server thread

        val port = 9343

        if(!serverUp){
            startServer(port)
            serverUp = true
        }

        Toast.makeText(getActivity()?.applicationContext,"It may take several minutes for the hidden service to be routable", Toast.LENGTH_LONG)


        return root
    }


    private fun streamToString(inputStream: InputStream): String {
        val s = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    private fun sendResponse(httpExchange: HttpExchange, responseText: String){
        httpExchange.sendResponseHeaders(200, responseText.length.toLong())
        val os = httpExchange.responseBody
        os.write(responseText.toByteArray())
        os.close()
    }

    private fun sendFile(context: Context?,httpExchange: HttpExchange, uri: Uri?){
        //get file bytes

        var responseText = context!!.contentResolver.openInputStream(uri).readBytes()


        httpExchange.sendResponseHeaders(200, responseText.size.toLong())
        val os = httpExchange.responseBody
        os.write(responseText)
        os.close()
    }


    private var mHttpServer: HttpServer? = null

    private fun startServer(port: Int) {
        try {
            mHttpServer = HttpServer.create(InetSocketAddress(port), 0)
            mHttpServer!!.executor = Executors.newCachedThreadPool()

            mHttpServer!!.createContext("/", rootHandler)
            mHttpServer!!.start()//startServer server;

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun stopServer() {
        if (mHttpServer != null){
            mHttpServer!!.stop(0)
        }
    }

    // Handler for root endpoint
    private val rootHandler = HttpHandler { exchange ->
        run {
            // Get request method
            when (exchange!!.requestMethod) {
                "GET" -> {
                    when(exchange.requestURI.path){
                        "/" -> {
                            //selected.joinToString(prefix = "<li><a href=\"yeet.html\">",  separator = "\n", postfix = "</a></li>") +
                            var ret = ""
                            selected.keys.forEach { elm -> ret += "\n<li><a href=\"$elm\">$elm</a></li>" }
                            sendResponse(exchange, "$HEADER<ul>\n$ret</ul>$FOOTER")
                            return@run
                        } else -> {
                            if(selected.keys.contains(exchange.requestURI.path)){
                                //sendResponse(exchange, "$HEADER<ul><li>hii</li><ul>$FOOTER")

                                sendFile(activity?.applicationContext,exchange, selected.get(exchange.requestURI.path))
                                return@run
                            }
                        }
                    }

                }
            }
        }
    }


    private val messageHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "GET" -> {
                    // Get all messages
                    sendResponse(httpExchange, "Would be all messages stringified json")
                }
            }
        }
    }

    fun getImageFilePath(context:Context?, uri: Uri?): String{
        var cursor: Cursor? = context!!.getContentResolver().query(uri, null, null, null, null)
        cursor!!.moveToFirst();
        var image_id = cursor.getString(0);
        image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
        cursor.close();
        cursor = context.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", arrayOf(image_id), null);
        cursor.moveToFirst();
        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path.split("/").last().replace("\\","/")
    }

    private fun getFileName(context:Context? ,uri: Uri?): String {
        var result = ""

        if (uri?.getScheme().equals("content")) {
            var cursor: Cursor? = context!!.getContentResolver().query(uri, null, null, null, null)
            try {
                if (cursor!=null && cursor!!.moveToFirst()){
                    result = cursor!!.getString(cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }finally {
                cursor!!.close()
            }
        }

        if(result == ""){
            result = uri!!.path
            val cut = result.lastIndexOf('/')

            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return Base64.getUrlEncoder().encodeToString(result.toByteArray())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER) {
            if (resultCode == Activity.RESULT_OK){
                if (null != data) {
                    if (null !=data.clipData) {
                        for (i in 0 until data.clipData.itemCount) {
                            val uri = data.clipData.getItemAt(i).uri

                            selected.put("/" + getFileName(getActivity()?.applicationContext,uri) , uri)
                            Toast.makeText(getActivity()?.applicationContext, "Filename put into map",
                                Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val uri = data.data
                        selected.put("/" + getFileName(getActivity()?.applicationContext,uri) , uri)
                        Toast.makeText(getActivity()?.applicationContext, "Filename put into map",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


}