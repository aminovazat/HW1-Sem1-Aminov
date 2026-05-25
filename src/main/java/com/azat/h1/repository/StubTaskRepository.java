package com.azat.h1.repository;

import com.azat.h1.model.Task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides fixed task data for comparing runtime repositories.
 */
public class StubTaskRepository {

	private final Map<Long, Task> tasks = new LinkedHashMap<>();

	public StubTaskRepository() {
		tasks.put(1L, new Task(1L, "Learn Spring Boot", "Read about controllers and services", false));
		tasks.put(2L, new Task(2L, "Write homework", "Implement basic To-Do List Manager logic", true));
	}

	public List<Task> findAll() {
		return new ArrayList<>(tasks.values());
	}
}
