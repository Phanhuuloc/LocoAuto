package com.sungwoo.aps.mobile.di

import dagger.Component
import javax.inject.Singleton

/**
 * Created by localadmin on 8/17/2017.
 */
@Singleton
@Component(modules = arrayOf(
        AppModule::class,
        LocoAutoModule::class,
        NetworkModule::class)
)
interface LocoAutoComponent {


}