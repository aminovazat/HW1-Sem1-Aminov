package com.azat.h1.repository;

import com.azat.h1.model.Task;
import com.azat.h1.model.Priority;
import com.azat.h1.service.PrototypeScopedBean;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Legacy in-memory task store kept out of the runtime Spring context.
 */
public class InMemoryTaskRepository {

	private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
	private final ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider;

	public InMemoryTaskRepository(ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider) {
		this.prototypeScopedBeanProvider = prototypeScopedBeanProvider;
		save(new Task(null, "Learn Spring", "Study Spring Boot basics", false));
		save(new Task(null, "Write Tests", "Cover REST endpoints with tests", false));
		save(new Task(null, "Finish Homework", "Complete the To-Do List Manager project", false));
	}

	public List<Task> findAll() {
		return new ArrayList<>(tasks.values());
	}

	public Optional<Task> findById(Long id) {
		return Optional.ofNullable(tasks.get(id));
	}

	public Task save(Task task) {
		if (task.getId() == null) {
			task.setId(prototypeScopedBeanProvider.getObject().generateTaskId());
		}
		if (task.getCreatedAt() == null) {
			task.setCreatedAt(LocalDateTime.now());
		}
		task.setLastModifiedAt(LocalDateTime.now());
		if (task.getPriority() == null) {
			task.setPriority(Priority.MEDIUM);
		}
		if (task.getTags() == null) {
			task.setTags(new HashSet<>());
		}

		tasks.put(task.getId(), task);
		return task;
	}

	public Optional<Task> update(Long id, Task task) {
		if (!tasks.containsKey(id)) {
			return Optional.empty();
		}

		task.setId(id);
		tasks.put(id, task);
		return Optional.of(task);
	}

	public boolean deleteById(Long id) {
		return tasks.remove(id) != null;
	}
}
