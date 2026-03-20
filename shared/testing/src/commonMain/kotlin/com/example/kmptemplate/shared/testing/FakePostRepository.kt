package com.example.kmptemplate.shared.testing

import com.example.kmptemplate.shared.domain.entity.Post
import com.example.kmptemplate.shared.domain.repository.PostRepository

class FakePostRepository(
    private val posts: List<Post> = defaultPosts
) : PostRepository {
    override suspend fun getPosts(): Result<List<Post>> = Result.success(posts)
    override suspend fun getPost(id: Int): Result<Post> =
        posts.firstOrNull { it.id == id }
            ?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Post not found: $id"))
}

val defaultPosts = listOf(
    Post(userId = 1, id = 1, title = "Sample Post 1", body = "Body of post 1"),
    Post(userId = 1, id = 2, title = "Sample Post 2", body = "Body of post 2"),
    Post(userId = 2, id = 3, title = "Sample Post 3", body = "Body of post 3"),
)
