package com.example.kmptemplate.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createHttpClient(): HttpClient = HttpClient(OkHttp)
