package com.example.onionshare
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.ProgressBar


class FileList(private val context: Activity, internal var fileslist: List<FileClass>) : ArrayAdapter<FileClass>(context, R.layout.file_item_fail, fileslist) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.file_item_fail, null, true)
        val filename = listViewItem.findViewById<View>(R.id.file_object) as TextView
        val status = listViewItem.findViewById<View>(R.id.status) as TextView
        val progress = listViewItem.findViewById<View>(R.id.progressBar) as ProgressBar
        val thisFile = fileslist[position]
        filename.text = thisFile.filename
        return listViewItem
    }
}