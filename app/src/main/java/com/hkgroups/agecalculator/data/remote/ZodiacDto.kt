package com.hkgroups.agecalculator.data.remote

import com.google.gson.annotations.SerializedName

data class CompatibilityDto(
    @SerializedName("sign_name") val signName: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("description") val description: String
)

data class ZodiacSignDto(
    @SerializedName("name") val name: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("date_range") val dateRange: String,
    @SerializedName("personality_summary") val personality: String,
    @SerializedName("ruling_planet") val rulingPlanet: String,
    @SerializedName("element") val element: String,
    @SerializedName("strengths") val strengths: List<String>,
    @SerializedName("weaknesses") val weaknesses: List<String>,
    @SerializedName("compatibilities") val compatibilities: List<CompatibilityDto>
)