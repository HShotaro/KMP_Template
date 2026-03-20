package com.example.kmptemplate.shared.data.repository

import com.example.kmptemplate.shared.testing.FakeSettings
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginRepositoryImplTest {

    private val settings = FakeSettings()
    private val repository = LoginRepositoryImpl(settings)

    @Test
    fun isAuthenticated_returnsFalseByDefault() {
        assertFalse(repository.isAuthenticated())
    }

    @Test
    fun login_withValidCredentials_succeeds() {
        val result = repository.login("user", "pass")
        assertTrue(result.isSuccess)
        assertTrue(repository.isAuthenticated())
    }

    @Test
    fun login_withEmptyCredentials_fails() {
        val result = repository.login("", "")
        assertTrue(result.isFailure)
        assertFalse(repository.isAuthenticated())
    }

    @Test
    fun logout_clearsAuthentication() {
        repository.login("user", "pass")
        repository.logout()
        assertFalse(repository.isAuthenticated())
    }
}
