package com.cool.request_shiro_desktop.core

import com.jerry.rt.core.http.interfaces.ISession
import java.util.*

class ShiroSession(private val sessionId:String):ISession {
    private val attributes = mutableMapOf<String,Any?>()
    private val createTime = System.currentTimeMillis()
    private var lastAccessTime = createTime
    private var maxInactiveInterval = -1
    private var isNew = false

    override fun getAttribute(name: String): Any? {
        return attributes[name]
    }

    override fun getAttributeNames(): Enumeration<String>? {
        return Collections.enumeration(attributes.keys)
    }

    override fun getCreationTime(): Long {
        return createTime
    }

    override fun getId(): String {
        return sessionId
    }

    override fun getLastAccessedTime(): Long {
        return lastAccessTime
    }

    override fun getMaxInactiveInterval(): Int {
        return maxInactiveInterval
    }

    override fun invalidate() {
        maxInactiveInterval = -1
    }

    override fun isNew(): Boolean {
        return isNew
    }

    override fun removeAttribute(name: String) {
        attributes.remove(name)
    }

    override fun setAttribute(name: String, value: Any?) {
        attributes[name] = value
    }

    override fun setIsNew(isNew: Boolean) {
        this.isNew = isNew
    }

    override fun setLastAccessedTime(time: Long) {
        lastAccessTime = time
    }

    override fun setMaxInactiveInterval(interval: Int) {
        maxInactiveInterval = interval
    }

    fun isInValidTime():Boolean{
        if(maxInactiveInterval==-1){
            return false
        }
        return System.currentTimeMillis() - lastAccessTime<=maxInactiveInterval
    }
}