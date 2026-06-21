package com.azat.h1.service;

import com.azat.h1.dto.AttachmentResponseDto;
import com.azat.h1.exception.AttachmentNotFoundException;
import com.azat.h1.exception.FileStorageException;
import com.azat.h1.model.Task;
import com.azat.h1.model.TaskAttachment;
import com.azat.h1.repository.TaskAttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 * Handles storing, loading, and deleting task attachments.
 */
@Service
public class AttachmentService {

	private static final Logger logger = LoggerFactory.getLogger(AttachmentService.class);

	private final TaskAttachmentRepository attachmentRepository;
	private final TaskService taskService;
	private final Path uploadDirectory;

	public AttachmentService(TaskAttachmentRepository attachmentRepository, TaskService taskService,
			@Value("${app.upload-dir:uploads}") String uploadDirectory) {
		this.attachmentRepository = attachmentRepository;
		this.taskService = taskService;
		this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
		createUploadDirectory();
	}

	@Transactional
	public AttachmentResponseDto storeAttachment(Long taskId, MultipartFile file) {
		Task task = taskService.getTaskOrThrow(taskId);
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Uploaded file must not be empty");
		}

		String originalFileName = cleanFileName(file.getOriginalFilename());
		String storedFileName = UUID.randomUUID() + "-" + originalFileName;
		Path targetFile = uploadDirectory.resolve(storedFileName).normalize();

		try (InputStream inputStream = file.getInputStream()) {
			Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			throw new FileStorageException("Could not store uploaded file", ex);
		}

		TaskAttachment attachment = new TaskAttachment(
				null,
				task,
				originalFileName,
				storedFileName,
				file.getContentType(),
				file.getSize(),
				LocalDateTime.now()
		);
		task.getAttachments().add(attachment);
		try {
			return toDto(attachmentRepository.save(attachment));
		} catch (RuntimeException ex) {
			deleteStoredFileQuietly(targetFile);
			throw ex;
		}
	}

	@Transactional(readOnly = true)
	public TaskAttachment getAttachment(Long attachmentId) {
		return attachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
	}

	@Transactional(readOnly = true)
	public Resource loadAsResource(Long attachmentId) {
		TaskAttachment attachment = getAttachment(attachmentId);
		try {
			Resource resource = new UrlResource(uploadDirectory.resolve(attachment.getStoredFileName()).toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new AttachmentNotFoundException(attachmentId);
			}
			return resource;
		} catch (MalformedURLException ex) {
			throw new FileStorageException("Could not load attachment", ex);
		}
	}

	@Transactional
	public void deleteAttachment(Long attachmentId) {
		TaskAttachment attachment = getAttachment(attachmentId);
		try {
			Files.deleteIfExists(uploadDirectory.resolve(attachment.getStoredFileName()));
		} catch (IOException ex) {
			throw new FileStorageException("Could not delete attachment file", ex);
		}
		attachmentRepository.delete(attachment);
	}

	@Transactional(readOnly = true)
	public List<AttachmentResponseDto> getTaskAttachments(Long taskId) {
		taskService.getTaskOrThrow(taskId);
		return attachmentRepository.findByTaskId(taskId).stream()
				.map(this::toDto)
				.toList();
	}

	public Path getUploadDirectory() {
		return uploadDirectory;
	}

	private AttachmentResponseDto toDto(TaskAttachment attachment) {
		AttachmentResponseDto dto = new AttachmentResponseDto();
		dto.setId(attachment.getId());
		dto.setTaskId(attachment.getTaskId());
		dto.setFileName(attachment.getFileName());
		dto.setContentType(attachment.getContentType());
		dto.setSize(attachment.getSize());
		dto.setUploadedAt(attachment.getUploadedAt());
		return dto;
	}

	private void createUploadDirectory() {
		try {
			Files.createDirectories(uploadDirectory);
		} catch (IOException ex) {
			throw new FileStorageException("Could not create upload directory", ex);
		}
	}

	private void deleteStoredFileQuietly(Path targetFile) {
		try {
			Files.deleteIfExists(targetFile);
		} catch (IOException ignored) {
			logger.warn("Could not clean up stored file {}", targetFile);
		}
	}

	private String cleanFileName(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return "file";
		}
		return Path.of(fileName).getFileName().toString();
	}
}
