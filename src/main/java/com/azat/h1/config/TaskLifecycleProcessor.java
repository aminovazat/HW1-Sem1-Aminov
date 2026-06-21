package com.azat.h1.config;

import com.azat.h1.repository.TaskRepository;
import com.azat.h1.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Logs initialization lifecycle events for task-related beans.
 */
@Component
public class TaskLifecycleProcessor implements BeanPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(TaskLifecycleProcessor.class);

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (isTaskBean(bean)) {
			logger.info("Before initialization of bean '{}': {}", beanName, bean.getClass().getSimpleName());
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (isTaskBean(bean)) {
			logger.info("After initialization of bean '{}': {}", beanName, bean.getClass().getSimpleName());
		}
		return bean;
	}

	private boolean isTaskBean(Object bean) {
		return bean instanceof TaskService || bean instanceof TaskRepository;
	}
}
