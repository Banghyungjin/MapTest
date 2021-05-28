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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


class MainActivity : AppCompatActivity() {
    val CLIENT_ID = "0l2mcc3fx2"
    val CLIENT_SECRET = "vRz8M0yRPc1QRNU1KGwcJIDXslLcOSjhmg0t9kfk"
    val BASE_URL_NAVER_API = "https://naveropenapi.apigw.ntruss.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fun returnLocation(){
            var response_string : ResultGetLocationJson? = null
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_NAVER_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(NaverReverseGeoAPI::class.java)
            val callGetLocation = api.getLocation(CLIENT_ID, CLIENT_SECRET, "127.2654387,36.5008113")
            callGetLocation.enqueue(object : Callback<ResultGetLocationJson> {
                override fun onResponse(
                    call: Call<ResultGetLocationJson>,
                    response: Response<ResultGetLocationJson>
                ) {
                    response_string = response.body()
                    Log.d("결과", "성공 : ${response_string?.results?.size}")
                    Toast.makeText(this@MainActivity, "${response_string?.results?.get(0)?.region?.area1?.name}", Toast.LENGTH_LONG).show()
                }

                override fun onFailure(call: Call<ResultGetLocationJson>, t: Throwable) {
                    Log.d("결과:", "실패 : $t")
                }
            })
        }

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





}