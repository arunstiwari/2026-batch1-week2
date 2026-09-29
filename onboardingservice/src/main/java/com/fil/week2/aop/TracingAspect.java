package com.fil.week2.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(0)
public class TracingAspect {
    private static final Logger log = LoggerFactory.getLogger(TracingAspect.class);

    // We will create a PointCut

    @Pointcut("@annotation(com.fil.week2.aop.Traced) || @within(com.fil.week2.aop.Traced)")
    public void traced() {
    }

    private String args(ProceedingJoinPoint joinPoint) {
        return Arrays.toString(joinPoint.getArgs());
    }

    private String label(Traced traced, MethodSignature signature) {
        if (!traced.value().isBlank()) {
            return traced.value();
        }
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    private Traced findTraced(ProceedingJoinPoint joinPoint, Method method) {
        Traced traced = AnnotatedElementUtils.findMergedAnnotation(method, Traced.class);
        if (traced != null) {
            return traced;
        }
        return AnnotatedElementUtils.findMergedAnnotation
                (joinPoint.getTarget().getClass(), Traced.class);
    }

    // We will create an Advice
    @Around("traced()")
    public Object traced(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Traced traced = findTraced(joinPoint, signature.getMethod());
        String label = label(traced, signature);
        log.info(" --> {}{}", label, traced.logArgs() ? args(joinPoint): "");
        long startedAt = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long endAt = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(endAt - startedAt);
            if (duration > traced.slowMillis()) {
                log.warn(" -->{} completed in {} ms (slower than {} ms)",
                        label, duration, traced.slowMillis() );
            }else {
                log.info(" -->{} completed in {} ms",
                        label, duration);
            }
            return result;
        }catch (Throwable failure){
            log.error("Failed with {} : {}",failure.getClass().getSimpleName(), failure.getMessage());
            throw failure;
        }
    }



}
