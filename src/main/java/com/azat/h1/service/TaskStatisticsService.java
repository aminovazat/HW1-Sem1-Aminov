package com.azat.h1.service;

import com.azat.h1.repository.TaskRepository;
import com.azat.h1.repository.StubTaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Compares task data available in different repository implementations.
 */
@Service
public class TaskStatisticsService {

	private final TaskRepository primaryRepository;
	private final StubTaskRepository stubRepository;

	public TaskStatisticsService(TaskRepository primaryRepository,
			@Qualifier("stubTaskRepository") StubTaskRepository stubRepository) {
		this.primaryRepository = primaryRepository;
		this.stubRepository = stubRepository;
	}

	public String compareRepositories() {
		return "Primary repository tasks: " + primaryRepository.findAll().size()
				+ ", stub repository tasks: " + stubRepository.findAll().size();
	}
}
