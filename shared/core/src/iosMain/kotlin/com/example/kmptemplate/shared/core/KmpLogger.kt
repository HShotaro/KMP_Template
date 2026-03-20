package com.example.kmptemplate.shared.core

import platform.Foundation.NSLog

actual object KmpLogger {
    actual fun d(tag: String, msg: String) { NSLog("D/$tag: $msg") }
    actual fun e(tag: String, msg: String) { NSLog("E/$tag: $msg") }
}
