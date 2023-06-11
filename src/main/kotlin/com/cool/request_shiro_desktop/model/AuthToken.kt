package com.cool.request_shiro_desktop.model

import java.io.Serializable

class AuthToken(private val username:String,private val password:Any) :Serializable{
     fun getPassword() = password
     fun getUserName() = username
}