package com.example.kmptemplate.shared.uimodel.di

import com.example.kmptemplate.shared.uimodel.MockConfig
import kotlin.native.concurrent.ThreadLocal

internal expect fun createAppComponent(): AppComponent

@ThreadLocal
object IosDependencies {
    private val component: AppComponent =
        if (MockConfig.isEnabled) MockAppComponent() else createAppComponent()

    val provider: IosViewModelProvider = IosViewModelProvider(component)
}
