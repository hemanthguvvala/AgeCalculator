package com.hkgroups.agecalculator.util

/**
 * A generic wrapper class for handling different states of data loading.
 * 
 * This sealed class provides a consistent way to represent:
 * - Loading states (with optional cached data)
 * - Success states (with the loaded data)
 * - Error states (with error message and optional cached data for offline mode)
 *
 * @param T The type of data being wrapped
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    /**
     * Represents a successful data loading state.
     * @param data The successfully loaded data
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Represents an error state during data loading.
     * @param message The error message describing what went wrong
     * @param data Optional cached data that can be displayed even when an error occurs
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    /**
     * Represents a loading state.
     * @param data Optional cached data that can be displayed while loading
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
