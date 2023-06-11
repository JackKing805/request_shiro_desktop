package com.cool.request_shiro_desktop.anno

import com.cool.request_shiro_desktop.bean.ShiroLogic
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class ShiroRole(
    val roles:Array<String>,//用户角色
    val logic: ShiroLogic = ShiroLogic.AND
)

