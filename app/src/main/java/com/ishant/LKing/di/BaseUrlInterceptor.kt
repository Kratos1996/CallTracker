package com.ishant.LKing.di

import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlInterceptor(private var baseUrl: String) : Interceptor {

    fun setBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val modifiedUrl = originalRequest.url.newBuilder()
            .scheme("https") // Set your scheme (http/https)
            .host(baseUrl)
            .build()
        val modifiedRequest = originalRequest.newBuilder()
            .url(modifiedUrl)
            .build()
        return chain.proceed(modifiedRequest)
    }
}