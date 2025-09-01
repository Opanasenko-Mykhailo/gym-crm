package com.gcs.app.logging.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RestLoggingAspect {

    private final HttpServletRequest request;
    private final SensitiveDataMasker dataMasker;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restController() {
    }

    @Before("restController()")
    public void logRequest(JoinPoint joinPoint) {
        try {
            String methodName = getMethodName(joinPoint);
            String maskedArgs = dataMasker.maskArguments(joinPoint.getArgs());
            log.info("REST Request: [{}] Method: {}, Args: {}", getRequestInfo(), methodName, maskedArgs);
        } catch (Exception e) {
            log.error("Failed to log request [{}]: {}", getRequestInfo(), e.getMessage(), e);
        }
    }

    @AfterReturning(pointcut = "restController()", returning = "result")
    public void logResponse(JoinPoint joinPoint, Object result) {
        try {
            String requestInfo = getRequestInfo();

            if (result == null) {
                log.info("REST Response: [{}] null", requestInfo);
                return;
            }
            log.info("REST Response: [{}] {}", requestInfo, dataMasker.mask(result));
        } catch (Exception e) {
            log.error("Failed to log response [{}]: {}", getRequestInfo(), e.getMessage(), e);
        }
    }

    @AfterThrowing(pointcut = "restController()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        log.error("REST Error: [{}] {}", getRequestInfo(), ex.getMessage(), ex);
    }

    private String getMethodName(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
    }

    private String getRequestInfo() {
        return String.format("%s %s", request.getMethod(), request.getRequestURI());
    }
}