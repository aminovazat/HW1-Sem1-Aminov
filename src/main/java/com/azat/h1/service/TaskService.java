package com.azat.h1.service;

import com.azat.h1.exception.TaskNotFoundException;
import com.azat.h1.model.Task;
import com.azat.h1.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains business operations for managing tasks.
 */
@Service
public class TaskService {

	private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

	private final TaskRepository repository;
	private final Map<String, Task> taskCache = new ConcurrentHashMap<>();

	public TaskService(TaskRepository repository) {
		this.repository = repository;
	}

	@PostConstruct
	public void initCache() {
		repository.findAll().forEach(task -> taskCache.put(String.valueOf(task.getId()), task));
		logger.info("Task cache initialized with {} tasks", taskCache.size());
	}

	@PreDestroy
	public void clearCache() {
		logger.info("Clearing task cache with {} tasks", taskCache.size());
		taskCache.clear();
	}

	public List<Task> getAllTasks() {
		return repository.findAll();
	}

	public Optional<Task> getTaskById(Long id) {
		return repository.findById(id);
	}

	public Task getRequiredTask(Long id) {
		return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
	}

	public Task createTask(Task task) {
		Task savedTask = repository.save(task);
		taskCache.put(String.valueOf(savedTask.getId()), savedTask);
		return savedTask;
	}

	public Optional<Task> updateTask(Long id, Task task) {
		Task existingTask = getRequiredTask(id);
		if (task.getDueDate() != null
				&& existingTask.getCreatedAt() != null
				&& task.getDueDate().isBefore(existingTask.getCreatedAt().toLocalDate())) {
			throw new IllegalArgumentException("dueDate must not be before task creation date");
		}
		task.setCreatedAt(existingTask.getCreatedAt());
		Optional<Task> updatedTask = repository.update(id, task);
		updatedTask.ifPresent(value -> taskCache.put(String.valueOf(id), value));
		return updatedTask;
	}

	public boolean deleteTask(Long id) {
		boolean deleted = repository.deleteById(id);
		if (deleted) {
			taskCache.remove(String.valueOf(id));
		}
		return deleted;
	}

	int getTaskCacheSize() {
		return taskCache.size();
	}
}
