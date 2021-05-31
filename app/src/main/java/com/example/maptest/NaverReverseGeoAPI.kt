package com.example.maptest

import com.example.maptest.jsons.ResultGetLocationJson
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverReverseGeoAPI {
    @GET("map-reversegeocode/v2/gc?request=coordsToaddr" +
            "&sourcecrs=epsg:4326&output=json&orders=legalcode,admcode")    // legalcode = 법정동, admcode = 행정동 2개의 리스트 반환
    fun getLocation(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("coords") LatLang: String

    ): Call<ResultGetLocationJson>
}