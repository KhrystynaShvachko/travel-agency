package com.epam.finaltask.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class MethodLoggingAspect {

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerMethods() {}

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceMethods() {}

    @Pointcut("execution(* com.epam.finaltask.service.impl.AbstractTokenStorage.*(..))")
    public void tokenStorageMethod() {}

    @Pointcut("execution(* com.epam.finaltask.util.*Util.*(..))")
    public void utilityMethods() {}

    @Pointcut("restControllerMethods() || serviceMethods() || tokenStorageMethod() || utilityMethods()")
    public void applicationPackagePointcut() {}

    @Around("applicationPackagePointcut()")
    public Object logExecutionMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String methodName = signature.getName();
        String className = signature.getDeclaringType().getSimpleName();
        Object[] args = joinPoint.getArgs();

        log.debug("START: {}.{}() | Args: {}", className, methodName, Arrays.toString(args));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();

            log.info("FINISH: {}.{}() | Time: {}ms | Result: {}",
                    className, methodName, stopWatch.getTotalTimeMillis(), result);

            return result;
        } catch (Throwable throwable) {
            if (stopWatch.isRunning()) stopWatch.stop();
            throw throwable;
        }
    }

    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("EXCEPTION in {}.{}() | Message: {} | Cause: {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                e.getMessage(),
                e.getCause() != null ? e.getCause() : "N/A");
    }
}