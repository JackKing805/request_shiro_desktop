package com.cool.request_shiro_desktop

import com.cool.request_shiro_desktop.bean.ShiroLogic
import com.cool.request_shiro_desktop.config.ShiroConfig
import com.cool.request_shiro_desktop.exception.NoShiroInfoException
import com.cool.request_shiro_desktop.exception.ShiroAuthException
import com.cool.request_shiro_desktop.exception.ShiroRoleException
import com.cool.request_shiro_desktop.exception.ShiroVerifyException
import com.cool.request_shiro_desktop.impl.ShiroCacheManager
import com.cool.request_shiro_desktop.interfaces.IShiroAuth
import com.cool.request_shiro_desktop.interfaces.IShiroCache
import com.cool.request_shiro_desktop.interfaces.IShiroCacheManager
import com.cool.request_shiro_desktop.interfaces.UserLoginToken
import com.cool.request_shiro_desktop.model.AuthToken
import com.cool.request_shiro_desktop.model.AuthenticationInfo
import com.cool.request_shiro_desktop.model.AuthorizationInfo
import com.cool.request_shiro_desktop.model.ShiroInfo
import com.cool.request_shiro_desktop.utils.InnerShiroUtils
import com.jerry.rt.core.http.pojo.Cookie
import com.jerry.rt.core.http.pojo.Request
import com.jerry.rt.core.http.pojo.Response
import java.util.*

/**
 * 如果是以token形式来进行认证的话，目前用session保存是无效，客户端一旦断开，session立马就会失效
 * 重新写一个缓存接口，使其可以自定怎么增加缓存，删除缓存，并且可以设置缓存有效期，或者定时删除缓存
 * 权限和角色注解修改，添加判定逻辑，1.and同时都必须拥有，2.or有其中一个就行
 */
object ShiroUtils {
    internal var shiroConfig =  ShiroConfig("shiro_token",1500)

    internal var iShiroAuth: IShiroAuth = object : IShiroAuth {
        override fun onAuthentication(authToken: AuthToken): AuthenticationInfo {
            return AuthenticationInfo(authToken,authToken.getPassword(),UUID.randomUUID().toString())
        }

        override fun onAuthorization(authorization: AuthenticationInfo): AuthorizationInfo {
            return AuthorizationInfo()
        }
    }

    internal var cacheManager: IShiroCacheManager = ShiroCacheManager()


    @Throws(exceptionClasses = [ShiroAuthException::class])
    fun login(userLoginToken: UserLoginToken):String{
        val shiroCache = getShiroCache(userLoginToken.getRequest())
        if (shiroCache!=null){
            val shiroInfo = shiroCache.getValue(shiroConfig.tokenName, null) as? ShiroInfo
            if (shiroInfo!=null){
                val expires = Date(System.currentTimeMillis() + shiroConfig.validTime*1000)
                userLoginToken.getResponse().addCookie(Cookie(shiroConfig.tokenName, value = shiroInfo.authenticationInfo.token, expires = expires, path = "/"))
                shiroCache.setExpires(expires)
                return shiroInfo.authenticationInfo.token
            }
        }

        val onAuthentication = iShiroAuth.onAuthentication(AuthToken(userLoginToken.getUserName(),userLoginToken.getPassword())) ?: throw ShiroAuthException("auth error")

        val onAuthorization = iShiroAuth.onAuthorization(onAuthentication)

        val newCache = shiroConfig.cacheType.newInstance().apply {
            setId(onAuthentication.token)
            val expires = Date(System.currentTimeMillis() + shiroConfig.validTime*1000)
            setExpires(expires)
            putValue(shiroConfig.tokenName, ShiroInfo(onAuthorization,onAuthentication))
        }

        cacheManager.addCache(newCache)
        userLoginToken.getResponse().addCookie(Cookie(shiroConfig.tokenName, value = onAuthentication.token, maxAge = shiroConfig.validTime, path = "/"))
        return onAuthentication.token
    }

