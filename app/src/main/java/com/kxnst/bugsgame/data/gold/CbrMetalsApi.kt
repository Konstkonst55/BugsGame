package com.kxnst.bugsgame.data.gold

import retrofit2.http.GET
import retrofit2.http.Query

interface CbrMetalsApi {
    @GET("scripts/xml_metall.asp")
    suspend fun getMetalsQuotes(
        @Query("date_req1") startDate: String,
        @Query("date_req2") endDate: String
    ): String
}
