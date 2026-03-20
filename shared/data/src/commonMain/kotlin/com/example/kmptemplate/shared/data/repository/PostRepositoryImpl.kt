package com.example.kmptemplate.shared.data.repository

import com.example.kmptemplate.shared.domain.entity.Post
import com.example.kmptemplate.shared.domain.repository.PostRepository
import com.example.kmptemplate.shared.network.api.PostApi
import me.tatarka.inject.annotations.Inject

@Inject
class PostRepositoryImpl(private val postApi: PostApi) : PostRepository {

    override suspend fun getPosts(): Result<List<Post>> = runCatching {
        postApi.getPosts()
    }

    override suspend fun getPost(id: Int): Result<Post> = runCatching {
        postApi.getPost(id)
    }
}
