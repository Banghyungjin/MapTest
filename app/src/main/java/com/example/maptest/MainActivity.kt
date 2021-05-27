package com.example.maptest


import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mobile : Button = findViewById(R.id.mobileMap)
        mobile.setOnClickListener {
            val nextIntent = Intent(this, MobileMapActivityTest::class.java)
            startActivity(nextIntent)
        }
        val web : Button = findViewById(R.id.webMap)
        web.setOnClickListener {
            val nextIntent2 = Intent(this, WebMapActivity::class.java)
            startActivity(nextIntent2)
        }

        val test : Button = findViewById(R.id.testMap)
        test.setOnClickListener {
            val nextIntent3 = Intent(this, MobileMapActivity::class.java)
            startActivity(nextIntent3)
            //Toast.makeText(this, getNaverLocationSting(), Toast.LENGTH_SHORT).show()
        }


    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //val nextIntent = Intent(this, MobileMapActivity::class.java)
                //startActivity(nextIntent)
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_CANCEL -> {
            }
            else -> {
            }
        }
        return true
    }

    private fun getNaverLocationSting (): String {

        val clientID = "0l2mcc3fx2"
        val clientSecret = "vRz8M0yRPc1QRNU1KGwcJIDXslLcOSjhmg0t9kfk"
        val requestUrl = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode" +
                "/v2/gc?request=coordsToaddr&coords=129.1133567,35.2982640" +
                "&sourcecrs=epsg:4326&output=json&orders=legalcode,admcode"
        val url = URL(requestUrl)
        var conn : HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientID)
        conn.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret)
        conn.connect()

        return conn.responseCode.toString()
    }

    fun readStream(inputStream: BufferedInputStream): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        bufferedReader.forEachLine { stringBuilder.append(it) }
        return stringBuilder.toString()
    }

}