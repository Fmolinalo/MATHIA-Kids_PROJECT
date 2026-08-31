package com.example.mathia

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MathiaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
