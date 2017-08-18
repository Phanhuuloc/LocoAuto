package com.sungwoo.aps.mobile.api.http

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by localadmin on 8/17/2017.
 */
interface SungwooApi {
    /**
     * Request parking infomation
     */
    @GET("/rpli")
    fun reqParkingLotInfo(): Call<ParkingLotRes>

    /**
     * Request parking
     */
    @POST("/rp")
    fun reqParking(@Query("lot") lot: Int): Call<ParkingRes>

    /**
     * Request car
     */
    @POST("/rc")
    fun reqCar(@Query("lot") lot: Int): Call<CarRes>
}

