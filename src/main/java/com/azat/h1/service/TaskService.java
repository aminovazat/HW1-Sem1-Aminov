package com.azat.h1.service;

import com.azat.h1.exception.BulkTaskUpdateException;
import com.azat.h1.exception.TaskNotFoundException;
import com.azat.h1.model.Task;
import com.azat.h1.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

	public List<Task> getTasksWithAttachments() {
		return repository.findAllWithAttachments();
	}

	public Task createTask(Task task) {
		prepareNewTask(task);
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

		existingTask.setTitle(task.getTitle());
		existingTask.setDescription(task.getDescription());
		existingTask.setCompleted(task.isCompleted());
		existingTask.setDueDate(task.getDueDate());
		existingTask.setPriority(task.getPriority());
		existingTask.setTags(task.getTags());

		Task updatedTask = repository.save(existingTask);
		taskCache.put(String.valueOf(id), updatedTask);
		return Optional.of(updatedTask);
	}

	public boolean deleteTask(Long id) {
		if (!repository.existsById(id)) {
			return false;
		}
		repository.deleteById(id);
		taskCache.remove(String.valueOf(id));
		return true;
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
		List<Task> updatedTasks = repository.saveAll(tasks);
		updatedTasks.forEach(task -> taskCache.put(String.valueOf(task.getId()), task));
		return updatedTasks;
	}

	int getTaskCacheSize() {
		return taskCache.size();
	}

	private void prepareNewTask(Task task) {
		if (task.getPriority() == null) {
			task.setPriority(com.azat.h1.model.Priority.MEDIUM);
		}
		if (task.getTags() == null) {
			task.setTags(Set.of());
		}
		if (task.getDueDate() != null && task.getCreatedAt() != null
				&& task.getDueDate().isBefore(task.getCreatedAt().toLocalDate())) {
			throw new IllegalArgumentException("dueDate must not be before task creation date");
		}
		if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("dueDate must not be in the past");
		}
	}
}
