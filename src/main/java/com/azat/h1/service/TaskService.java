package com.azat.h1.service;

import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.exception.BulkTaskUpdateException;
import com.azat.h1.exception.TaskNotFoundException;
import com.azat.h1.model.Priority;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
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
		taskCache.clear();
		repository.findAll().forEach(task -> taskCache.put(String.valueOf(task.getId()), task));
		logger.info("Task cache initialized with {} tasks", taskCache.size());
	}

	@PreDestroy
	public void clearCache() {
		logger.info("Clearing task cache with {} tasks", taskCache.size());
		taskCache.clear();
	}

	@Transactional(readOnly = true)
	public List<Task> getAllTasks() {
		return repository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<Task> getTaskById(Long id) {
		return repository.findById(id);
	}

	@Transactional(readOnly = true)
	public Task getTaskOrThrow(Long id) {
		return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
	}

	@Transactional
	public Task createTask(Task task) {
		task.setId(null);
		prepareTaskDefaults(task);
		Task savedTask = repository.save(task);
		taskCache.put(String.valueOf(savedTask.getId()), savedTask);
		return savedTask;
	}

	@Transactional
	public Optional<Task> updateTask(Long id, Task task) {
		return repository.findById(id).map(existingTask -> {
			copyTaskFields(task, existingTask);
			Task updatedTask = repository.save(existingTask);
			taskCache.put(String.valueOf(id), updatedTask);
			return updatedTask;
		});
	}

	@Transactional
	public Task updateExistingTask(Long id, Task task) {
		if (!repository.existsById(id)) {
			throw new TaskNotFoundException(id);
		}
		task.setId(id);
		prepareTaskDefaults(task);
		Task updatedTask = repository.save(task);
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

	@Transactional
	public boolean deleteTask(Long id) {
		if (!repository.existsById(id)) {
			return false;
		}
		repository.deleteById(id);
		taskCache.remove(String.valueOf(id));
		return true;
	}

	@Transactional(readOnly = true)
	public List<Task> getTasksDueWithinNextSevenDays() {
		LocalDate today = LocalDate.now();
		return repository.findDueBetween(today, today.plusDays(7));
	}

	@Transactional(readOnly = true)
	public List<Task> getTasksWithAttachments() {
		return repository.findAllWithAttachments();
	}

	@Transactional(
			propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED,
			rollbackFor = BulkTaskUpdateException.class
	)
	public List<Task> bulkCompleteTasks(List<Long> ids) {
		List<Task> tasks = repository.findAllById(ids);
		Set<Long> foundIds = new HashSet<>();
		tasks.forEach(task -> foundIds.add(task.getId()));

		List<Long> missingIds = ids.stream()
				.filter(id -> !foundIds.contains(id))
				.toList();
		if (!missingIds.isEmpty()) {
			throw new BulkTaskUpdateException(missingIds);
		}

		tasks.forEach(task -> task.setCompleted(true));
		List<Task> savedTasks = repository.saveAll(tasks);
		savedTasks.forEach(task -> taskCache.put(String.valueOf(task.getId()), task));
		return savedTasks;
	}

	int getTaskCacheSize() {
		return taskCache.size();
	}

	private void copyTaskFields(Task source, Task target) {
		target.setTitle(source.getTitle());
		target.setDescription(source.getDescription());
		target.setCompleted(source.isCompleted());
		target.setDueDate(source.getDueDate());
		target.setPriority(source.getPriority());
		target.setTags(source.getTags());
		prepareTaskDefaults(target);
	}

	private void prepareTaskDefaults(Task task) {
		if (task.getCreatedAt() == null) {
			task.setCreatedAt(LocalDateTime.now());
		}
		if (task.getPriority() == null) {
			task.setPriority(Priority.MEDIUM);
		}
		if (task.getTags() == null) {
			task.setTags(Set.of());
		}
	}
}
