package com.azat.h1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Generates task identifiers from a new prototype-scoped bean instance.
 */
@Component
@Scope("prototype")
public class PrototypeScopedBean {

	private static final Logger logger = LoggerFactory.getLogger(PrototypeScopedBean.class);

	public PrototypeScopedBean() {
		logger.info("Created prototype scoped bean");
	}

	/**
	 * Generates a positive numeric task id from a UUID while keeping the Task id type as Long.
	 *
	 * @return generated task id
	 */
	public Long generateTaskId() {
		return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
	}
}
