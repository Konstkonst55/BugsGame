package com.kxnst.bugsgame.data.gold

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

import java.util.concurrent.TimeUnit

object CbrGoldPriceNetworkFactory {
    fun createApi(): CbrMetalsApi {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(7, TimeUnit.SECONDS)
            .callTimeout(8, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .client(httpClient)
            .baseUrl(CBR_BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(CbrMetalsApi::class.java)
    }

    private const val CBR_BASE_URL = "https://www.cbr.ru/"
}
