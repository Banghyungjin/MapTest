package com.example.maptest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class WebMapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_map)

        val webMapView : WebView = findViewById(R.id.webView)
        webMapView.settings.javaScriptEnabled = true

        webMapView.loadUrl("file:///android_asset/navermapAPI.html")
    }

}