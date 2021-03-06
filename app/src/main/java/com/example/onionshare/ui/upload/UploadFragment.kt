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
import android.content.ClipboardManager
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.ListView
import android.widget.Toast
import com.example.onionshare.FileClass
import com.example.onionshare.FileList

import com.sun.net.httpserver.*
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class UploadFragment : Fragment() {

    private lateinit var uploadViewModel: UploadViewModel

    private lateinit var listViewFiles: ListView


    companion object{
        private var serverUp = false

        private var filelistdisplay: MutableList<FileClass> = ArrayList()


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


        //val fileAdapter = FileList(getActivity()!!, filelistdisplay)
        //listViewFiles.adapter = fileAdapter
        listViewFiles = root.findViewById(R.id.filesList)
        val fileAdapter = FileList(getActivity()!!, filelistdisplay)
        listViewFiles.adapter = fileAdapter



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

        val getfiles: Button = root.findViewById(R.id.file_selection_button)

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

        Toast.makeText(activity?.applicationContext,"It may take several minutes for the hidden service to be routable", Toast.LENGTH_LONG).show()


        return root
    }

    override fun onResume(){
        super.onResume()
        val fileAdapter = FileList(getActivity()!!, filelistdisplay)
        listViewFiles.adapter = fileAdapter

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

    private fun sendFile(context: Context?,httpExchange: HttpExchange, uri: Uri){
        //get file bytes

        var responseText = context!!.contentResolver.openInputStream(uri)?.readBytes()


        httpExchange.sendResponseHeaders(200, responseText!!.size.toLong())
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
                                sendFile(activity?.applicationContext,exchange, selected.get(exchange.requestURI.path)!!)
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
        var cursor: Cursor? = context!!.getContentResolver().query(uri!!, null, null, null, null)
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
            var cursor: Cursor? = context!!.getContentResolver().query(uri!!, null, null, null, null)
            try {
                if (cursor!=null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }finally {
                cursor!!.close()
            }
        }

        if(result == ""){
            result = uri!!.path as String
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
                        for (i in 0 until data.clipData!!.itemCount) {
                            val uri = data.clipData!!.getItemAt(i).uri
                            val filename = "/"+getFileName(getActivity()?.applicationContext,uri)

                            if(!selected.containsKey(filename)) {
                                filelistdisplay.add(
                                    FileClass(
                                        String(
                                            android.util.Base64.decode(
                                                filename.substring(1),
                                                android.util.Base64.DEFAULT
                                            )
                                        ),
                                        uri.toString(),
                                        false
                                    )
                                )
                            }
                            selected.put("/" + getFileName(getActivity()?.applicationContext,uri) , uri)



                            Toast.makeText(getActivity()?.applicationContext, "Filename put into map",
                                Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val uri = data.data
                        val filename = "/"+getFileName(getActivity()?.applicationContext,uri)
                        if(!selected.containsKey(filename)) {
                            filelistdisplay.add(
                                FileClass(
                                    String(
                                        android.util.Base64.decode(
                                            filename.substring(1),
                                            android.util.Base64.DEFAULT
                                        )
                                    ),
                                    uri!!.toString(),
                                    false
                                )
                            )
                        }
                        selected.put("/" + getFileName(getActivity()?.applicationContext,uri) , uri!!)
                        Toast.makeText(getActivity()?.applicationContext, "Filename put into map",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


}