package com.sungwoo.aps.mobile

import android.app.Application
import com.sungwoo.aps.mobile.di.AppModule
import com.sungwoo.aps.mobile.di.DaggerLocoAutoComponent
import com.sungwoo.aps.mobile.di.LocoAutoComponent

/**
 * Created by localadmin on 8/16/2017.
 */

class LocoAutoApp : Application(){
    companion object {
        lateinit var locoAutoComponent: LocoAutoComponent
    }

    override fun onCreate() {
        super.onCreate()
        locoAutoComponent = DaggerLocoAutoComponent.builder()
                .appModule(AppModule(this))
                //.newsModule(NewsModule()) Module with empty constructor is implicitly created by dagger.
                .build()
    }
}
