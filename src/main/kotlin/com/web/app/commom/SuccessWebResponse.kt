package com.web.app.commom

data class SuccessWebResponse<T>(
    override val body: T?,
    override val message: String? = "success",
) : WebResponse<T>(body = body as T, ok = true, message = message, status = 200)
