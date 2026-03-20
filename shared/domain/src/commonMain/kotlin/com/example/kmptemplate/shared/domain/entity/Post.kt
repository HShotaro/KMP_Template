package com.example.kmptemplate.shared.domain.entity

import com.example.kmptemplate.shared.domain.model.SharedIdentifiable
import com.example.kmptemplate.shared.domain.model.SharedSendable
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
) : SharedSendable(), SharedIdentifiable {
    override val id: String get() = this.id.toString()
    // Swift 向け no-arg コンストラクタ
    constructor() : this(0, 0, "", "")
}
