package com.example.maptest


import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import org.w3c.dom.Element
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mobile : Button = findViewById(R.id.mobileMap)
        mobile.setOnClickListener {
            val nextIntent = Intent(this, MobileMapActivity::class.java)
            startActivity(nextIntent)
        }
        val web : Button = findViewById(R.id.webMap)
        web.setOnClickListener {
            val nextIntent2 = Intent(this, WebMapActivity::class.java)
            startActivity(nextIntent2)
        }

        val test : Button = findViewById(R.id.testMap)
        test.setOnClickListener {

            val nextIntent3 = Intent(this, MobileMapActivityTest::class.java)
            startActivity(nextIntent3)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
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

    override fun onBackPressed() {  // 뒤로가기 시 alertdialog를 띄워 확실히 종료할 것인지 확인함
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("종료 확인").setMessage("정말 종료할거에요?")
        builder.setPositiveButton("그래") { dialog, which ->
            finish()
        }
        builder.setNegativeButton("아니") { dialog, which -> }
        builder.show()
    }
}