package com.example.kmptemplate

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kmptemplate.feature.home.HomeScreen
import com.example.kmptemplate.feature.login.LoginScreen
import com.example.kmptemplate.shared.uimodel.HomeViewModel
import com.example.kmptemplate.shared.uimodel.LoginViewModel
import com.example.kmptemplate.shared.uimodel.di.AppComponent

@Composable
fun App(
    loginViewModel: LoginViewModel,
    component: AppComponent
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val loginState by loginViewModel.uiState.collectAsState()
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = if (loginState.isAuthenticated) "home" else "login"
            ) {
                composable("login") {
                    LoginScreen(
                        errorMessage = loginState.errorMessage,
                        onLoginClick = { username, password ->
                            loginViewModel.login(username, password)
                        }
                    )
                }
                composable("home") {
                    val homeViewModel: HomeViewModel = component.homeViewModel()
                    HomeScreen(
                        viewModel = homeViewModel,
                        onLogout = { loginViewModel.logout() }
                    )
                }
            }
        }
    }
}
