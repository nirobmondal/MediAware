package com.example.mediaware.core.common.result

sealed interface Resource<out T> {
    data class Success<out T>(val data: T, val isFromCache: Boolean = false) : Resource<T>
    data class Error(val messageBn: String, val cause: Throwable? = null) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}
