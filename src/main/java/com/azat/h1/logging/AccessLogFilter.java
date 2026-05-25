package com.azat.h1.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs compact access records without request or authorization bodies.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AccessLogFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(AccessLogFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		long startNanos = System.nanoTime();
		try {
			filterChain.doFilter(request, response);
		} finally {
			long timeMs = (System.nanoTime() - startNanos) / 1_000_000;
			logger.info("HTTP {} {} -> status={} timeMs={} trace={}",
					request.getMethod(),
					request.getRequestURI(),
					response.getStatus(),
					timeMs,
					MDC.get(TraceIdFilter.TRACE_ID_KEY));
		}
	}
}
