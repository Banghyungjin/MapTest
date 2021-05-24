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
import com.naver.maps.map.util.FusedLocationSource
import org.json.JSONObject
import kotlin.math.roundToInt


class MobileMapActivity : AppCompatActivity(), OnMapReadyCallback {

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
        val inputStream= am.open("TL_SCCO_CTPRVN.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jObject = JSONObject(jsonString)
        val jsonlist = jObject.getJSONArray("features")
        val geocoder = Geocoder(this)
        val uiSettings = naverMap.uiSettings
        val projection = naverMap.projection
        //val circle = CircleOverlay()
        val polygon = PolygonOverlay()

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
            polygon.map = null
        }
        naverMap.setOnMapLongClickListener { point, coord ->
            val metersPerPixel = projection.metersPerPixel
            val address = geocoder.getFromLocation(coord.latitude, coord.longitude,1)[0]
            val addressRegex = "[0-9-]".toRegex()
            val regexedAddress : String = addressRegex.replace(address.getAddressLine(0),"")
            val regexedAddressList : ArrayList<String> = regexedAddress.split(" ") as ArrayList<String>
            regexedAddressList.removeAt(0)
            if (regexedAddressList[regexedAddressList.size - 1].isEmpty()) {
                regexedAddressList.removeAt(regexedAddressList.size - 1)
            }
            val mapString = if (metersPerPixel < 5) {
                regexedAddressList[0] + " " + regexedAddressList[1] + " " + regexedAddressList[2]
            } else if (metersPerPixel < 40) {
                regexedAddressList[0] + " " + regexedAddressList[1]
            } else {
                regexedAddressList[0]
            }

            val jsonObjectCoordinatesList : ArrayList<String>
            for (i in 0 until jsonlist.length()) {
                val jsonObject = jsonlist.getJSONObject(i)
                val jsonObjectProperties = jsonObject.getJSONObject("properties")
                val jsonObjectPropertiesNameKor = jsonObjectProperties.getString("CTP_KOR_NM")
                val jsonObjectGeometry = jsonObject.getJSONObject("geometry")
                if (regexedAddressList[0] == jsonObjectPropertiesNameKor) {
                    var jsonObjectCoordinates = jsonObjectGeometry.getString("coordinates")
                    val jsonRegex = "[\\[\\]]".toRegex()
                    jsonObjectCoordinates = jsonRegex.replace(jsonObjectCoordinates,"")
                    jsonObjectCoordinatesList = jsonObjectCoordinates.split(",") as ArrayList<String>
                    val jsonObjectLatLngList: ArrayList<LatLng> = arrayListOf<LatLng>()
                    var index = 0
                    while(index < jsonObjectCoordinatesList.size) {
                        jsonObjectLatLngList.add(LatLng(jsonObjectCoordinatesList[i].toDouble(),
                            jsonObjectCoordinatesList[i + 1].toDouble()))
                        index += 2
                    }
                    polygon.coords = jsonObjectLatLngList
                    Toast.makeText(this,
                        "${(coord.latitude * 1000).roundToInt() / 1000f}\n" +
                                "${(coord.longitude * 1000).roundToInt() / 1000f}" +
                                "\n$mapString\n$jsonObjectPropertiesNameKor" +
                                "\nindex = $index\nsize = ${jsonObjectLatLngList.size}"
                        , Toast.LENGTH_SHORT).show()
                    //circle.center = LatLng(coord.latitude, coord.longitude)
                    break
                }
            }

            //Toast.makeText(this, final_text, Toast.LENGTH_LONG).show()

            //circle.radius = metersPerPixel * 300
            //circle.color = Color.argb(50, 65, 105, 225)
            //circle.outlineColor = Color.argb(100,25, 50, 102)
            //circle.outlineWidth = 5
            //circle.map = naverMap
            //polygon.color = Color.argb(50, 65, 105, 225)
            polygon.map = naverMap
        }
        naverMap.locationTrackingMode = LocationTrackingMode.Follow //시작할 때 추적모드를 켜서 자동으로 현재 위치로 오게 함
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}