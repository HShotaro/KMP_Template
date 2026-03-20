package com.example.kmptemplate.shared.uimodel.di

import com.example.kmptemplate.shared.domain.entity.Post
import com.example.kmptemplate.shared.domain.repository.LoginRepository
import com.example.kmptemplate.shared.domain.repository.PostRepository
import com.example.kmptemplate.shared.uimodel.HomeViewModel
import com.example.kmptemplate.shared.uimodel.LoginViewModel

class MockAppComponent : AppComponent() {

    private val loginRepo = object : LoginRepository {
        override fun isAuthenticated(): Boolean = true
        override fun login(username: String, password: String): Result<Unit> = Result.success(Unit)
        override fun logout() {}
    }

    private val postRepo = object : PostRepository {
        override suspend fun getPosts(): Result<List<Post>> = Result.success(mockPosts)
        override suspend fun getPost(id: Int): Result<Post> =
            mockPosts.firstOrNull { it.id == id }
                ?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("Post not found: $id"))
    }

    override val loginViewModel: LoginViewModel = LoginViewModel(loginRepo)
    override fun homeViewModel(): HomeViewModel = HomeViewModel(postRepo)
}

private val mockPosts = listOf(
    Post(userId = 1, id = 1, title = "Mock Post 1", body = "Mock body content 1"),
    Post(userId = 1, id = 2, title = "Mock Post 2", body = "Mock body content 2"),
    Post(userId = 2, id = 3, title = "Mock Post 3", body = "Mock body content 3"),
    Post(userId = 2, id = 4, title = "Mock Post 4", body = "Mock body content 4"),
    Post(userId = 3, id = 5, title = "Mock Post 5", body = "Mock body content 5"),
)
