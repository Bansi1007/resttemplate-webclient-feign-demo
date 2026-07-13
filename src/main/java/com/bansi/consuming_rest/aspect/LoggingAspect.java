package com.bansi.consuming_rest.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//https://www.geeksforgeeks.org/advance-java/aspect-oriented-programming-aop-in-spring-framework/
@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

//    //https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html
//    private static final DateTimeFormatter FMT =
//            DateTimeFormatter.ofPattern("EEE, MMM dd, hh:mm:ss a");

    @Around("execution(* com.bansi.consuming_rest.controller..*(..)) || " +
            "execution(* com.bansi.consuming_rest.service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();

        // log.info("Entered method {} at {}", methodName, LocalDateTime.now().format(FMT));

        log.info("Entered method {} ", methodName);

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("Exiting method {}. time taken to execute {} ms", methodName, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            // ERROR level + pass exception object so stack trace prints
            log.error("Exception in method {} after {} ms: {}", methodName, duration, ex.getMessage(), ex);
            throw ex;
        }
//        finally {
//            long duration = System.currentTimeMillis() - start;
//            log.info("Exiting method {}. time taken to execute..... {} ms......",
//                    methodName, duration);
//        }
    }
}