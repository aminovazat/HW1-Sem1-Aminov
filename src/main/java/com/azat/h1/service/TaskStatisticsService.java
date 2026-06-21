package com.azat.h1.service;

import com.azat.h1.repository.TaskRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Provides simple task statistics and demonstrates named, scoped, and property-based injection.
 */
@Service
public class TaskStatisticsService {

	private final TaskRepository primaryRepository;
	private final RequestScopedBean requestScopedBean;
	private final ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider;
	private final String appName;
	private final String appVersion;

	public TaskStatisticsService(TaskRepository primaryRepository,
			RequestScopedBean requestScopedBean,
			ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider,
			@Value("${app.name:To-Do List Manager}") String appName,
			@Value("${app.version:1.0.0}") String appVersion) {
		this.primaryRepository = primaryRepository;
		this.requestScopedBean = requestScopedBean;
		this.prototypeScopedBeanProvider = prototypeScopedBeanProvider;
		this.appName = appName;
		this.appVersion = appVersion;
	}

	public String compareRepositories() {
		return appName + " " + appVersion
				+ ". Primary repository tasks: " + primaryRepository.count()
				+ ", persistence: JPA";
	}

	public String getScopeDetails() {
		Long generatedTaskId = prototypeScopedBeanProvider.getObject().generateTaskId();
		return "requestId=" + requestScopedBean.getRequestId()
				+ ", requestStartedAt=" + requestScopedBean.getCreatedAt()
				+ ", prototypeTaskId=" + generatedTaskId;
	}
}
