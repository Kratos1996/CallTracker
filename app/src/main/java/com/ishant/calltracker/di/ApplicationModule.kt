package com.ishant.calltracker.di
/*
* © Copyright Ishant Sharma
* Android Developer
* JAVA/KOTLIN
* +91-7732993378
* ishant.sharma1947@gmail.com
* */

import android.content.Context
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.google.gson.Gson
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.api.ApiInterface
import com.ishant.calltracker.data.ContactRepository
import com.ishant.calltracker.data.ContactRepositoryImpl
import com.ishant.calltracker.database.room.AppDB
import com.ishant.calltracker.database.room.CallTrackerDao
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.domain.ContactUseCaseImpl
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.TelephonyManagerPlus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    val BASE_URL = "https://wappblaster.in/api/"

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()


    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): CallTrackerApplication {
        return app as CallTrackerApplication
    }

    @Singleton
    @Provides
    fun getTelephoneManager(@ApplicationContext app: Context) = TelephonyManagerPlus(app)

    @Provides
    @Singleton
    fun getDeveloperName(): String = "©Copyright Ishant Sharma"

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDB {
        return Room.databaseBuilder(context, AppDB::class.java,"CallTrackerIshantDatabase").fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providesPostDao(db: AppDB): CallTrackerDao = db.getDao()

    @Provides
    fun providesDatabaseRepository(@ApplicationContext context: Context,db: AppDB):DatabaseRepository =
        DatabaseRepository(db,context)


    @Provides
    @Singleton
    fun provideChunkCollector(@ApplicationContext context: Context): ChuckerCollector {
        return ChuckerCollector(
            context = context,
            // Toggles visibility of the notification
            showNotification = true,
            // Allows to customize the retention period of collected data
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )
    }

    @Provides
    @Singleton
    fun provideChunkInterceptor(
        @ApplicationContext context: Context,
        collector: ChuckerCollector
    ): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .collector(collector)
            .maxContentLength(250000L)
            .redactHeaders("Auth-Token", "Bearer")
            .alwaysReadResponseBody(enable = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        interceptor: ChuckerInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(Interceptor { chain ->
                val newRequest = chain.request().newBuilder().addHeader(
                    "Authorization", "Bearer " + AppPreference.firebaseToken
                ).build()
                chain.proceed(newRequest)
            })
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }


    @Provides
    @Singleton
    fun provideApi(okHttpClient: OkHttpClient): ApiInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }

    @Provides
    fun provideContactRepository(api : ApiInterface):ContactRepository = ContactRepositoryImpl(api)

    @Provides
    fun provideContactUseCase(repository : ContactRepository):ContactUseCase = ContactUseCaseImpl(repository)
}