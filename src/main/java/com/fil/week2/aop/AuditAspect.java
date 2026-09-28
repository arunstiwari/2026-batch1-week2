package com.fil.week2.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) " +
            "&& execution(public * *(..))")
    public void restController() {
    }

    @Around("restController()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
           Object object =  audit(joinPoint);
        return object;
    }

    private Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
            try {
                Object result = joinPoint.proceed();
                log.info("Controller Layer: OK");
                return result;
            }catch (Throwable e){
                log.error("Controller Layer: FAILED", e);
                throw e ;
            }
    }
}
