package com.example.maptest

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.annotation.UiThread
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.maptest.jsons.ResultGetLocationJson
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PolygonOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import org.json.JSONObject
import org.w3c.dom.Element
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class MobileMapActivityTest : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource    // 주소 받아오는 거
    private lateinit var naverMap: NaverMap                     // 네이버 맵 객체
    private val CLIENT_ID = "0l2mcc3fx2"
    private val CLIENT_SECRET = "vRz8M0yRPc1QRNU1KGwcJIDXslLcOSjhmg0t9kfk"
    private val BASE_URL_NAVER_API = "https://naveropenapi.apigw.ntruss.com/"

    // 지도에 그릴 폴리곤 윤곽선 색깔들
    private val lineColorArray : ArrayList<Int> = arrayListOf(Color.rgb(10,180,10),
        Color.rgb(250,200,0), Color.rgb(255,102,0), Color.rgb(255,0,0))
    // 지도에 그릴 폴리곤 내부 색깔들
    private val colorArray : ArrayList<Int> = arrayListOf(Color.argb(80, 10,180,10),
        Color.argb(80,200,250,0), Color.argb(80,255,102,0),
        Color.argb(80,255,0,0))

    private var counter = 0

    // 행정구역 이름
    private val locationArray : ArrayList<String> = arrayListOf("강원도","경기도","경상남도","경상북도",
        "광주광역시","대구광역시","대전광역시","부산광역시","서울특별시","세종특별자치시","울산광역시",
        "인천광역시","전라남도","전라북도","제주특별자치도","충청남도","충청북도")

    private val covidLocationArray : ArrayList<String> = arrayListOf("강원","경기","경남","경북",
        "광주","대구","대전","부산","서울","세종","울산","인천","전남","전북","제주","충남","충북","합계")

    private val covidNumberArray : ArrayList<ArrayList<String>> = arrayListOf()
    private var multiPolygonArray : ArrayList<ArrayList<PolygonOverlay>> = arrayListOf()   // 지도에 표시할 폴리곤 (배열로 만들어 다수를 한꺼번에 표시)
    private var polygonArray : ArrayList<PolygonOverlay> = arrayListOf()   // 지도에 표시할 폴리곤 (배열로 만들어 다수를 한꺼번에 표시)
    private var indexOfLocationArray = locationArray.size

    override fun onCreate(savedInstanceState: Bundle?) {    // 액티비티 시작될 때 실행되는 함수
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_map)
        locationSource =    // 로케이션 소스 받아옴
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)   // 맵이 비동기로 작동되도록 해줌
        val covidThread = NetworkThread()
        covidThread.start()
        val locationThread = MakeLocationArrayThread()
        locationThread.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap    // 네이버 맵 객체
        val uiSettings = naverMap.uiSettings    // 네이버 모바일맵 UI 사용
