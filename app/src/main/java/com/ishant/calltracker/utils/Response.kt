package com.ishant.calltracker.utils

sealed class Response<out N> {
    class Success<out R>(val response: R?) : Response<R>()
    class Message(val message: String?) : Response<Nothing>()
    class Loading(val isLoading: Boolean) : Response<Nothing>()
    object Empty : Response<Nothing>()
}

sealed class DBResponse<out N> {
    class Success<out R>(val response: R?) : DBResponse<R>()
    class Message(val message: String?) : DBResponse<Nothing>()
    class Loading(val isLoading: Boolean) : DBResponse<Nothing>()
    object Empty : DBResponse<Nothing>()
}