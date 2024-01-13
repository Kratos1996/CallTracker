package com.ishant.calltracker.network


import com.google.gson.Gson
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

suspend fun <T>  FlowCollector<Resource<T>>.catchExceptions(e: Exception, gson:Gson)  {

        when(e) {
            is HttpException -> {
                try {
                    val errorObj = e.response()?.errorBody()?.charStream()?.readText()
                        ?.let { JSONObject(it) }
                    when (e.code()) {

                        else -> emit(Resource.Error<T>(e.message()))
                    }
                }catch (e:Exception){
                    emit(Resource.Error<T>("Something went wronge"))
                }
            }
            is IOException -> {
                emit(Resource.Error<T>("No Internet Connection"))
            }
            else -> {
                emit(Resource.Error<T>("Something went wronge"))
            }
        }

}

suspend fun <T>  Any.catchExceptions(e: Exception, gson:Gson) = flow  {

    when(e) {
        is HttpException -> {
            try {
                val errorObj = e.response()?.errorBody()?.charStream()?.readText()
                    ?.let { JSONObject(it) }
                when (e.code()) {

                    else -> emit(Resource.Error<T>(e.message()))
                }
            }catch (e:Exception){
                emit(Resource.Error<T>("Something went wronge"))
            }
        }
        is IOException -> {
            emit(Resource.Error<T>("No Internet Connection"))
        }
        else -> {
            emit(Resource.Error<T>("Something went wronge"))
        }
    }

}