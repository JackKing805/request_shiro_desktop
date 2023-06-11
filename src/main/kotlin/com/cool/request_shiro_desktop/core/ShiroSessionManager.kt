package com.cool.request_shiro_desktop.core

import com.cool.request_shiro_desktop.utils.IdGenerator
import com.cool.request_shiro_desktop.utils.InnerShiroUtils
import com.jerry.rt.bean.RtSessionConfig
import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.interfaces.ISession
import com.jerry.rt.core.http.interfaces.ISessionManager
import com.jerry.rt.core.http.pojo.ProtocolPackage
import kotlinx.coroutines.*
import java.net.URI

class ShiroSessionManager:ISessionManager {
    private val sessionLock = Any()
    private val sessions = mutableListOf<ShiroSession>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
    })
    private lateinit var rtSessionConfig: RtSessionConfig

    override fun active(rtSessionConfig: RtSessionConfig) {
        this.rtSessionConfig = rtSessionConfig
    }

    override fun deactivate() {
        scope.cancel()
    }

    override fun createSession(sessionId: String?): ISession {
        val findSession = findSession(sessionId)
        if (findSession!=null){
            findSession.setIsNew(false)
            findSession.setLastAccessedTime(System.currentTimeMillis())
            return findSession
        }
        val shiroSession = ShiroSession(generateSessionId())
        shiroSession.setIsNew(true)
        shiroSession.setMaxInactiveInterval(rtSessionConfig.sessionValidTime.toMillis().toInt())
        synchronized(sessionLock){
            sessions.add(shiroSession)
        }
        scope.launch {
            while (shiroSession.isInValidTime()){
                delay(2000)
            }
            removeSession(shiroSession)
        }
        return shiroSession
    }

    override fun findSession(sessionId: String?): ISession? {
        sessionId?:return null
        return synchronized(sessionLock){
            sessions.find { it.getId() == sessionId }
        }
    }

    override fun generateSessionId(): String {
        return IdGenerator.generatorId()
    }

    override fun getSessionKey(
        rtContext: RtContext,
        path: String,
        uri: URI,
        header: ProtocolPackage.Header
    ): String? {
        return header.getCookie(rtContext.getRtConfig().rtSessionConfig.sessionKey) ?: return InnerShiroUtils.parameterToArray(uri)[rtContext.getRtConfig().rtSessionConfig.sessionKey]
    }

    override fun removeSession(session: ISession) {
        synchronized(sessionLock){
            sessions.remove(session)
        }
    }
}