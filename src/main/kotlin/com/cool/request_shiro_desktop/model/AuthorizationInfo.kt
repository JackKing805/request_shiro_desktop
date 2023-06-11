package com.cool.request_shiro_desktop.model

import java.io.Serializable


//授权信息
class AuthorizationInfo:Serializable {
    private val roles = mutableListOf<String>()
    private val permissions = mutableListOf<String>()

    fun setRole(role:String){
        if (!roles.contains(role)){
            roles.add(role)
        }
    }

    fun setPermission(permission:String){
        if (!permissions.contains(permission)){
            permissions.add(permission)
        }
    }

    internal fun getRoles() = roles

    internal fun getPermissions() = permissions
}