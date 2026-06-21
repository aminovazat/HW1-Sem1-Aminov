package com.azat.h1.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Logs method calls in the service layer.
 */
@Aspect
@Component
public class LoggingAspect {

	private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

	@Around("execution(* com.azat.h1.service..*(..))")
	public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		String methodName = joinPoint.getSignature().toShortString();
		logger.info("Calling {} with arguments {}", methodName, Arrays.toString(joinPoint.getArgs()));

		try {
			Object result = joinPoint.proceed();
			logger.info("{} returned {}", methodName, result);
			return result;
		} catch (Throwable ex) {
			logger.error("{} failed", methodName, ex);
			throw ex;
		}
	}
}
