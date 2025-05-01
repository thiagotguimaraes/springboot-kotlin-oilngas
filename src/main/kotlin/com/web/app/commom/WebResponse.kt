package com.web.app.commom

open class WebResponse<T>(
    open val body: T?,
    open val ok: Boolean,
    open val message: String?,
    open val status: Int?,
)