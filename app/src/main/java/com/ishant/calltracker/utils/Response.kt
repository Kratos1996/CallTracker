package com.ishant.calltracker.utils

import com.ishant.calltracker.network.Resource

sealed class Response<out N> {
    class Success<out R>(val response: R?) : Response<R>()
    class Message(val message: String?) : Response<Nothing>()
    class Loading(val isLoading: Boolean) : Response<Nothing>()
    object Empty : Response<Nothing>()
}