package com.example.maptest

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
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
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class MobileMapActivityTest : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource    // 주소 받아오는 거
    private lateinit var naverMap: NaverMap                     // 네이버 맵 객체
    private val CLIENT_ID = "0l2mcc3fx2"
    private val CLIENT_SECRET = "vRz8M0yRPc1QRNU1KGwcJIDXslLcOSjhmg0t9kfk"
    private val BASE_URL_NAVER_API = "https://naveropenapi.apigw.ntruss.com/"

    private val lineColorArray : ArrayList<Int> = arrayListOf(Color.rgb(244, 0, 0),
        Color.rgb(247, 119, 0), Color.rgb(244, 226, 0), Color.rgb(0, 206, 37),
        Color.rgb(65, 105, 225), Color.rgb(123, 0, 225))

    private val colorArray : ArrayList<Int> = arrayListOf(Color.argb(80, 244, 0, 0),
        Color.argb(80,247, 119, 0), Color.argb(80,244, 226, 0), Color.argb(80,0, 206, 37),
        Color.argb(80,65, 105, 225), Color.argb(80,123, 0, 225))

    private var counter = 0

    private val locationArray : ArrayList<String> = arrayListOf("강원도","경기도","경상남도","경상북도",
        "광주광역시","대구광역시","대전광역시","부산광역시","서울특별시","세종특별자치시","울산광역시",
        "인천광역시","전라남도","전라북도","제주특별자치도","충청남도","충청북도")

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
            var responseString: ResultGetLocationJson?
            val retrofit = Retrofit.Builder()
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
                        val mapCenterCoordLongi = responseString?.results?.get(0)?.region?.area1?.coords?.center?.x  // 선택한 지역 중심지 경도
                        val mapCenterCoordLati = responseString?.results?.get(0)?.region?.area1?.coords?.center?.y  // 선택한 지역 중심지 위도
                        indexOfLocationArray = locationArray.indexOf(mapString)
                        if (mapCenterCoordLati != null && mapCenterCoordLongi != null) {    // 선택한 지역의 중심 좌표에 마커를 생성
                            marker.position = LatLng(mapCenterCoordLati.toDouble(),mapCenterCoordLongi.toDouble())  // 마커 위경도 설정
                            marker.captionText = "$mapString"   // 마커 텍스트 설정
                            marker.setCaptionAligns(Align.Top)  // 마커 텍스트 위치
                            marker.captionTextSize = 20f        // 마커 텍스트 크기
                            marker.subCaptionText = "북위 : $mapCenterCoordLati\n동경 : $mapCenterCoordLongi"
                            marker.subCaptionColor = Color.DKGRAY
                            marker.subCaptionTextSize = 15f
                            marker.map = naverMap               // 마커 표시
                        }
                        //Log.d("결과", "인덱스 : $indexOfLocationArray")
                        for (i in multiPolygonArray[indexOfLocationArray]) {
                            i.color = Color.argb(80,65,105,225)  // 폴리곤 내부 색깔 설정
                            i.outlineColor = Color.rgb(65,105,225)  // 폴리곤 외곽선 색깔 설정
                            i.outlineWidth = 10 // 폴리곤 외곽선 굵기 설정
                            i.map = naverMap    // 폴리곤 표시
                        }
                        counter ++
                        counter %= colorArray.size
                    }
                    Log.d("결과", "성공 : ${responseString?.results?.size} $requestCoord")
                }
                override fun onFailure(call: Call<ResultGetLocationJson>, t: Throwable) {
                    Log.d("결과:", "실패 : $t")
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
            returnLocation(coord)   // 이걸로 위에 긴 거 실행
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
            if (showAllcounter) {
                for (m in multiPolygonArray) {
                    for (i in m) {
                        i.color = Color.argb(80,65,105,225)  // 폴리곤 내부 색깔 설정
                        i.outlineColor = Color.rgb(65,105,225)  // 폴리곤 외곽선 색깔 설정  // 폴리곤 외곽선 색깔 설정
                        i.outlineWidth = 10 // 폴리곤 외곽선 굵기 설정
                        i.map = naverMap    // 폴리곤 표시
                    }
//                    counter ++
//                    counter %= colorArray.size
                }
                showAllcounter = false
//                counter = 0
            }
            else {
                for (m in multiPolygonArray) {
                    for (i in m) {
                        i.map = null    // 폴리곤 표시
                    }
                }
                marker.map = null
                showAllcounter = true
//                counter = 0
            }
        }

        naverMap.locationTrackingMode = LocationTrackingMode.Follow //시작할 때 추적모드를 켜서 자동으로 현재 위치로 오게 함
    }

    companion object {  // 모름
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000   // 위치 요청 허가 값
    }



}