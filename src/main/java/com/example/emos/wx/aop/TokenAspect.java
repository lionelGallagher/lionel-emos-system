package com.example.emos.wx.aop;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Component
@Aspect
public class TokenAspect {
    @Resource
    private ThreadLocalToken threadLocalToken;

    @Pointcut("execution(public * com.example.emos.wx.controller.*.*(..))")
    public void aspect() {

    }

    @Around("aspect()")
    public Object aroundAspect(ProceedingJoinPoint point) throws Throwable {
        String token = threadLocalToken.getLocal();
        R r = (R) point.proceed();
        if (token != null) {
            r.put("token", token);
        }
        return r;
    }

}
