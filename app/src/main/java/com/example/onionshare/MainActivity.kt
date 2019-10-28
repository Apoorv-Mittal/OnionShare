package com.example.onionshare

import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
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
import com.example.onionshare.ui.upload.UploadFragment




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_upload, R.id.navigation_download
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val a = TorTask().execute()

        Toast.makeText(applicationContext, a.get(),Toast.LENGTH_LONG).show()


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

        override fun doInBackground(vararg strings: String): String {
            var l: String = ""
            val fileStorageLocation = "torfiles"
            val onionProxyManager =
                com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager(
                    applicationContext, fileStorageLocation
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

                val httpClient = getNewHttpClient()
                val port = onionProxyManager.iPv4LocalHostSocksPort
                val socksaddr = InetSocketAddress("127.0.0.1", port)
                val context = HttpClientContext.create()
                context.setAttribute("socks.address", socksaddr)

                //http://wikitjerrta4qgz4.onion/
                //https://api.duckduckgo.com/?q=whats+my+ip&format=json
                val httpGet = HttpGet("http://wikitjerrta4qgz4.onion/")
                val httpResponse = httpClient.execute(httpGet, context)
                val httpEntity = httpResponse.entity
                val httpResponseStream = httpEntity.content

                val httpResponseReader = BufferedReader(
                    InputStreamReader(httpResponseStream, "iso-8859-1"), 8
                )

                for (line in httpResponseReader.lines()) {
                    l = l.plus(line)
                    println(line)
                }

                httpResponseStream.close()
            } catch (e: Exception) {
                e.printStackTrace()

            }

            return l
        }

        override fun onPreExecute() {

        }

        override fun onPostExecute(result: String) {


        }
    }
}
