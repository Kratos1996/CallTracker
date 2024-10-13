package com.ishant.jagtap.di

import android.app.Activity
import android.app.Dialog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object FragmentModule {

    @Provides
    fun provideProgressBar(activity: Activity): Dialog = Dialog(activity)
}