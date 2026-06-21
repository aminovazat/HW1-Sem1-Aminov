package com.azat.h1.service;

import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceLifecycleTests {

	@Autowired
	private TaskService taskService;

	@Test
	void postConstructFillsTaskCacheFromRepository() {
		taskService.createTask(new Task(null, "Cache task", "Reloaded into cache", false, null,
				LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of("cache")));
		taskService.initCache();

		assertThat(taskService.getTaskCacheSize()).isGreaterThanOrEqualTo(1);
	}

	@Test
	void preDestroyClearCacheEmptiesTaskCache() {
		taskService.clearCache();

		assertThat(taskService.getTaskCacheSize()).isZero();
		taskService.initCache();
	}
}
