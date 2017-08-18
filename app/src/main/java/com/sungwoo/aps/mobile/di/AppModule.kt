package com.sungwoo.aps.mobile.di

import android.app.Application
import android.content.Context
import com.sungwoo.aps.mobile.LocoAutoApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by localadmin on 8/17/2017.
 */

@Module
class AppModule(val app: LocoAutoApp) {

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideApplication() : Application = app

}