//        val projection = naverMap.projection
//        var counter = true
        val marker = Marker()
        naverMap.locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)   // 로케이션 소스 받아옴
        uiSettings.isLocationButtonEnabled = true   // 현재 위치 버튼 사용
        naverMap.isIndoorEnabled = true             // 실내 지도 사용
        //naverMap.buildingHeight = 1f              // 지도 확대시 빌딩 3D로 보임, 50% 투명도 float값으로 빌딩 높이 조절가능
        naverMap.setOnSymbolClickListener { symbol ->   // 심볼 마커 클릭 시 위, 경도와 이름 출력 함수
            Toast.makeText(this, symbol.caption + "\n위도 = " +
                    (symbol.position.latitude * 100).roundToInt() / 100f +
                    "\n경도 = " + (symbol.position.longitude * 100).roundToInt() / 100f,
                Toast.LENGTH_SHORT).show()
            // 밑에서 true로 하면 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
            false
        }
        fun returnLocation(coord : LatLng){
            var responseString: ResultGetLocationJson?      // reversegeocoding api에서 값을 받아올 변수
            val retrofit = Retrofit.Builder()               // retrofit http 통신
                .baseUrl(BASE_URL_NAVER_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(NaverReverseGeoAPI::class.java)
            val requestCoord : String = coord.longitude.toString() + "," + coord.latitude.toString()
            val callGetLocation = api.getLocation(CLIENT_ID, CLIENT_SECRET, requestCoord)
            callGetLocation.enqueue(object : Callback<ResultGetLocationJson> {
                override fun onResponse(
                    call: Call<ResultGetLocationJson>,
                    response: Response<ResultGetLocationJson>
                ) {
                    responseString = response.body()
                    if (responseString?.results?.size != 0 && responseString?.results?.get(0)?.region?.area1?.name != null) {
                        val mapString = responseString?.results?.get(0)?.region?.area1?.name    // 선택한 지역 광역시도 이름
                        val mapStringIndex = locationArray.indexOf(mapString)
                        val covidInfo = covidNumberArray[mapStringIndex]
                        val mapCenterCoordLongi = responseString?.results?.get(0)?.region?.area1?.coords?.center?.x  // 선택한 지역 중심지 경도
                        val mapCenterCoordLati = responseString?.results?.get(0)?.region?.area1?.coords?.center?.y  // 선택한 지역 중심지 위도
                        indexOfLocationArray = locationArray.indexOf(mapString)
                        counter = if (covidInfo[0].toInt() > 10000) {
                            3
                        } else if (covidInfo[0].toInt() > 5000) {
                            2
                        } else if (covidInfo[0].toInt() > 1000) {
                            1
                        } else {
                            0
                        }

                        // 마커 설정 및 출력 코드
                        if (mapCenterCoordLati != null && mapCenterCoordLongi != null) {    // 선택한 지역의 중심 좌표에 마커를 생성
                            marker.position = LatLng(mapCenterCoordLati.toDouble(),mapCenterCoordLongi.toDouble())  // 마커 위경도 설정
                            marker.captionText = "$mapString"   // 마커 텍스트 설정
                            marker.setCaptionAligns(Align.Top)  // 마커 텍스트 위치
                            marker.captionTextSize = 25f        // 마커 텍스트 크기
                            marker.subCaptionText = "${returnDateString()} 확진자 : ${covidInfo[0]}명" +
                                    "\n${returnDateString()} 확진자 증가량 : \n내부발병 ${covidInfo[1]}명" +
                                    " + 외부유입 ${covidInfo[2]}명 = " +
                                    "총 ${covidInfo[3]}명" +
                                    "\n${returnDateString()} 격리자 : ${covidInfo[4]}명" +
                                    "\n${returnDateString()} 사망자  : ${covidInfo[5]}명"
                            marker.subCaptionColor = Color.DKGRAY
                            marker.subCaptionTextSize = 17f
                            marker.isHideCollidedSymbols = true
                            marker.icon = MarkerIcons.BLACK
                            marker.iconTintColor = lineColorArray[counter]
                            marker.map = naverMap               // 마커 표시
                        }
                        //폴리곤 설정 및 출력 코드
                        for (i in multiPolygonArray[indexOfLocationArray]) {
                            i.color = colorArray[counter]  // 폴리곤 내부 색깔 설정
                            i.outlineColor = lineColorArray[counter]  // 폴리곤 외곽선 색깔 설정
                            i.outlineWidth = 10 // 폴리곤 외곽선 굵기 설정
                            i.map = naverMap    // 폴리곤 표시
                        }

                    }
//                    Log.d("결과", "성공 : ${responseString?.results?.size} $requestCoord")
                }
                override fun onFailure(call: Call<ResultGetLocationJson>, t: Throwable) {
//                    Log.d("결과:", "실패 : $t")
                }
            })
        }
        naverMap.setOnMapLongClickListener { point, coord ->    // 맵을 길게 클릭 시 현재 있는 폴리곤 전부 삭제
            for (m in multiPolygonArray) {
                for (i in m) {
                    i.map = null    // 폴리곤 표시
                }
            }
            marker.map = null
//            counter = 0
            try {
                returnLocation(coord)   // 이걸로 위에 긴 거 실행
            }catch (e: Exception){
                e.printStackTrace()
            }

        }

        naverMap.setOnMapClickListener { point, coord ->    // 맵을 짧게 클릭 시 실행되는 함수
            for (m in multiPolygonArray) {
                for (i in m) {
                    i.map = null    // 폴리곤 표시
                }
            }
            marker.map = null
//            counter = 0
        }

        val showAll : FloatingActionButton = findViewById(R.id.floatingActionButton)
        var showAllcounter = true
        showAll.setOnClickListener {
            marker.map = null
            if (showAllcounter) {
                for (m in 0 until multiPolygonArray.size) {
                    counter = if (covidNumberArray[m][0].toInt() > 10000) {
                        3
                    } else if (covidNumberArray[m][0].toInt() > 5000) {
                        2
                    } else if (covidNumberArray[m][0].toInt() > 1000) {
                        1
                    } else {
                        0
                    }
                    for (i in multiPolygonArray[m]) {
                        i.color = colorArray[counter]  // 폴리곤 내부 색깔 설정
                        i.outlineColor = lineColorArray[counter]  // 폴리곤 외곽선 색깔 설정
                        i.outlineWidth = 5 // 폴리곤 외곽선 굵기 설정
                        i.map = naverMap    // 폴리곤 표시
                    }
                }
                // 마커 설정 및 출력 코드
                marker.position = LatLng(37.0,127.8)  // 마커 위경도 설정
                marker.captionText = "전국"   // 마커 텍스트 설정
                marker.setCaptionAligns(Align.Top)  // 마커 텍스트 위치
                marker.captionTextSize = 25f        // 마커 텍스트 크기
                marker.subCaptionText = "${returnDateString()} 확진자 : ${covidNumberArray[covidNumberArray.size - 1][0]}명" +
                        "\n${returnDateString()} 확진자 증가량 : \n내부발병 ${covidNumberArray[covidNumberArray.size - 1][1]}명" +
                        " + 외부유입 ${covidNumberArray[covidNumberArray.size - 1][2]}명 = " +
                        "총 ${covidNumberArray[covidNumberArray.size - 1][3]}명" +
                        "\n${returnDateString()} 격리자 : ${covidNumberArray[covidNumberArray.size - 1][4]}명" +
                        "\n${returnDateString()} 사망자  : ${covidNumberArray[covidNumberArray.size - 1][5]}명"
                marker.subCaptionColor = Color.DKGRAY
                marker.subCaptionTextSize = 17f
                marker.isHideCollidedSymbols = true
                marker.icon = MarkerIcons.BLACK
                marker.iconTintColor = lineColorArray[3]
                marker.map = naverMap
                showAllcounter = false

            }
            else {
                for (m in multiPolygonArray) {
                    for (i in m) {
                        i.map = null    // 폴리곤 표시
                    }
                }
                showAllcounter = true
                marker.map = null
            }
        }
        naverMap.locationTrackingMode = LocationTrackingMode.Follow //시작할 때 추적모드를 켜서 자동으로 현재 위치로 오게 함
    }

    companion object {  // 모름
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000   // 위치 요청 허가 값
    }

    inner class NetworkThread: Thread(){    // 스레드에서 코로나19 데이터를 공공데이터 api를 사용해서 읽어옴
        @SuppressLint("SimpleDateFormat")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {    // 날짜에 따른 코로나 19데이터를 읽어옴(오전에는 아직 당일 데이터가 안올라와서 어제 걸로 읽어옴)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1) //변경하고 싶은 원하는 날짜 수를 넣어 준다.
            val TimeToDate = calendar.time
            val formatter = SimpleDateFormat("yyyyMMdd") //날짜의 모양을 원하는 대로 변경 해 준다.
            val ampmformatter = SimpleDateFormat("aa")
            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val finalResultDate = formatter.format(TimeToDate)
            val ampm = ampmformatter.format(TimeToDate).toString()
            try {
                // 접속할 페이지의 주소
                var site = "http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19SidoInfStateJson?"
                if (ampm == "오전") {
                    site += "startCreateDt=${finalResultDate}&endCreateDt=${finalResultDate}"
                }
                val url = URL(site)
                val conn = url.openConnection() //공공코로나 API 키로 접속
                conn.setRequestProperty("serviceKey", "C/53dRPVlGwdFgAwBz0uNqX/B5COnUkL9cRSvJ01NqdIejOQZaHm/Ch30E5AnXC3DnhSI17+64HJa57wiJNvKg==")
                val input = conn.getInputStream()

                val factory = DocumentBuilderFactory.newInstance()
                val builder = factory.newDocumentBuilder()
                // doc: xml문서를 모두 읽어와서 분석을 끝냄
                val doc = builder.parse(input)

                // root: xml 문서의 모든 데이터들을 갖고 있는 객체
                val root = doc.documentElement

                // xml 문서에서 태그 이름이 item인 태그들이 item_node_list에 리스트로 담김
                val itemNodeList = root.getElementsByTagName("item")

                // item_node_list에 들어있는 태그 객체 수만큼 반복함
                for (name in covidLocationArray) {
                    for(i in 0 until itemNodeList.length){
                        // i번째 태그 객체를 item_element에 넣음
                        val itemElement = itemNodeList.item(i) as Element

                        // item태그 객체에서 원하는 데이터를 태그이름을 이용해서 데이터를 가져옴
                        // xml 문서는 태그 이름으로 데이터를 가져오면 무조건 리스트로 나옴
                        val gubunList = itemElement.getElementsByTagName("gubun")
                        val gubunNode = gubunList.item(0) as Element
                        val gubun = gubunNode.textContent
                        // 지역의 코로나 api 데이터 중 앱에 필요한 부분만 빼와서 리스트 형태로 저장
                        if (gubun == name) {
                            val defcntList = itemElement.getElementsByTagName("defCnt")
                            val defcntNode = defcntList.item(0) as Element
                            val defCnt = defcntNode.textContent

                            val deathcntList = itemElement.getElementsByTagName("deathCnt")
                            val deathcntNode = deathcntList.item(0) as Element
                            val deathCnt = deathcntNode.textContent

                            val incdecList = itemElement.getElementsByTagName("incDec")
                            val incdecNode = incdecList.item(0) as Element
                            val incDec = incdecNode.textContent

                            val isolingcntList = itemElement.getElementsByTagName("isolIngCnt")
                            val isolingcntNode = isolingcntList.item(0) as Element
                            val isolIngCnt = isolingcntNode.textContent

                            val outsideList = itemElement.getElementsByTagName("overFlowCnt")
                            val outsideNode = outsideList.item(0) as Element
                            val outside = outsideNode.textContent

                            val insideList = itemElement.getElementsByTagName("localOccCnt")
                            val insideNode = insideList.item(0) as Element
                            val inside = insideNode.textContent
                            // 리스트를 저장해서 다음에 쓸 수 있게 해줌
                            runOnUiThread {
                                val inputCovidArray : ArrayList<String> = arrayListOf()
                                inputCovidArray.add(defCnt)
                                inputCovidArray.add(inside)
                                inputCovidArray.add(outside)
                                inputCovidArray.add(incDec)
                                inputCovidArray.add(isolIngCnt)
                                inputCovidArray.add(deathCnt)
                                covidNumberArray.add(inputCovidArray)
                            }
                            break
                        }

                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
//            Toast.makeText(this@MobileMapActivityTest,covidNumberArray.size ,
//                Toast.LENGTH_SHORT).show()
        }
    }

    inner class MakeLocationArrayThread : Thread() {    // 스레드에서 행정구역의 좌표를 배열에 넣어 줌
        override fun run() {
            try {
                val am = resources.assets   // 에셋 폴더를 사용가능하게 해줌
                val inputStream= am.open("광역시도_변환.json")    // 해당 광역시도의 geojson 파일 열기
                val findName = "CTP_KOR_NM"     // 광역시도 이름 변수 명
                val jsonString = inputStream.bufferedReader().use { it.readText() } // 해당 파일에서 이름이랑 주소 좌표 읽어옴
                val jObject = JSONObject(jsonString)
                val jsonlist = jObject.getJSONArray("features")
                var jsonObjectCoordinatesList : ArrayList<String>
                val jsonRegex = "[\\[\\]]".toRegex()
                for (name in locationArray) {
                    for (i in 0 until jsonlist.length()) {  // geojson 파일에서 해당하는 좌표를 읽고 폴리곤을 만들어 폴리곤 리스트에 넣음
                        val jsonObject = jsonlist.getJSONObject(i)
                        val jsonObjectProperties = jsonObject.getJSONObject("properties")
                        val jsonObjectPropertiesNameKor = jsonObjectProperties.getString(findName)
                        val jsonObjectGeometry = jsonObject.getJSONObject("geometry")
                        if (name == jsonObjectPropertiesNameKor) {
                            if (jsonObjectGeometry.getString("coordinates").contains("]]],")) { // 해당 지역이 다수의 폴리곤으로 이루어진 경우
                                val multiPolyList: ArrayList<String> = jsonObjectGeometry.getString("coordinates").split("]]],") as ArrayList<String>
                                for (j in multiPolyList) {
                                    val jsonObjectCoordinates = jsonRegex.replace(j,"")
                                    jsonObjectCoordinatesList = jsonObjectCoordinates.split(",") as ArrayList<String>
                                    val jsonObjectLatLngList: ArrayList<LatLng> = arrayListOf<LatLng>()
                                    var index = 0
                                    while(index < jsonObjectCoordinatesList.size) {
                                        jsonObjectLatLngList.add(LatLng(jsonObjectCoordinatesList[index + 1].toDouble(),
                                            jsonObjectCoordinatesList[index].toDouble()))
                                        index += 2
                                    }
                                    val polygon = PolygonOverlay()
                                    polygon.coords = jsonObjectLatLngList
                                    if (name == "전라남도" && jsonObjectLatLngList.size > 1000) {  // 전남은 도 중 유일하게 광주로 인해 내부에 구멍이 있음
                                        val holeCoordinates = jsonRegex.replace(jsonObjectGeometry.getString("holes"),"")
                                        val holeCoordinatesList = holeCoordinates.split(",") as ArrayList<String>
                                        val holeLatLngList: ArrayList<LatLng> = arrayListOf<LatLng>()
                                        var holeIndex = 0
                                        while(holeIndex < holeCoordinatesList.size) {
                                            holeLatLngList.add(LatLng(holeCoordinatesList[holeIndex + 1].toDouble(),
                                                holeCoordinatesList[holeIndex].toDouble()))
                                            holeIndex += 2
                                        }
                                        polygon.holes = listOf(holeLatLngList) // 광주부분을 전남에서 빼줌
                                    }
                                    polygonArray.add(polygon)
                                }
                            }
                            else {  // 해당 지역이 폴리곤 1개로만 이루어진 경우
                                val jsonObjectCoordinates = jsonRegex.replace(jsonObjectGeometry.getString("coordinates"),"")
                                jsonObjectCoordinatesList = jsonObjectCoordinates.split(",") as ArrayList<String>
                                val jsonObjectLatLngList: ArrayList<LatLng> = arrayListOf<LatLng>()
                                var index = 0
                                while(index < jsonObjectCoordinatesList.size) {
                                    jsonObjectLatLngList.add(LatLng(jsonObjectCoordinatesList[index + 1].toDouble(),
                                        jsonObjectCoordinatesList[index].toDouble()))
                                    index += 2
                                }
                                val polygon = PolygonOverlay()
                                polygon.coords = jsonObjectLatLngList
                                polygonArray.add(polygon)
                            }
                        }
                    }
                    multiPolygonArray.add(polygonArray)
                    polygonArray = arrayListOf()
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun returnDateString() : String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 0) //변경하고 싶은 원하는 날짜 수를 넣어 준다.
        val timeToString = calendar.time
        val formatter = SimpleDateFormat("aa")
        formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val ampm = formatter.format(timeToString)
        return if (ampm == "오전") {
            "어제"
        } else {
            "오늘"
        }
    }
}
