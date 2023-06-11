package com.cool.request_shiro_desktop.register

import com.cool.request_core.ReflectUtils
import com.cool.request_core.base.annotations.Bean
import com.cool.request_core.base.annotations.ConfigRegister
import com.cool.request_core.base.bean.ControllerReferrer
import com.cool.request_core.base.interfaces.IConfig
import com.cool.request_core.core.Core
import com.cool.request_shiro_desktop.ShiroUtils
import com.cool.request_shiro_desktop.anno.ShiroLogin
import com.cool.request_shiro_desktop.anno.ShiroPermission
import com.cool.request_shiro_desktop.anno.ShiroRole
import com.cool.request_shiro_desktop.bean.ShiroLogic
import com.cool.request_shiro_desktop.config.ShiroConfig
import com.cool.request_shiro_desktop.core.ShiroSessionManager
import com.cool.request_shiro_desktop.exception.ShiroPermissionException
import com.cool.request_shiro_desktop.exception.ShiroRoleException
import com.cool.request_shiro_desktop.exception.ShiroVerifyException
import com.cool.request_shiro_desktop.interfaces.IShiroAuth
import com.cool.request_shiro_desktop.interfaces.IShiroCacheManager
import com.cool.request_shiro_desktop.model.ShiroInfo
import com.cool.request_shiro_desktop.utils.InnerShiroUtils
import com.jerry.rt.bean.RtSessionConfig
import com.jerry.rt.core.http.pojo.Cookie
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import java.util.*

@ConfigRegister(priority = 0, registerClass = Any::class)
class ShiroConfigRegister : IConfig() {

    @Bean
    fun registerSession() = RtSessionConfig(
        sessionKey = "SHIRO_SESSION_ID",
        sessionClazz = ShiroSessionManager::class.java
    )

    override fun onCreate() {
        Core.getBean(ShiroConfig::class.java)?.let {
            ShiroUtils.shiroConfig = it as ShiroConfig
        }


        val shiroConfig = ShiroUtils.shiroConfig
        Core.getBean(shiroConfig.authInter)?.let {
            ShiroUtils.iShiroAuth = it as IShiroAuth
        }
        Core.getBean(shiroConfig.cacheManagerType)?.let {
            ShiroUtils.cacheManager = it as IShiroCacheManager
        }
    }


    override fun onRequestPre(
        request: Request,
        response: Response,
        controllerReferrer: ControllerReferrer
    ): Boolean {
        val clazzRoleAnno = ReflectUtils.getAnnotation(controllerReferrer.instance.javaClass, ShiroRole::class.java)
        val clazzPermissionAnno = ReflectUtils.getAnnotation(controllerReferrer.instance.javaClass, ShiroPermission::class.java)

        val methodRoleAnno = ReflectUtils.getAnnotation(controllerReferrer.method, ShiroRole::class.java)
        val methodPermissionAnno = ReflectUtils.getAnnotation(controllerReferrer.method, ShiroPermission::class.java)

        if (clazzRoleAnno==null && clazzPermissionAnno == null && methodRoleAnno == null && methodPermissionAnno==null){
            val loginAnno = ReflectUtils.getAnnotation(controllerReferrer.instance.javaClass, ShiroLogin::class.java)
            if (loginAnno!=null){//添加了对登陆的验证，未登陆会直接抛出移除，让请求无法直接到达controller
                val token = ShiroUtils.iShiroAuth.getAccessToken(request, ShiroUtils.shiroConfig.tokenName) ?: throw ShiroVerifyException("user token is invalid")
                ShiroUtils.cacheManager.getCache(token) ?: throw ShiroVerifyException("token is invalid")
            }
            return true
        }

        val token = ShiroUtils.iShiroAuth.getAccessToken(request, ShiroUtils.shiroConfig.tokenName) ?: throw ShiroVerifyException("user token is invalid")
        val shiroCache = ShiroUtils.cacheManager.getCache(token) ?: throw ShiroVerifyException("token is invalid")

        //刷新token时间
        val expires = Date(System.currentTimeMillis() + ShiroUtils.shiroConfig.validTime*1000)
        response.addCookie(Cookie(ShiroUtils.shiroConfig.tokenName, value = shiroCache.getID(), expires =expires, path = "/"))
        shiroCache.setExpires(expires)

        val shiroInfo =  shiroCache.getValue(ShiroUtils.shiroConfig.tokenName, null) as? ShiroInfo ?:throw ShiroVerifyException("no valid auth info")

        val roles = shiroInfo.authorizationInfo.getRoles()
        val permissions = shiroInfo.authorizationInfo.getPermissions()

        if (clazzRoleAnno!=null && clazzRoleAnno.roles.isNotEmpty()){
            when(clazzRoleAnno.logic){
                ShiroLogic.AND -> {
                    if (!InnerShiroUtils.isChildList(roles,clazzRoleAnno.roles.toList())){
                        throw ShiroRoleException(*clazzRoleAnno.roles.subtract(roles.toSet()).toTypedArray())
                    }
                }
                ShiroLogic.OR -> {
                    if (clazzRoleAnno.roles.intersect(roles.toSet()).isEmpty()){
                        throw ShiroRoleException(*clazzRoleAnno.roles.subtract(roles.toSet()).toTypedArray())
                    }
                }
            }
        }

        if (clazzPermissionAnno!=null && clazzPermissionAnno.permissions.isNotEmpty()){
            when(clazzPermissionAnno.logic){
                ShiroLogic.AND -> {
                    if (!InnerShiroUtils.isChildList(permissions,clazzPermissionAnno.permissions.toList())){
                        throw ShiroPermissionException(*clazzPermissionAnno.permissions.subtract(permissions.toSet()).toTypedArray())
                    }
                }
                ShiroLogic.OR -> {
                    if (clazzPermissionAnno.permissions.intersect(permissions.toSet()).isEmpty()){
                        throw ShiroPermissionException(*clazzPermissionAnno.permissions.subtract(permissions.toSet()).toTypedArray())
                    }
                }
            }
        }

        if (methodRoleAnno!=null && methodRoleAnno.roles.isNotEmpty()){
            when(methodRoleAnno.logic){
                ShiroLogic.AND -> {
                    if (!InnerShiroUtils.isChildList(roles,methodRoleAnno.roles.toList())){
                        throw ShiroRoleException(*methodRoleAnno.roles.subtract(roles.toSet()).toTypedArray())
                    }
                }
                ShiroLogic.OR -> {
                    if (methodRoleAnno.roles.intersect(roles.toSet()).isEmpty()){
                        throw ShiroRoleException(*methodRoleAnno.roles.subtract(roles.toSet()).toTypedArray())
                    }
                }
            }
        }

        if (methodPermissionAnno!=null && methodPermissionAnno.permissions.isNotEmpty()){
            when(methodPermissionAnno.logic){
                ShiroLogic.AND -> {
                    if (!InnerShiroUtils.isChildList(permissions,methodPermissionAnno.permissions.toList())){
                        throw ShiroPermissionException(*methodPermissionAnno.permissions.subtract(permissions.toSet()).toTypedArray())
                    }
                }
                ShiroLogic.OR -> {
                    if(methodPermissionAnno.permissions.intersect(permissions.toSet()).isEmpty()){
                        throw ShiroPermissionException(*methodPermissionAnno.permissions.subtract(permissions.toSet()).toTypedArray())
                    }
                }
            }
        }
        return true
    }
}