package com.cool.request_shiro_desktop.interfaces

import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

abstract class UserLoginToken(private val request: Request,private val response: Response) {
    abstract fun getPassword():Any

    abstract fun getUserName():String

    internal fun getRequest() = request

    internal fun getResponse() = response
}