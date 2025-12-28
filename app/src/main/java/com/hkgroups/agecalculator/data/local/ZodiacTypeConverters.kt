package com.hkgroups.agecalculator.data.local

import androidx.room.TypeConverter
import com.hkgroups.agecalculator.data.model.Compatibility
import org.json.JSONArray
import org.json.JSONObject

/**
 * Type converters for Room Database to handle complex types.
 * Converts List<String> and List<Compatibility> to JSON strings and vice versa.
 * Uses org.json for serialization to avoid Kotlin 2.1 type inference issues with Gson.
 */
class ZodiacTypeConverters {

    /**
     * Convert JSON string to List<String>
     */
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(value)
            List(jsonArray.length()) { i -> jsonArray.getString(i) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert List<String> to JSON string
     */
    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return if (list.isNullOrEmpty()) {
            "[]"
        } else {
            JSONArray(list).toString()
        }
    }

    /**
     * Convert JSON string to List<Compatibility>
     */
    @TypeConverter
    fun fromCompatibilityList(value: String?): List<Compatibility> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(value)
            List(jsonArray.length()) { i ->
                val jsonObject = jsonArray.getJSONObject(i)
                Compatibility(
                    signName = jsonObject.optString("signName", ""),
                    rating = jsonObject.optInt("rating", 0),
                    description = jsonObject.optString("description", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert List<Compatibility> to JSON string
     */
    @TypeConverter
    fun toCompatibilityList(list: List<Compatibility>?): String {
        return if (list.isNullOrEmpty()) {
            "[]"
        } else {
            try {
                val jsonArray = JSONArray()
                list.forEach { compat ->
                    val jsonObject = JSONObject()
                    jsonObject.put("signName", compat.signName)
                    jsonObject.put("rating", compat.rating)
                    jsonObject.put("description", compat.description)
                    jsonArray.put(jsonObject)
                }
                jsonArray.toString()
            } catch (e: Exception) {
                "[]"
            }
        }
    }
}
