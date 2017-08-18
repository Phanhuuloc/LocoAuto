package com.sungwoo.aps.mobile.di

import com.sungwoo.aps.mobile.api.http.RestApi
import com.sungwoo.aps.mobile.api.http.SungwooApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Created by localadmin on 8/17/2017.
 */
@Module
class LocoAutoModule() {
    @Provides
    @Singleton
    fun provideNewsAPI(sungwooApi: SungwooApi): RestApi = RestApi(sungwooApi)

    @Provides
    @Singleton
    fun provideRedditApi(retrofit: Retrofit): SungwooApi = retrofit.create(SungwooApi::class.java)

}