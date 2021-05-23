package com.example.maptest

import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.PolygonOverlay
import com.naver.maps.map.util.FusedLocationSource
import org.json.JSONObject
import java.io.IOException
import kotlin.math.roundToInt

class MobileMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

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
        val clientId = "0l2mcc3fx2"
        val clientPw = "vRz8M0yRPc1QRNU1KGwcJIDXslLcOSjhmg0t9kfk"
        val geocoder = Geocoder(this)
        val uiSettings = naverMap.uiSettings
        val projection = naverMap.projection

        this.naverMap = naverMap

        var circle = CircleOverlay()

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
            circle.map = null
        }
        naverMap.setOnMapLongClickListener { point, coord ->
            val metersPerPixel = projection.metersPerPixel
            var address = geocoder.getFromLocation(coord.latitude, coord.longitude,1).get(0)
            var addressline = "[0-9-]".toRegex()
            var addressline2 : String = addressline.replace(address.getAddressLine(0),"")
            var addrlist : ArrayList<String> = addressline2.split(" ") as ArrayList<String>
            addrlist.removeAt(0)
            if (addrlist[addrlist.size - 1].isEmpty()) {
                addrlist.removeAt(addrlist.size - 1)
            }
            var mapString = if (metersPerPixel < 5) {
                addrlist[0] + " " + addrlist[1] + " " + addrlist[2]
            } else if (metersPerPixel < 40) {
                addrlist[0] + " " + addrlist[1]
            } else {
                addrlist[0]
            }
            Toast.makeText(this, "${(coord.latitude*1000).roundToInt()/1000f}\n ${(coord.longitude*1000).roundToInt()/1000f}\n $mapString"
                ,Toast.LENGTH_SHORT).show()
            circle.center = LatLng(coord.latitude, coord.longitude)
            circle.radius = metersPerPixel * 300
            circle.color = Color.argb(50, 65, 105, 225)
            circle.outlineColor = Color.argb(100,25, 50, 102)
            circle.outlineWidth = 5
            circle.map = naverMap

        }
        naverMap.locationTrackingMode = LocationTrackingMode.Follow //시작할 때 추적모드를 켜서 자동으로 현재 위치로 오게 함
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}