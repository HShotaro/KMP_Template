package com.example.kmptemplate.shared.data.repository

import com.example.kmptemplate.shared.domain.repository.LoginRepository
import com.russhwolf.settings.Settings
import me.tatarka.inject.annotations.Inject

@Inject
class LoginRepositoryImpl(private val settings: Settings) : LoginRepository {

    private companion object {
        const val KEY_IS_AUTHENTICATED = "is_authenticated"
    }

    override fun isAuthenticated(): Boolean =
        settings.getBoolean(KEY_IS_AUTHENTICATED, defaultValue = false)

    override fun login(username: String, password: String): Result<Unit> {
        // Sample: any non-empty credentials succeed
        return if (username.isNotBlank() && password.isNotBlank()) {
            settings.putBoolean(KEY_IS_AUTHENTICATED, true)
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Username and password are required"))
        }
    }

    override fun logout() {
        settings.putBoolean(KEY_IS_AUTHENTICATED, false)
    }
}
