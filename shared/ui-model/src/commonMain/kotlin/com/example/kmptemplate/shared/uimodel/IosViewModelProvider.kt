package com.example.kmptemplate.shared.uimodel.di

import com.example.kmptemplate.shared.uimodel.HomeViewModel
import com.example.kmptemplate.shared.uimodel.LoginViewModel

class IosViewModelProvider(private val component: AppComponent) {
    // シングルトン
    val loginViewModel: LoginViewModel by lazy { component.loginViewModel }
    // View ライフサイクルに合わせて毎回新インスタンス
    val homeViewModel: HomeViewModel get() = component.homeViewModel()
}
