package com.hoopcarpool.fluxy

/**
 * Models an async operation
 */
sealed class Result<T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Loading<T>(val value: T? = null) : Result<T>()
    data class Failure<T>(val exception: Throwable? = null, val value: T? = null) : Result<T>()
    class Empty<T> : Result<T>() {
        override fun toString() = "Empty()"
    }

    /**
     * Return if current result is in a completed state
     */
    fun hasConcluded() = this is Success || this is Failure
}
