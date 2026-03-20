package com.example.kmptemplate.shared.testing

import com.example.kmptemplate.shared.domain.repository.LoginRepository

class FakeLoginRepository(
    private var authenticated: Boolean = false
) : LoginRepository {
    override fun isAuthenticated(): Boolean = authenticated
    override fun login(username: String, password: String): Result<Unit> {
        authenticated = true
        return Result.success(Unit)
    }
    override fun logout() { authenticated = false }
}
