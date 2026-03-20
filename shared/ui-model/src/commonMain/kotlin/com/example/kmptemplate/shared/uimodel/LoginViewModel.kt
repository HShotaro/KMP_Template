package com.example.kmptemplate.shared.uimodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmptemplate.shared.domain.model.SharedSendable
import com.example.kmptemplate.shared.domain.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import me.tatarka.inject.annotations.Inject

data class LoginUiState(
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null
) : SharedSendable() {
    constructor() : this(false, null)
}

interface LoginViewModelInterface {
    fun login(username: String, password: String)
    fun logout()
}

open class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel(), LoginViewModelInterface {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(isAuthenticated = loginRepository.isAuthenticated())
    }

    fun observeUiState(onChange: (LoginUiState) -> Unit) {
        uiState.onEach { onChange(it) }.launchIn(viewModelScope)
    }

    override fun login(username: String, password: String) {
        loginRepository.login(username, password)
            .onSuccess { _uiState.update { it.copy(isAuthenticated = true, errorMessage = null) } }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    override fun logout() {
        loginRepository.logout()
        _uiState.value = LoginUiState()
    }
}
