package com.example.onionshare

// File class containing name, url, and downloaded status
data class FileClass (val filename: String="",val fileurl: String = "", var downloaded: Boolean = false)