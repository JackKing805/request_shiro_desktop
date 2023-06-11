package com.cool.request_shiro_desktop.exception

class ShiroRoleException(vararg roles:String):Exception("don't have ${roles.toList()}")