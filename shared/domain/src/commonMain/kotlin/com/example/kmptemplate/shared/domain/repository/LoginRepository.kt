package com.example.kmptemplate.shared.domain.repository

interface LoginRepository {
    fun isAuthenticated(): Boolean
    fun login(username: String, password: String): Result<Unit>
    fun logout()
}
