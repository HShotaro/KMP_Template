package com.example.kmptemplate

import android.app.Application
import com.example.kmptemplate.shared.uimodel.AppContextHolder
import com.example.kmptemplate.shared.uimodel.di.AppComponent
import com.example.kmptemplate.shared.uimodel.di.MockAppComponent
import com.example.kmptemplate.shared.uimodel.di.create

class KmpApp : Application() {
    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        AppContextHolder.appContext = applicationContext
        component = if (BuildConfig.MOCK_MODE) MockAppComponent() else AppComponent::class.create()
    }
}
