package com.cool.request_shiro_desktop.exception

class ShiroPermissionException(vararg permissions:String):Exception("don't have ${permissions.toList()}")