package com.cool.request_shiro_desktop.interfaces

import com.cool.request_shiro_desktop.model.AuthToken
import com.cool.request_shiro_desktop.model.AuthenticationInfo
import com.cool.request_shiro_desktop.model.AuthorizationInfo
import com.jerry.rt.core.http.pojo.Request


interface IShiroAuth {
    //认证,返回token
    fun onAuthentication(authToken: AuthToken): AuthenticationInfo?

    //授权
    fun onAuthorization(authorization: AuthenticationInfo): AuthorizationInfo

    //获取访问者的token
    fun getAccessToken(request: Request,shiroTokenName:String):String?{
        return request.getPackage().getHeader().getCookie(shiroTokenName)
    }
}
