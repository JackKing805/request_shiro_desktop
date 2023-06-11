package com.cool.request_shiro_desktop.config

import com.cool.request_shiro_desktop.impl.ShiroCache
import com.cool.request_shiro_desktop.impl.ShiroCacheManager
import com.cool.request_shiro_desktop.interfaces.IShiroAuth
import com.cool.request_shiro_desktop.interfaces.IShiroCache
import com.cool.request_shiro_desktop.interfaces.IShiroCacheManager


data class ShiroConfig(
    val tokenName:String,//token保存的名字
    val validTime:Int = 60,//s
    val authInter:Class<out IShiroAuth> = IShiroAuth::class.java,
    val cacheManagerType:Class<out IShiroCacheManager> = ShiroCacheManager::class.java,
    val cacheType:Class<out IShiroCache> = ShiroCache::class.java,
)