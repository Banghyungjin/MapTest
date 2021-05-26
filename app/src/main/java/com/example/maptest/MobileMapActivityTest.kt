package com.example.maptest

import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PolygonOverlay
import com.naver.maps.map.util.FusedLocationSource
import org.json.JSONObject
import kotlin.math.roundToInt


class MobileMapActivityTest : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource    //주소 받아오는 거
    private lateinit var naverMap: NaverMap                     //네이버 맵 객체


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_map)

        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
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
        this.naverMap = naverMap
        val am = resources.assets
        val geocoder = Geocoder(this)
        val uiSettings = naverMap.uiSettings
//        val projection = naverMap.projection
        var multiPolygonArray : ArrayList<PolygonOverlay> = arrayListOf<PolygonOverlay>()
//        var counter = true
        naverMap.locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        uiSettings.isLocationButtonEnabled = true
        naverMap.isIndoorEnabled = true
        naverMap.buildingHeight = 0.5f
        naverMap.setOnSymbolClickListener { symbol ->
            Toast.makeText(this, symbol.caption + "\n위도 = " +
                    (symbol.position.latitude * 100).roundToInt() / 100f +
                    "\n경도 = " + (symbol.position.longitude * 100).roundToInt() / 100f,
                Toast.LENGTH_SHORT).show()
            // 밑에서 true로 하면 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
            false
        }
        naverMap.setOnMapLongClickListener { point, coord ->
            for (i in multiPolygonArray) {
                i.map = null
            }
            multiPolygonArray = arrayListOf<PolygonOverlay>()
        }
        naverMap.setOnMapClickListener { point, coord ->
            if (multiPolygonArray.size != 0) {
                for (i in multiPolygonArray) {
                    i.map = null
                }
                multiPolygonArray = arrayListOf<PolygonOverlay>()
            }

//            val metersPerPixel = projection.metersPerPixel
            val address = geocoder.getFromLocation(coord.latitude, coord.longitude,1)[0]
            val addressRegex = "[0-9-]".toRegex()
            val regexedAddress : String = addressRegex.replace(address.getAddressLine(0),"")
            val regexedAddressList : ArrayList<String> = regexedAddress.split(" ") as ArrayList<String>
            if (regexedAddressList.size > 1) {
                val nationString : String = regexedAddressList[0]
                if (nationString == "대한민국") {
                    val mapString : String = regexedAddressList[1]
                    val inputStream= am.open("${mapString}_변환.json")
                    val findName = "CTP_KOR_NM"
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    val jObject = JSONObject(jsonString)
                    val jsonlist = jObject.getJSONArray("features")
                    var jsonObjectCoordinatesList : ArrayList<String>
                    regexedAddressList.removeAt(0)
                    if (regexedAddressList[regexedAddressList.size - 1].isEmpty()) {
                        regexedAddressList.removeAt(regexedAddressList.size - 1)
                    }

                    if (mapString.contains("[시도]".toRegex())) {
                        for (i in 0 until jsonlist.length()) {
                            val jsonObject = jsonlist.getJSONObject(i)
                            val jsonObjectProperties = jsonObject.getJSONObject("properties")
                            val jsonObjectPropertiesNameKor = jsonObjectProperties.getString(findName)
                            val jsonObjectGeometry = jsonObject.getJSONObject("geometry")
                            val jsonRegex = "[\\[\\]]".toRegex()
                            if (mapString == jsonObjectPropertiesNameKor) {
                                if (jsonObjectGeometry.getString("coordinates").contains("]]],")) {
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
                                        if (mapString == "전라남도" && jsonObjectLatLngList.size > 1000) {
                                            val holeCoordinates = jsonRegex.replace(jsonObjectGeometry.getString("holes"),"")
                                            val holeCoordinatesList = holeCoordinates.split(",") as ArrayList<String>
                                            val holeLatLngList: ArrayList<LatLng> = arrayListOf<LatLng>()
                                            var holeIndex = 0
                                            while(holeIndex < holeCoordinatesList.size) {
                                                holeLatLngList.add(LatLng(holeCoordinatesList[holeIndex + 1].toDouble(),
                                                    holeCoordinatesList[holeIndex].toDouble()))
                                                holeIndex += 2
                                            }
                                            polygon.holes = listOf(holeLatLngList)
                                        }
                                        multiPolygonArray.add(polygon)
                                    }
                                }
                                else {
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
                                    multiPolygonArray.add(polygon)
                                }
                                break
                            }
                        }
                        Toast.makeText(this, mapString, Toast.LENGTH_SHORT).show()
                        for (i in multiPolygonArray) {
                            i.color = Color.argb(80, 65, 105, 225)
                            i.outlineColor = Color.rgb(65,105,225)
                            i.outlineWidth = 10
                            i.map = naverMap
                        }
                    }
                }
                }


        }
        naverMap.locationTrackingMode = LocationTrackingMode.Follow //시작할 때 추적모드를 켜서 자동으로 현재 위치로 오게 함
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}