package com.cool.request_shiro_desktop.impl

import com.cool.request_shiro_desktop.interfaces.UserLoginToken
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response

class SimpleUserLogin(request: Request,response: Response,val username:String,val password:String): UserLoginToken(request,response) {
    override fun getPassword(): Any {
        return password
    }

    override fun getUserName(): String {
        return username
    }
}