package com.example.kmptemplate.shared.uimodel.di

import com.example.kmptemplate.shared.data.repository.LoginRepositoryImpl
import com.example.kmptemplate.shared.data.repository.PostRepositoryImpl
import com.example.kmptemplate.shared.domain.repository.LoginRepository
import com.example.kmptemplate.shared.domain.repository.PostRepository
import com.example.kmptemplate.shared.network.api.PostApi
import com.example.kmptemplate.shared.network.buildHttpClient
import com.example.kmptemplate.shared.uimodel.HomeViewModel
import com.example.kmptemplate.shared.uimodel.LoginViewModel
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class Singleton

@Singleton
@Component
abstract class AppComponent {

    // LoginViewModel はシングルトン（アプリ全体で共有）
    abstract val loginViewModel: LoginViewModel

    // HomeViewModel は呼び出しのたびに新インスタンスを生成
    abstract fun homeViewModel(): HomeViewModel

    @Singleton
    @Provides
    fun provideSettings(): Settings = Settings()

    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient = buildHttpClient()

    @Provides
    fun provideLoginRepository(impl: LoginRepositoryImpl): LoginRepository = impl

    @Provides
    fun providePostRepository(impl: PostRepositoryImpl): PostRepository = impl
}