    fun logout(request: Request,response: Response){
        val token = iShiroAuth.getAccessToken(request, shiroConfig.tokenName)
        if (token!=null){
            val cache = cacheManager.getCache(token)
            if (cache!=null){
                cacheManager.removeCache(cache)
            }
            response.addCookie(Cookie(shiroConfig.tokenName, value = "", maxAge = 0, path = "/"))
        }
    }


    fun getShiroCache(request: Request) : IShiroCache?{
        val token = iShiroAuth.getAccessToken(request, shiroConfig.tokenName)
        if (token!=null){
            val shiroCache = cacheManager.getCache(token)
            if (shiroCache!=null){
                return shiroCache
            }
        }
        return null
    }
    private fun getShiroInfo(request: Request) : ShiroInfo?{
        val token = iShiroAuth.getAccessToken(request, shiroConfig.tokenName)
        if (token!=null){
            val shiroInfo = cacheManager.getCache(token)?.getValue(shiroConfig.tokenName, null) as? ShiroInfo
            if (shiroInfo!=null){
                return shiroInfo
            }
        }
        return null
    }


    @Throws(exceptionClasses = [NoShiroInfoException::class])
    fun getAuthInfo(request: Request): ShiroInfo {
        return getShiroInfo(request) ?: throw NoShiroInfoException()
    }

    private fun refreshExpires(token:String): ShiroInfo?{
        val shiroCache = cacheManager.getCache(token)
        return if (shiroCache!=null){
            val expires = Date(System.currentTimeMillis() + shiroConfig.validTime*1000)
            shiroCache.setExpires(expires)
            shiroCache.getValue(shiroConfig.tokenName,null) as? ShiroInfo
        }else{
            null
        }
    }

    fun verifyRoles(request: Request,needRoles:List<String>,logic: ShiroLogic = ShiroLogic.AND){
        val token = iShiroAuth.getAccessToken(request, shiroConfig.tokenName) ?: throw ShiroVerifyException("token is invalid")
        val shiroInfo = refreshExpires(token) ?:throw ShiroVerifyException("no valid auth info")
        if (needRoles.isNotEmpty()){
            val roles = shiroInfo.authorizationInfo.getRoles()
            when(logic){
                ShiroLogic.AND -> {
                    if (!InnerShiroUtils.isChildList(roles,needRoles)){
                        throw ShiroRoleException(*needRoles.subtract(roles.toSet()).toTypedArray())
                    }
                }
                ShiroLogic.OR -> {
                    if (roles.intersect(needRoles.toSet()).isEmpty()){
                        throw ShiroRoleException(*needRoles.subtract(roles.toSet()).toTypedArray())
                    }
                }
            }
        }
    }

    fun verifyPermissions(request: Request,needPermissions:List<String>,logic: ShiroLogic = ShiroLogic.AND){
        val token = iShiroAuth.getAccessToken(request, shiroConfig.tokenName) ?: throw ShiroVerifyException("token is invalid")
        val shiroInfo = refreshExpires(token) ?:throw ShiroVerifyException("no valid auth info")
        if (needPermissions.isNotEmpty()){
            val permissions = shiroInfo.authorizationInfo.getPermissions()
            when(logic){
                ShiroLogic.AND -> {
                    if (!InnerShiroUtils.isChildList(permissions,needPermissions)){
                        throw ShiroRoleException(*needPermissions.subtract(permissions.toSet()).toTypedArray())
                    }
                }
                ShiroLogic.OR -> {
                    if (permissions.intersect(needPermissions.toSet()).isEmpty()){
                        throw ShiroRoleException(*needPermissions.subtract(permissions.toSet()).toTypedArray())
                    }
                }
            }
        }
    }

    fun verify(request: Request, needRoles: List<String>, needPermissions: List<String>, roleLogic: ShiroLogic = ShiroLogic.AND, permissionLogic: ShiroLogic = ShiroLogic.AND){
        verifyRoles(request,needRoles,roleLogic)
        verifyPermissions(request,needPermissions,permissionLogic)
    }
}