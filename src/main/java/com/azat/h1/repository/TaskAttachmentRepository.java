package com.azat.h1.repository;

import com.azat.h1.model.TaskAttachment;

import java.util.List;
import java.util.Optional;

/**
 * Provides CRUD operations for task attachment metadata.
 */
public interface TaskAttachmentRepository {

	TaskAttachment save(TaskAttachment attachment);

	Optional<TaskAttachment> findById(Long id);

	List<TaskAttachment> findByTaskId(Long taskId);

	boolean deleteById(Long id);
}
