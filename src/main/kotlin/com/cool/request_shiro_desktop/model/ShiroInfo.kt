package com.cool.request_shiro_desktop.model

import com.cool.request_shiro_desktop.model.AuthenticationInfo
import com.cool.request_shiro_desktop.model.AuthorizationInfo
import java.io.Serializable

data class ShiroInfo (
    val authorizationInfo: AuthorizationInfo,
    val authenticationInfo: AuthenticationInfo
):Serializable