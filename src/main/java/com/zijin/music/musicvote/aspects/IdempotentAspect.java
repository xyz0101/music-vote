package com.zijin.music.musicvote.aspects;

import com.zijin.music.musicvote.anno.Idempotent;
import com.zijin.music.musicvote.constant.Const;
import com.zijin.music.musicvote.utils.Util;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Order(1)
public class IdempotentAspect {

    /**
     * 设置切点
     * 可是用 || 定义多个切点
     */
    @Pointcut(
            "(execution(public * com.zijin.music.musicvote.controller.MusicVoteController.*(..)))"
    )
    public void controllerLog(){

    }

    @Before("controllerLog()")
    public void before(JoinPoint joinPoint){
        //获取签名
        Signature signature= joinPoint.getSignature();
        //将签名转为方法签名
        MethodSignature methodSignature = (MethodSignature) signature;
        //获取方法
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(Idempotent.class)){
            Idempotent annotation = method.getAnnotation(Idempotent.class);
            int time = annotation.idemTime();
            String key = Util.getToken();
            if (StringUtils.isEmpty(key)||Const.IDEMPOTENT_MAP.containsKey(key)){
                throw  new RuntimeException("请勿重复提交表单");
            }else {
                Const.IDEMPOTENT_MAP.put(key,"Y");
            }
        }
    }




}
