package com.azat.h1.repository;

import com.azat.h1.model.Task;

import java.util.List;
import java.util.Optional;

/**
 * Provides CRUD operations for tasks.
 */
public interface TaskRepository {

	List<Task> findAll();

	Optional<Task> findById(Long id);

	Task save(Task task);

	Optional<Task> update(Long id, Task task);

	boolean deleteById(Long id);
}
