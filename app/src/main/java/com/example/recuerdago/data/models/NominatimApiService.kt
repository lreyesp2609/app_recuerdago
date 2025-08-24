package com.example.recuerdago.data.models

import retrofit2.http.GET
import retrofit2.http.Query

data class NominatimResponse(
    val display_name: String?,
    val lat: String?,
    val lon: String?
)

interface NominatimApiService {
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("format") format: String = "json",
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("addressdetails") addressdetails: Int = 1
    ): NominatimResponse
}
