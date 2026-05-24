package com.azat.h1.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskServiceLifecycleTests {

	@Autowired
	private TaskService taskService;

	@Test
	void postConstructFillsTaskCacheFromRepository() {
		assertThat(taskService.getTaskCacheSize()).isGreaterThanOrEqualTo(3);
	}

	@Test
	void preDestroyClearCacheEmptiesTaskCache() {
		taskService.clearCache();

		assertThat(taskService.getTaskCacheSize()).isZero();
		taskService.initCache();
	}
}
