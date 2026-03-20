package com.example.kmptemplate.shared.uimodel

import com.example.kmptemplate.shared.testing.FakeLoginRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginViewModelTest {

    private val repo = FakeLoginRepository()
    private val viewModel = LoginViewModel(repo)

    @Test
    fun initialState_isNotAuthenticated() {
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun login_updatesStateToAuthenticated() = runTest {
        viewModel.login("user", "pass")
        assertTrue(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun logout_clearsAuthentication() = runTest {
        viewModel.login("user", "pass")
        viewModel.logout()
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }
}
