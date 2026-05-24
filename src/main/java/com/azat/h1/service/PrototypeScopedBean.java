package com.azat.h1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates task identifiers from a prototype-scoped bean instance.
 */
@Component
@Scope("prototype")
public class PrototypeScopedBean {

	private static final Logger logger = LoggerFactory.getLogger(PrototypeScopedBean.class);
	private static final AtomicLong idGenerator = new AtomicLong(1);

	public PrototypeScopedBean() {
		logger.info("Created prototype scoped bean");
	}

	public Long generateTaskId() {
		return idGenerator.getAndIncrement();
	}
}
