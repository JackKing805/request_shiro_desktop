package com.cool.request_shiro_desktop.utils

import java.util.UUID

internal object IdGenerator {
    fun generatorId(from:String = ""):String{
        return from + UUID.randomUUID().toString()
    }
}