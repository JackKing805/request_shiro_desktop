package com.cool.request_shiro_desktop.interfaces



interface IShiroCacheManager {
    //添加缓存
    fun addCache(cache: IShiroCache)

    //移除缓存
    fun removeCache(cache: IShiroCache)

    //获取缓存
    fun getCache(id:String): IShiroCache?

    fun listOf():List<IShiroCache>

    fun clear()
}