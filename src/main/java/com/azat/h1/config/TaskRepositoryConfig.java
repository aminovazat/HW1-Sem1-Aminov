package com.azat.h1.config;

import com.azat.h1.repository.StubTaskRepository;
import com.azat.h1.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares additional task repository beans used for demonstration.
 */
@Configuration
public class TaskRepositoryConfig {

	@Bean(name = "stubTaskRepository")
	public TaskRepository stubTaskRepository() {
		return new StubTaskRepository();
	}
}
