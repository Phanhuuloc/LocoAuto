package com.sungwoo.aps.mobile.api.http

import com.sungwoo.aps.mobile.data.ParkingData
import java.util.ArrayList

/**
 * Created by localadmin on 8/17/2017.
 */
class CarRes(
        val v1: String?,
        val v2: String?,
        val parkingList: ArrayList<ParkingData>
)