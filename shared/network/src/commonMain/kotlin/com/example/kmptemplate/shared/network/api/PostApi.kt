package com.example.kmptemplate.shared.network.api

import com.example.kmptemplate.shared.domain.entity.Post
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import me.tatarka.inject.annotations.Inject

private const val BASE_URL = "https://jsonplaceholder.typicode.com"

@Inject
class PostApi(private val httpClient: HttpClient) {

    suspend fun getPosts(): List<Post> =
        httpClient.get("$BASE_URL/posts").body()

    suspend fun getPost(id: Int): Post =
        httpClient.get("$BASE_URL/posts/$id").body()
}
