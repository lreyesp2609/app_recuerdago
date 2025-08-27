package com.example.recuerdago.network

import com.example.recuerdago.data.models.DirectionsRequest
import com.example.recuerdago.data.models.DirectionsResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ORSService {
    @Headers(
        "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8",
        "Content-Type: application/json; charset=utf-8"
    )
    @POST("/v2/directions/{profile}")
    suspend fun getRoute(
        @Path("profile") profile: String,
        @Body body: DirectionsRequest
    ): DirectionsResponse
}
