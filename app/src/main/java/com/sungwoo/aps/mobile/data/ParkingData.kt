package com.sungwoo.aps.mobile.data

import com.sungwoo.aps.mobile.data.GPSData

/**
 * Created by localadmin on 8/17/2017.
 */
data class ParkingData(
        val id:Int,
        val area:String?,
        val status:String?,
        val gps1: GPSData?,
        val gps2: GPSData?,
        val gps3: GPSData?,
        val gps4: GPSData?
)