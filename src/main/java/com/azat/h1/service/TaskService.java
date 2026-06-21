package com.azat.h1.service;

import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.exception.TaskNotFoundException;
import com.azat.h1.model.Task;
import com.azat.h1.repository.TaskRepository;
import com.azat.h1.validation.TaskUpdateValidationCommand;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains business operations for managing tasks.
 */
@Service
public class TaskService {

	private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

	private final TaskRepository repository;
	private final Validator validator;
	private final Map<String, Task> taskCache = new ConcurrentHashMap<>();

	public TaskService(TaskRepository repository, Validator validator) {
		this.repository = repository;
		this.validator = validator;
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

	public Task getTaskOrThrow(Long id) {
		return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
	}

	public Task createTask(Task task) {
		Task savedTask = repository.save(task);
		taskCache.put(String.valueOf(savedTask.getId()), savedTask);
		return savedTask;
	}

	public Optional<Task> updateTask(Long id, Task task) {
		Optional<Task> updatedTask = repository.update(id, task);
		updatedTask.ifPresent(value -> taskCache.put(String.valueOf(id), value));
		return updatedTask;
	}

	public Task updateExistingTask(Long id, Task task) {
		Task updatedTask = repository.update(id, task).orElseThrow(() -> new TaskNotFoundException(id));
		taskCache.put(String.valueOf(id), updatedTask);
		return updatedTask;
	}

	public void validateUpdate(Task task, TaskUpdateDto updateDto) {
		Set<ConstraintViolation<TaskUpdateValidationCommand>> violations =
				validator.validate(new TaskUpdateValidationCommand(task, updateDto));
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
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
