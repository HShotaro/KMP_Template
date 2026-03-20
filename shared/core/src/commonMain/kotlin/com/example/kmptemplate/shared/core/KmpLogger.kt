package com.example.kmptemplate.shared.core

expect object KmpLogger {
    fun d(tag: String, msg: String)
    fun e(tag: String, msg: String)
}
