package com.cool.request_shiro_desktop.model

import java.io.Serializable

//认证信息
data class AuthenticationInfo(
    val main:Any,
    val password:Any,
    val token:String
):Serializable