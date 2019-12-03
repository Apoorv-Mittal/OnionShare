package com.example.onionshare

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cz.msebera.android.httpclient.conn.DnsResolver
import java.net.InetAddress
import java.net.UnknownHostException


class SplashScreen : AppCompatActivity() {

    private var _port = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_screen)

        TorTask().execute()
    }

    internal class FakeDnsResolver : DnsResolver {
        @Throws(UnknownHostException::class)
        override fun resolve(host: String): Array<InetAddress> {
            return arrayOf(InetAddress.getByAddress(byteArrayOf(1, 1, 1, 1)))
        }
    }


    private inner class TorTask : android.os.AsyncTask<String, Int, String>() {

        private lateinit var i : Intent


        override fun doInBackground(vararg strings: String): String {
            val fileStorageLocation = "hiddenservicemanager"
            val onionProxyManager =
                com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager(
                    applicationContext,
                    fileStorageLocation
                )
            val totalSecondsPerTorStartup = 60
            val totalTriesPerTorStartup = 5
            try {
                val ok = onionProxyManager.startWithRepeat(
                    totalSecondsPerTorStartup,
                    totalTriesPerTorStartup
                )
                if (!ok) {
                    println("Couldn't start tor")
                    Toast.makeText(applicationContext,"TOR wasn't able to start", Toast.LENGTH_LONG).show()
                    finishAndRemoveTask()
                }

                while (!onionProxyManager.isRunning)
                    Thread.sleep(90)
                println("Tor initialized on port " + onionProxyManager.iPv4LocalHostSocksPort)

                i.putExtra("port",onionProxyManager.iPv4LocalHostSocksPort)

                val hiddenServicePort = 443
                val localPort = 9343
                val onionAddress =
                    onionProxyManager.publishHiddenService(hiddenServicePort, localPort)
                println("Tor onion address of the server is: $onionAddress")
                i.putExtra("URL", "http://" + onionAddress + ":" + hiddenServicePort.toString())

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "TOR Library was not able to perform its tasks", Toast.LENGTH_LONG).show()
                }
                finishAndRemoveTask()
                e.printStackTrace()
            }

            i.putExtra("Result", "success")
            startActivity(i)
            finish()

            return ""
        }

        override fun onPreExecute() {
            i = Intent(this@SplashScreen, MainActivity::class.java)
            i.action = "com.onionshare.main"
        }


    }

    fun get_port(): Int {
        return _port
    }
}