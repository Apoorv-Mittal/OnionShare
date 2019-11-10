package com.example.onionshare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.msebera.android.httpclient.client.HttpClient
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.protocol.HttpClientContext
import cz.msebera.android.httpclient.config.RegistryBuilder
import cz.msebera.android.httpclient.conn.DnsResolver
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory
import cz.msebera.android.httpclient.impl.client.HttpClients
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager
import cz.msebera.android.httpclient.ssl.SSLContexts
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.io.File
import com.msopentech.thali.toronionproxy.Utilities
import android.system.Os.accept
import android.R.attr.port
import android.content.Context
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.io.ObjectInputStream
import java.net.ServerSocket


class SplashScreen : AppCompatActivity() {

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


    fun getNewHttpClient(): HttpClient {

        val reg = RegistryBuilder.create<ConnectionSocketFactory>()
            .register("http", MyConnectionSocketFactory())
            .register("https", MySSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
            .build()
        val cm = PoolingHttpClientConnectionManager(reg, FakeDnsResolver())
        return HttpClients.custom()
            .setConnectionManager(cm)
            .build()
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
            val totalSecondsPerTorStartup = 4 * 60
            val totalTriesPerTorStartup = 5
            try {
                val ok = onionProxyManager.startWithRepeat(
                    totalSecondsPerTorStartup,
                    totalTriesPerTorStartup
                )
                if (!ok)
                    println("Couldn't start tor")

                while (!onionProxyManager.isRunning)
                    Thread.sleep(90)
                println("Tor initialized on port " + onionProxyManager.iPv4LocalHostSocksPort)

                val hiddenServicePort = 8080
                val localPort = 9343
                val onionAddress =
                    onionProxyManager.publishHiddenService(hiddenServicePort, localPort)
                println("Tor onion address of the server is: $onionAddress")
                i.putExtra("URL", onionAddress)
                val serverSocket = ServerSocket(localPort)
                while (true) {
                    println("Waiting for client request")
                    val receivedSocket = serverSocket.accept()
                    val ois = ObjectInputStream(receivedSocket.getInputStream())
                    val message = ois.readObject() as String

                    //Here we will print the message received from the client to the console.
                    /*You may want to modify this function to display the received
                    string in your View.*/
                    println("Message Received: $message")
                }
            } catch (e: Exception) {
                e.printStackTrace()

            }

            return ""
        }

        override fun onPreExecute() {
            i = Intent(this@SplashScreen, MainActivity::class.java)
            i.action = "com.onionshare.main"
        }

        override fun onPostExecute(result: String) {
            i.putExtra("Result", result)
            startActivity(i)
            finish()
        }
    }
}