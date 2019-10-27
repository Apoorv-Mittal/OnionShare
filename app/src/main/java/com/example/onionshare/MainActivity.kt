package com.example.onionshare

import android.app.Application
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

import org.torproject.android.binary.TorResourceInstaller
import android.widget.Toast
import com.jrummyapps.android.shell.Shell
import android.util.Log
import java.io.File


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

        try {
            val torResourceInstaller = TorResourceInstaller(this, filesDir)

            val fileTorBin = torResourceInstaller.installResources()
            val fileTorRc = torResourceInstaller.torrcFile

            val success = fileTorBin != null && fileTorBin.canExecute()

            val message = "Tor install success? $success"

            if (success) {
                runTorShellCmd(fileTorBin, fileTorRc)
            }


            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)


        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT)

        }
    }

    private fun logNotice(notice: String) {

        Log.e("SampleTor", notice)
    }


    @Throws(Exception::class)
    private fun runTorShellCmd(fileTor: File, fileTorrc: File): Boolean {
        val appCacheHome =
            getDir(SampleTorServiceConstants.DIRECTORY_TOR_DATA, Application.MODE_PRIVATE)

        if (!fileTorrc.exists()) {
            logNotice("torrc not installed: " + fileTorrc.getCanonicalPath())
            return false
        }

        val torCmdString = (fileTor.getCanonicalPath()
                + " DataDirectory " + appCacheHome.canonicalPath
                + " --defaults-torrc " + fileTorrc)

        var exitCode = -1

        try {
            exitCode = exec(torCmdString + " --verify-config", true)
        } catch (e: Exception) {
            logNotice("Tor configuration did not verify: " + e.message)
            return false
        }

        try {
            exitCode = exec(torCmdString, true)
        } catch (e: Exception) {
            logNotice("Tor was unable to start: " + e.message)
            return false
        }

        if (exitCode != 0) {
            logNotice("Tor did not start. Exit:$exitCode")
            return false
        }

        return true
    }


    @Throws(Exception::class)
    private fun exec(cmd: String, wait: Boolean): Int {
        val shellResult = Shell.run(cmd)

        //  debug("CMD: " + cmd + "; SUCCESS=" + shellResult.isSuccessful());

        if (!shellResult.isSuccessful()) {
            throw Exception("Error: " + shellResult.exitCode + " ERR=" + shellResult.getStderr() + " OUT=" + shellResult.getStdout())
        }

        return shellResult.exitCode
    }
}
