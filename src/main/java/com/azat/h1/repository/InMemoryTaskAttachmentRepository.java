package com.azat.h1.repository;

import com.azat.h1.model.TaskAttachment;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores task attachment metadata in memory.
 */
@Repository
public class InMemoryTaskAttachmentRepository implements TaskAttachmentRepository {

	private final Map<Long, TaskAttachment> attachments = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	@Override
	public TaskAttachment save(TaskAttachment attachment) {
		if (attachment.getId() == null) {
			attachment.setId(idGenerator.getAndIncrement());
		}
		attachments.put(attachment.getId(), attachment);
		return attachment;
	}

	@Override
	public Optional<TaskAttachment> findById(Long id) {
		return Optional.ofNullable(attachments.get(id));
	}

	@Override
	public List<TaskAttachment> findByTaskId(Long taskId) {
		return attachments.values().stream()
				.filter(attachment -> taskId.equals(attachment.getTaskId()))
				.sorted(Comparator.comparing(TaskAttachment::getId))
				.toList();
	}

	@Override
	public boolean deleteById(Long id) {
		return attachments.remove(id) != null;
	}
}
