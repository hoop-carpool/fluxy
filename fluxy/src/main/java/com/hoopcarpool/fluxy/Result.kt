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

    fun conclude(): ConcludedResult<T>? =
        when (this) {
            is Success -> ConcludedResult.Success(value)
            is Failure -> ConcludedResult.Failure(exception, value)
            else -> null
        }

    fun doOnLoading(cb: (Loading<T>) -> Unit) {
        if (this is Loading) cb(this)
    }

    fun doOnSuccess(cb: (Success<T>) -> Unit) {
        if (this is Success) cb(this)
    }

    fun doOnFailure(cb: (Failure<T>) -> Unit) {
        if (this is Failure) cb(this)
    }

    fun doOnEmpty(cb: (Empty<T>) -> Unit) {
        if (this is Empty) cb(this)
    }

    fun ifLoading(cb: (Boolean) -> Unit) {
        cb(this is Loading)
    }

    fun ifSuccess(cb: (Boolean) -> Unit) {
        cb(this is Success)
    }

    fun ifFailure(cb: (Boolean) -> Unit) {
        cb(this is Failure)
    }

    fun ifEmpty(cb: (Boolean) -> Unit) {
        cb(this is Empty)
    }
}


/**
 * Models an async operation in a concluded state
 */
sealed class ConcludedResult<T> {
    data class Success<T>(val value: T) : ConcludedResult<T>()
    data class Failure<T>(val exception: Throwable? = null, val value: T? = null) : ConcludedResult<T>()
}
