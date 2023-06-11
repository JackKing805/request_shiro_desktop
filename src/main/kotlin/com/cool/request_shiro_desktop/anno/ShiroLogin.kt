package com.cool.request_shiro_desktop.anno

import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class ShiroLogin//如果类上没有其他验证的注解，才会验证此注解

