package com.azat.h1.service;

import com.azat.h1.dto.AttachmentResponseDto;
import com.azat.h1.exception.AttachmentNotFoundException;
import com.azat.h1.model.TaskAttachment;
import com.azat.h1.repository.TaskAttachmentRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Stores, loads, and deletes task attachments.
 */
@Service
public class AttachmentService {

	private final TaskService taskService;
	private final TaskAttachmentRepository attachmentRepository;
	private final Path uploadDirectory = Path.of("uploads");

	public AttachmentService(TaskService taskService, TaskAttachmentRepository attachmentRepository) {
		this.taskService = taskService;
		this.attachmentRepository = attachmentRepository;
	}

	public AttachmentResponseDto storeAttachment(Long taskId, MultipartFile file) {
		taskService.getRequiredTask(taskId);
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("file must not be empty");
		}

		try {
			Files.createDirectories(uploadDirectory);
			String originalFileName = file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename();
			String storedFileName = UUID.randomUUID() + "-" + originalFileName;
			Path destination = uploadDirectory.resolve(storedFileName).normalize();

			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
			}

			TaskAttachment attachment = new TaskAttachment(
					null,
					taskId,
					originalFileName,
					storedFileName,
					file.getContentType(),
					file.getSize(),
					LocalDateTime.now()
			);
			return toResponseDto(attachmentRepository.save(attachment));
		} catch (IOException ex) {
			throw new IllegalStateException("Could not store attachment", ex);
		}
	}

	public TaskAttachment getAttachment(Long attachmentId) {
		return attachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
	}

	public Resource loadAsResource(Long attachmentId) {
		TaskAttachment attachment = getAttachment(attachmentId);
		try {
			Resource resource = new UrlResource(uploadDirectory.resolve(attachment.getStoredFileName()).toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new AttachmentNotFoundException(attachmentId);
			}
			return resource;
		} catch (MalformedURLException ex) {
			throw new AttachmentNotFoundException(attachmentId);
		}
	}

	public boolean deleteAttachment(Long attachmentId) {
		TaskAttachment attachment = getAttachment(attachmentId);
		try {
			Files.deleteIfExists(uploadDirectory.resolve(attachment.getStoredFileName()));
		} catch (IOException ex) {
			throw new IllegalStateException("Could not delete attachment file", ex);
		}
		return attachmentRepository.deleteById(attachmentId);
	}

	public List<AttachmentResponseDto> getTaskAttachments(Long taskId) {
		taskService.getRequiredTask(taskId);
		return attachmentRepository.findByTaskId(taskId).stream()
				.map(this::toResponseDto)
				.toList();
	}

	private AttachmentResponseDto toResponseDto(TaskAttachment attachment) {
		AttachmentResponseDto dto = new AttachmentResponseDto();
		dto.setId(attachment.getId());
		dto.setFileName(attachment.getFileName());
		dto.setSize(attachment.getSize());
		dto.setUploadedAt(attachment.getUploadedAt());
		return dto;
	}
}
