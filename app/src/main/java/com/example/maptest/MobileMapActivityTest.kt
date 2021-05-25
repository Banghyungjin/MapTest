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
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.PolygonOverlay
import com.naver.maps.map.overlay.PolylineOverlay
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
        val projection = naverMap.projection

        //val circle = CircleOverlay()

        val polygon2 = PolygonOverlay()
        var multiPolygonArray : ArrayList<PolygonOverlay> = arrayListOf<PolygonOverlay>()
        //val polyline = PolylineOverlay()

        naverMap.locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        uiSettings.isLocationButtonEnabled = true
        naverMap.isIndoorEnabled = true
        naverMap.buildingHeight = 0.5f
        naverMap.setOnSymbolClickListener { symbol ->
            Toast.makeText(this, symbol.caption + "\n위도 = " +
                    (symbol.position.latitude * 100).roundToInt() / 100f +
                    "\n경도 = " + (symbol.position.longitude * 100).roundToInt() / 100f,
                Toast.LENGTH_SHORT).show()
            // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
            true
        }
        naverMap.setOnMapClickListener{ point, coord ->
            //circle.map = null

            for (i in multiPolygonArray) {
                i.map = null
            }
            multiPolygonArray = arrayListOf<PolygonOverlay>()
        }
        naverMap.setOnMapLongClickListener { point, coord ->
            if (multiPolygonArray.size != 0) {
                for (i in multiPolygonArray) {
                    i.map = null
                }
                multiPolygonArray = arrayListOf<PolygonOverlay>()
            }
            val metersPerPixel = projection.metersPerPixel
            val address = geocoder.getFromLocation(coord.latitude, coord.longitude,1)[0]
            val addressRegex = "[0-9-]".toRegex()
            val regexedAddress : String = addressRegex.replace(address.getAddressLine(0),"")
            val regexedAddressList : ArrayList<String> = regexedAddress.split(" ") as ArrayList<String>
            var mapString : String = regexedAddressList[1]
            var inputStream= am.open("${mapString}_변환.json")
            var findName : String = "CTP_KOR_NM"

            regexedAddressList.removeAt(0)
            if (regexedAddressList[regexedAddressList.size - 1].isEmpty()) {
                regexedAddressList.removeAt(regexedAddressList.size - 1)
            }

            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jObject = JSONObject(jsonString)
            val jsonlist = jObject.getJSONArray("features")

            var jsonObjectCoordinatesList : ArrayList<String>
            for (i in 0 until jsonlist.length()) {
                val jsonObject = jsonlist.getJSONObject(i)
                val jsonObjectProperties = jsonObject.getJSONObject("properties")
                val jsonObjectPropertiesNameKor = jsonObjectProperties.getString("$findName")
                val jsonObjectGeometry = jsonObject.getJSONObject("geometry")
                if (mapString == jsonObjectPropertiesNameKor) {
                    val multiPolyList: ArrayList<String> = jsonObjectGeometry.getString("coordinates").split("]]],") as ArrayList<String>
                    for (i in multiPolyList) {
//                        var jsonObjectCoordinates = jsonObjectGeometry.getString("coordinates")
                        val jsonRegex = "[\\[\\]]".toRegex()
                        var jsonObjectCoordinates = jsonRegex.replace(i,"")
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
//                    Toast.makeText(this, multiPolygonArray.size.toString(), Toast.LENGTH_LONG).show()
//                    Toast.makeText(this,
//                        "lat = ${(coord.latitude * 1000).roundToInt() / 1000f}\n" +
//                                "long = ${(coord.longitude * 1000).roundToInt() / 1000f}\n$mapString"
//                        , Toast.LENGTH_SHORT).show()
//                    //circle.center = LatLng(coord.latitude, coord.longitude)
                    break
                }
            }

            Toast.makeText(this, mapString, Toast.LENGTH_LONG).show()

            //circle.radius = metersPerPixel * 300
            //circle.color = Color.argb(50, 65, 105, 225)
            //circle.outlineColor = Color.argb(100,25, 50, 102)
            //circle.outlineWidth = 5
            //circle.map = naverMap
            for (i in multiPolygonArray) {
                i.color = Color.argb(80, 65, 105, 225)
                i.map = naverMap
            }

        }
        naverMap.locationTrackingMode = LocationTrackingMode.Follow //시작할 때 추적모드를 켜서 자동으로 현재 위치로 오게 함
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}