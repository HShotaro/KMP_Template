package com.example.kmptemplate.shared.domain.model

/**
 * Swift の Identifiable に適合させるための共通インターフェース
 */
interface SharedIdentifiable {
    val id: String
}

/**
 * Swift の Sendable に適合させるためのマーカークラス。
 * open class にすることで Swift 側での extension が可能になる。
 */
open class SharedSendable
