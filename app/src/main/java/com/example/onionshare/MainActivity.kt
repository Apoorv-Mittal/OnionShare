package com.example.onionshare

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager


class MainActivity : AppCompatActivity() {

    private var URL = ""
    private var port = 0

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


        URL = intent.getStringExtra("URL")

        port = intent.getIntExtra("port", -1)

        val a = intent.getStringExtra("Result")


        Toast.makeText(applicationContext, a,Toast.LENGTH_LONG).show()
    }

    fun getUrl(): String {
        return URL
    }

    fun get_port(): Int {
        return port
    }


}
