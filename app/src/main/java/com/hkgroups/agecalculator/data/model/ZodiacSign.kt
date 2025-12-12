package com.hkgroups.agecalculator.data.model


data class ZodiacSign(
    val name: String,
    val symbol: String,
    val dateRange: String,
    val personality: String,
    val compatibilities: List<Compatibility>,
    val rulingPlanet: String,
    val element: String,
    val strengths: List<String>,
    val weaknesses: List<String>
)