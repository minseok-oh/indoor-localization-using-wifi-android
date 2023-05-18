package com.example.client

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 1
    private val INTERVAL = 1000L
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val POST_URL = "http://192.168.0.6:8000/save"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        } else {
            startWifiDataFetching()
        }
    }

    private fun startWifiDataFetching() {
        GlobalScope.launch(Dispatchers.Main) {
            var i: Int = 0
            while (i < 20) {
                getWifiRssi()
                delay(INTERVAL)
                i++
            }
        }
    }

    private fun getWifiRssi() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            wifiManager.startScan()

            val scanResults: List<ScanResult> = wifiManager.scanResults
            val coroutineScope = CoroutineScope(Dispatchers.Main)

            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val json = createRssiJson(scanResults)
                    sendWifiRssiData(json)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startWifiDataFetching()
            }
        }
    }

    private fun createRssiJson(scanResults: List<ScanResult>): String {
        var jsonData: String = "{\"wifi\": {\n"
        for (scanResult in scanResults) {
            jsonData = jsonData + "\"${scanResult.BSSID}\": \"${scanResult.level}\""
            if (scanResults.last() != scanResult) jsonData = jsonData + ",\n"
        }
        jsonData = jsonData + "}}"

        println(jsonData)
        return jsonData
    }

    private fun sendWifiRssiData(json: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()

        val requestBody = json.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(POST_URL)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    println("success")
                } else {
                    println("failure")
                }
            }
        } catch (e: java.lang.Exception) {
            println(e)
        }

    }
}