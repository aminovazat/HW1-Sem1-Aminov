package com.azat.h1.repository;

import com.azat.h1.model.Task;
import com.azat.h1.model.Priority;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

/**
 * Provides CRUD operations for tasks.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

	@Query("""
			select task
			from Task task
			where task.dueDate between :from and :to
			order by task.dueDate asc
			""")
	List<Task> findTasksDueBetween(LocalDate from, LocalDate to);

	@EntityGraph(attributePaths = "attachments")
	@Query("select task from Task task")
	List<Task> findAllWithAttachments();
}
