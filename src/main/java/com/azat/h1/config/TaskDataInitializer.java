package com.azat.h1.config;

import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import com.azat.h1.repository.TaskRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Adds a few starter tasks for local development and integration tests.
 */
@Component
@Profile({"dev", "test"})
public class TaskDataInitializer implements ApplicationRunner {

	private final TaskRepository taskRepository;

	public TaskDataInitializer(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (taskRepository.count() > 0) {
			return;
		}

		taskRepository.saveAll(List.of(
				new Task(null, "Learn Spring", "Read about Spring Boot basics", false, null,
						LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of("spring")),
				new Task(null, "Write Tests", "Cover controller endpoints", false, null,
						LocalDate.now().plusDays(2), Priority.HIGH, Set.of("tests")),
				new Task(null, "Finish Homework", "Complete the To-Do List Manager", false, null,
						LocalDate.now().plusDays(3), Priority.HIGH, Set.of("homework"))
		));
	}
}
