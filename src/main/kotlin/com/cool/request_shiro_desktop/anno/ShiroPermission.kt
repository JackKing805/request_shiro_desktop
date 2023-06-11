package com.cool.request_shiro_desktop.anno

import com.cool.request_shiro_desktop.bean.ShiroLogic
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class ShiroPermission(
    val permissions:Array<String>,//权限
    val logic: ShiroLogic = ShiroLogic.AND
)

