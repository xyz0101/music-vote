package com.zijin.music.musicvote.aspects;

import com.zijin.music.musicvote.anno.VoidMethod;
import com.zijin.music.musicvote.constant.Const;
import com.zijin.music.musicvote.utils.Response;
import com.zijin.music.musicvote.utils.Util;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

@Aspect
@Component
@Order(2)
public class ErrorCatchAspect {
    /**
     * 设置切点
     * 可是用 || 定义多个切点
     */
    @Pointcut(
            "(execution(public * com.zijin.music.musicvote.controller.MusicVoteController.*(..)))"
    )
    public void controllerLog(){

    }

    /**
     * 对当前切点进行前置拦截
     * 拦截条件 ：方法有@EnableAutoLogWrite 注解
     * 拦截之后封装日志并且作为消息发送
     * @return
     */
    @Around("controllerLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取签名
        Signature signature= joinPoint.getSignature();
        //将签名转为方法签名
        MethodSignature methodSignature = (MethodSignature) signature;
        //获取方法
        Method method = methodSignature.getMethod();
        Object o = null;
        try {
            o= joinPoint.proceed();
            String token = Util.getToken();
            if (!StringUtils.isEmpty(token)) {
                Const.IDEMPOTENT_MAP.remove(token);
            }
        }catch (RuntimeException e){
            if (!method.isAnnotationPresent(VoidMethod.class)){
                return Response.error(e.getMessage());
            }
        }
        return o;
    }

}
