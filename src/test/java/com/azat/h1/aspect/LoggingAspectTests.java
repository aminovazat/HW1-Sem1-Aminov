package com.azat.h1.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class LoggingAspectTests {

	private final LoggingAspect loggingAspect = new LoggingAspect();

	@Test
	void logServiceMethodLogsArgumentsAndResult(CapturedOutput output) throws Throwable {
		ProceedingJoinPoint joinPoint = joinPoint("TaskService.getAllTasks()", "argument");
		when(joinPoint.proceed()).thenReturn("result");

		Object result = loggingAspect.logServiceMethod(joinPoint);

		assertThat(result).isEqualTo("result");
		verify(joinPoint).getArgs();
		verify(joinPoint).proceed();
		assertThat(output).contains("Calling TaskService.getAllTasks() with arguments [argument]");
		assertThat(output).contains("TaskService.getAllTasks() returned result");
	}

	@Test
	void logServiceMethodAllowsAndLogsNullResult(CapturedOutput output) throws Throwable {
		ProceedingJoinPoint joinPoint = joinPoint("TaskService.clearCache()");
		when(joinPoint.proceed()).thenReturn(null);

		Object result = loggingAspect.logServiceMethod(joinPoint);

		assertThat(result).isNull();
		verify(joinPoint).proceed();
		assertThat(output).contains("TaskService.clearCache() returned null");
	}

	@Test
	void logServiceMethodLogsAndRethrowsException(CapturedOutput output) throws Throwable {
		ProceedingJoinPoint joinPoint = joinPoint("TaskService.failingMethod()");
		IllegalStateException exception = new IllegalStateException("boom");
		when(joinPoint.proceed()).thenThrow(exception);

		assertThatThrownBy(() -> loggingAspect.logServiceMethod(joinPoint))
				.isSameAs(exception);
		verify(joinPoint).getArgs();
		verify(joinPoint).proceed();
		assertThat(output).contains("TaskService.failingMethod() failed");
		assertThat(output).contains("boom");
	}

	private ProceedingJoinPoint joinPoint(String methodName, Object... args) {
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		Signature signature = mock(Signature.class);
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.toShortString()).thenReturn(methodName);
		when(joinPoint.getArgs()).thenReturn(args);
		return joinPoint;
	}
}
