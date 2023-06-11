package com.cool.request_shiro_desktop.utils

import java.net.URI

internal object InnerShiroUtils {
    fun parameterToArray(uri:URI):Map<String,String>{
        val query = uri.query?:return emptyMap()
        val map = mutableMapOf<String,String>()
        query.split("&").forEach {
            val split = it.split("=")
            map[split[0]] = split[1]
        }
        return map
    }

    fun isChildList(parent:List<String>,child:List<String>):Boolean{
        child.forEach {
            if (!parent.contains(it)){
                return false
            }
        }
        return true
    }
}