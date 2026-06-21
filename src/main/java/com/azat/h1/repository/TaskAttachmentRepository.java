package com.azat.h1.repository;

import com.azat.h1.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Provides database-backed CRUD operations for task attachment metadata.
 */
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

	@Query("select attachment from TaskAttachment attachment where attachment.task.id = :taskId")
	List<TaskAttachment> findByTaskId(@Param("taskId") Long taskId);

	@Query("""
			select attachment
			from TaskAttachment attachment
			where attachment.id = :id and attachment.task.id = :taskId
			""")
	Optional<TaskAttachment> findByIdAndTaskId(@Param("id") Long id, @Param("taskId") Long taskId);
}
