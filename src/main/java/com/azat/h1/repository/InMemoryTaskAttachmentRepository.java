package com.azat.h1.repository;

import com.azat.h1.model.TaskAttachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Legacy in-memory attachment store kept out of the runtime Spring context.
 */
public class InMemoryTaskAttachmentRepository {

	private final Map<Long, TaskAttachment> attachments = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	public TaskAttachment save(TaskAttachment attachment) {
		if (attachment.getId() == null) {
			attachment.setId(idGenerator.getAndIncrement());
		}
		attachments.put(attachment.getId(), attachment);
		return attachment;
	}

	public Optional<TaskAttachment> findById(Long id) {
		return Optional.ofNullable(attachments.get(id));
	}

	public List<TaskAttachment> findByTaskId(Long taskId) {
		return attachments.values().stream()
				.filter(attachment -> taskId.equals(attachment.getTaskId()))
				.toList();
	}

	public boolean deleteById(Long id) {
		return attachments.remove(id) != null;
	}
}
