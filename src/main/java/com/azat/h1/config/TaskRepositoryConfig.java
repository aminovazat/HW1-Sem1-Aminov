package com.azat.h1.config;

import com.azat.h1.model.Task;
import com.azat.h1.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Declares additional task repository beans used for demonstration.
 */
@Configuration
public class TaskRepositoryConfig {

	@Bean(name = "stubTaskRepository")
	public TaskRepository stubTaskRepository() {
		return new StubTaskRepository();
	}

	private static class StubTaskRepository implements TaskRepository {

		private final Map<Long, Task> tasks = new LinkedHashMap<>();

		StubTaskRepository() {
			tasks.put(1L, new Task(1L, "Learn Spring Boot", "Read about controllers and services", false));
			tasks.put(2L, new Task(2L, "Write homework", "Implement basic To-Do List Manager logic", true));
		}

		@Override
		public List<Task> findAll() {
			return new ArrayList<>(tasks.values());
		}

		@Override
		public Optional<Task> findById(Long id) {
			return Optional.ofNullable(tasks.get(id));
		}

		@Override
		public Task save(Task task) {
			tasks.put(task.getId(), task);
			return task;
		}

		@Override
		public Optional<Task> update(Long id, Task task) {
			if (!tasks.containsKey(id)) {
				return Optional.empty();
			}

			task.setId(id);
			tasks.put(id, task);
			return Optional.of(task);
		}

		@Override
		public boolean deleteById(Long id) {
			return tasks.remove(id) != null;
		}
	}
}
