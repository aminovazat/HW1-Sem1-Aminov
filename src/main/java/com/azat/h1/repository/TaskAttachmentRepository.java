package com.azat.h1.repository;

import com.azat.h1.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Provides CRUD operations for task attachment metadata.
 */
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

	List<TaskAttachment> findByTaskId(Long taskId);
}
