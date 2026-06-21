package com.azat.h1.repository;

import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Provides database-backed CRUD and query operations for tasks.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

	@Query("""
			select t from Task t
			where t.dueDate between :start and :end
			order by t.dueDate asc
			""")
	List<Task> findDueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

	@EntityGraph(attributePaths = "attachments")
	@Query("select distinct t from Task t")
	List<Task> findAllWithAttachments();
}
