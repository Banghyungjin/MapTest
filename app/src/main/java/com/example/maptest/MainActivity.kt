package com.example.maptest


import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.Button


class MainActivity : AppCompatActivity() {
    val CLIENT_ID = "0l2mcc3fx2"
    val CLIENT_SECRET = "vRz8M0yRPc1QRNU1KGwcJIDXslLcOSjhmg0t9kfk"
    val BASE_URL_NAVER_API = "https://naveropenapi.apigw.ntruss.com/"

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

            val nextIntent3 = Intent(this, MobileMapActivityTest::class.java)
            startActivity(nextIntent3)
//            returnLocation()
//            Toast.makeText(this, returnLocation().toString(), Toast.LENGTH_SHORT).show()
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


    override fun onBackPressed() {  // 뒤로가기 시 alertdialog를 띄워 확실히 종료할 것인지 확인함
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("종료 확인").setMessage("정말 종료할거에요?")
        builder.setPositiveButton("그래", DialogInterface.OnClickListener{dialog, which ->
            finish()
        })
        builder.setNegativeButton("아니", DialogInterface.OnClickListener{dialog, which -> })
        builder.show()
    }


}