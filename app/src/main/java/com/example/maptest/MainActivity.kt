package com.example.maptest


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.Button

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
            val nextIntent2 = Intent(this, MobileMapActivity::class.java)
            //val nextIntent2 = Intent(Intent.ACTION_VIEW, Uri.parse("nmap://map?&appname=com.example.maptest"))
            startActivity(nextIntent2)
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
}