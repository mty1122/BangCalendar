package com.mty.bangcalendar.di

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class ActivityModule {
    @Binds
    abstract fun bindLifecycleOwner(activity: FragmentActivity): LifecycleOwner
}