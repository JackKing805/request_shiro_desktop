package com.cool.request_shiro_desktop.impl

import com.cool.request_shiro_desktop.interfaces.IShiroCache
import com.cool.request_shiro_desktop.interfaces.IShiroCacheManager

class ShiroCacheManager : IShiroCacheManager {
    private val lock = Any()
    private val cacheList = mutableSetOf<IShiroCache>()

    override fun addCache(cache: IShiroCache) {
        synchronized(lock) {
            val old = cacheList.find { it.getID() == cache.getID() }
            if (old!=null){
                cacheList.remove(old)
            }
            cacheList.add(cache)
        }
    }

    override fun removeCache(cache: IShiroCache) {
        synchronized(lock) {
            cacheList.remove(cache)
        }
    }

    override fun getCache(id: String): IShiroCache? {
        val c = synchronized(lock) {
            cacheList.find { it.getID() == id }
        }

        if (c != null) {
            if (c.isValid()){
                return c
            }else{
                removeCache(c)
            }
        }

        return null
    }

    override fun listOf(): List<IShiroCache> {
        return cacheList.filter { it.isValid() }.toList()
    }

    override fun clear() {
        synchronized(cacheList) {
            cacheList.clear()
        }
    }
}