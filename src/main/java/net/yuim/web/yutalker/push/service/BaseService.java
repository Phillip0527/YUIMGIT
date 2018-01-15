package net.yuim.web.yutalker.push.service;

import net.yuim.web.yutalker.push.bean.db.User;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

public class BaseService {
    // 添加一个上下文注解，该注解会给securityContext赋值
    // 具体的值是我们拦截器中所返回的SecurityContext
    @Context
    protected SecurityContext securityContext;

    /**
     * 从上下文中获取自己的信息
     * @return User
     */
    protected User getSelf(){
        return (User) securityContext.getUserPrincipal();
    }
}