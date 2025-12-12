package com.hkgroups.agecalculator.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ZodiacApiService {

    // This defines a call to get a list of all sign names
    @GET("signs")
    suspend fun getAllSignNames(): List<String>

    // This defines a call to get the full details for a specific sign
    @GET("signs/{signName}")
    suspend fun getZodiacSignDetails(@Path("signName") signName: String): ZodiacSignDto
}