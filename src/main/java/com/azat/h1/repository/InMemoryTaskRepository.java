package com.azat.h1.repository;

import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import com.azat.h1.service.PrototypeScopedBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores tasks in memory and serves as the primary task repository.
 */
@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {

	private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
	private final ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider;

	public InMemoryTaskRepository(ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider) {
		this.prototypeScopedBeanProvider = prototypeScopedBeanProvider;
		save(new Task(null, "Learn Spring", "Study Spring Boot basics", false));
		save(new Task(null, "Write Tests", "Cover REST endpoints with tests", false));
		save(new Task(null, "Finish Homework", "Complete the To-Do List Manager project", false));
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
		if (task.getId() == null) {
			task.setId(prototypeScopedBeanProvider.getObject().generateTaskId());
		}
		prepareForSave(task);

		tasks.put(task.getId(), task);
		return task;
	}

	@Override
	public Optional<Task> update(Long id, Task task) {
		if (!tasks.containsKey(id)) {
			return Optional.empty();
		}

		task.setId(id);
		Task existingTask = tasks.get(id);
		task.setCreatedAt(existingTask.getCreatedAt());
		prepareForSave(task);
		tasks.put(id, task);
		return Optional.of(task);
	}

	@Override
	public boolean deleteById(Long id) {
		return tasks.remove(id) != null;
	}

	private void prepareForSave(Task task) {
		if (task.getCreatedAt() == null) {
			task.setCreatedAt(LocalDateTime.now());
		}
		if (task.getPriority() == null) {
			task.setPriority(Priority.MEDIUM);
		}
		if (task.getTags() == null) {
			task.setTags(Set.of());
		} else {
			task.setTags(new LinkedHashSet<>(task.getTags()));
		}
	}
}
