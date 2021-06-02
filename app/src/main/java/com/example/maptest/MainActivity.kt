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
            val nextIntent = Intent(this, MobileMapActivityTest::class.java)
            startActivity(nextIntent)
        }
        val web : Button = findViewById(R.id.webMap)
        web.setOnClickListener {
//            val nextIntent2 = Intent(this, WebMapActivity::class.java)
//            startActivity(nextIntent2)
            web.setOnClickListener {
                var thread = NetworkThread()
                thread.start()
            }


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
        builder.setPositiveButton("그래") { dialog, which ->
            finish()
        }
        builder.setNegativeButton("아니") { dialog, which -> }
        builder.show()
    }

    inner class NetworkThread: Thread(){
        override fun run() {
            try {
                // 접속할 페이지의 주소
                var site = "http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19SidoInfStateJson?" +
                        "serviceKey=C%2F53dRPVlGwdFgAwBz0uNqX%2FB5COnUkL9cRSvJ01NqdIejOQZaHm%2FCh30E5AnXC3DnhSI17%2B64HJa57wiJNvKg%3D%3D" +
                        "&startCreateDt=20200410&endCreateDt=20200410"
                var url = URL(site)
                var conn = url.openConnection()
                var input = conn.getInputStream()

                var factory = DocumentBuilderFactory.newInstance()
                var builder = factory.newDocumentBuilder()
                // doc: xml문서를 모두 읽어와서 분석을 끝냄
                var doc = builder.parse(input)

                // root: xml 문서의 모든 데이터들을 갖고 있는 객체
                var root = doc.documentElement

                // xml 문서에서 태그 이름이 item인 태그들이 item_node_list에 리스트로 담김
                var item_node_list = root.getElementsByTagName("item")

                // item_node_list에 들어있는 태그 객체 수만큼 반복함
                for(i in 0 until item_node_list.length){
                    // i번째 태그 객체를 item_element에 넣음
                    var item_element = item_node_list.item(i) as Element

                    // item태그 객체에서 원하는 데이터를 태그이름을 이용해서 데이터를 가져옴
                    // xml 문서는 태그 이름으로 데이터를 가져오면 무조건 리스트로 나옴
                    var gubun_list = item_element.getElementsByTagName("gubun")
                    var defCnt_list = item_element.getElementsByTagName("defCnt")
                    var deathCnt_list = item_element.getElementsByTagName("deathCnt")
                    var isolIngCnt_list = item_element.getElementsByTagName("isolIngCnt")


                    var gubun_node = gubun_list.item(0) as Element
                    var defCnt_node = defCnt_list.item(0) as Element
                    var deathCnt_node = deathCnt_list.item(0) as Element
                    var isolIngCnt_node = isolIngCnt_list.item(0) as Element

                    // 태그 사이에 있는 문자열을 가지고 오는 작업
                    var gubun = gubun_node.textContent
                    var defCnt = defCnt_node.textContent
                    var deathCnt = deathCnt_node.textContent
                    var isolIngCnt = isolIngCnt_node.textContent

                    // Ui에 데이터를 출력해주는 부분
                    runOnUiThread {
                        Log.d("", "$gubun\n$defCnt\n$deathCnt\n$isolIngCnt")
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }


}