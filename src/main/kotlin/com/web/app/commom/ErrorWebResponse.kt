package com.web.app.commom

data class ErrorWebResponse<T>(override val message: String?, override val status: Int?) :
    WebResponse<T>(body = null, ok = false, message = message, status = status)
