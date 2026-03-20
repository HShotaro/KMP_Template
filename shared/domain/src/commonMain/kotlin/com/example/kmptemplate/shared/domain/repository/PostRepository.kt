package com.example.kmptemplate.shared.domain.repository

import com.example.kmptemplate.shared.domain.entity.Post

interface PostRepository {
    suspend fun getPosts(): Result<List<Post>>
    suspend fun getPost(id: Int): Result<Post>
}
