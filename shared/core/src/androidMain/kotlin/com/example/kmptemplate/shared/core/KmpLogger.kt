package com.example.kmptemplate.shared.core

import android.util.Log

actual object KmpLogger {
    actual fun d(tag: String, msg: String) { Log.d(tag, msg) }
    actual fun e(tag: String, msg: String) { Log.e(tag, msg) }
}
