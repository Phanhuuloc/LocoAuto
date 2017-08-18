package com.sungwoo.aps.mobile.api.http

import retrofit2.Call
import javax.inject.Inject

/**
 * Created by localadmin on 8/17/2017.
 */
class RestApi @Inject constructor(private val sungwooApi: SungwooApi) {
    fun reqParkingLotInfo(): Call<ParkingLotRes> {
        return sungwooApi.reqParkingLotInfo()
    }

    fun reqParkingLotInfo(lot: Int): Call<ParkingRes> {
        return sungwooApi.reqParking(lot)
    }

    fun reqCar(lot: Int): Call<CarRes> {
        return sungwooApi.reqCar(lot)
    }
}