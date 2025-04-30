package com.web.app.utils

fun String.removeLineBreaks(): String {
    return this.replace(Regex("[\r\n]+\\s*"), " ").trim()
}

fun String.lineUpTrimIndent(): String {
    return this.removeLineBreaks().trimIndent()
}