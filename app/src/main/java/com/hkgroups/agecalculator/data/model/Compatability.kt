package com.hkgroups.agecalculator.data.model

data class Compatibility(
    val signName: String, // The sign to compare with
    val rating: Int, // e.g., 1 to 5 stars
    val description: String
)