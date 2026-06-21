package com.azat.h1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Holds data that is unique for a single HTTP request.
 */
@Component
@RequestScope
public class RequestScopedBean {

	private static final Logger logger = LoggerFactory.getLogger(RequestScopedBean.class);

	private final String requestId = UUID.randomUUID().toString();
	private final LocalDateTime createdAt = LocalDateTime.now();

	public RequestScopedBean() {
		logger.info("Created request scoped bean with requestId {}", requestId);
	}

	public String getRequestId() {
		return requestId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
