package com.azat.h1.service;

import com.azat.h1.dto.AttachmentResponseDto;
import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.exception.AttachmentNotFoundException;
import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = "app.upload-dir=target/test-uploads")
class AttachmentServiceTests {

	@Autowired
	private AttachmentService attachmentService;

	@Autowired
	private TaskService taskService;

	@AfterEach
	void cleanUploads() throws Exception {
		Path uploadDirectory = attachmentService.getUploadDirectory();
		if (Files.exists(uploadDirectory)) {
			try (var paths = Files.walk(uploadDirectory)) {
				paths.sorted(Comparator.reverseOrder())
						.filter(path -> !path.equals(uploadDirectory))
						.forEach(path -> {
							try {
								Files.deleteIfExists(path);
							} catch (Exception ignored) {
								throw new IllegalStateException("Could not clean test upload file", ignored);
							}
						});
			}
		}
	}

	@Test
	void storesMetadataAndFile() throws Exception {
		Task task = taskService.createTask(new Task(null, "File service task", "Stores file", false, null,
				LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of("file")));
		MockMultipartFile file = new MockMultipartFile("file", "service.txt", "text/plain",
				"service".getBytes());

		AttachmentResponseDto response = attachmentService.storeAttachment(task.getId(), file);

		assertThat(response.getId()).isNotNull();
		assertThat(response.getTaskId()).isEqualTo(task.getId());
		assertThat(response.getFileName()).isEqualTo("service.txt");
		assertThat(attachmentService.loadAsResource(response.getId()).getContentAsByteArray())
				.isEqualTo("service".getBytes());
	}

	@Test
	void deletesMetadataAndFile() {
		Task task = taskService.createTask(new Task(null, "Delete file task", "Deletes file", false, null,
				LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of("file")));
		MockMultipartFile file = new MockMultipartFile("file", "delete.txt", "text/plain",
				"delete".getBytes());
		AttachmentResponseDto response = attachmentService.storeAttachment(task.getId(), file);

		attachmentService.deleteAttachment(response.getId());

		assertThatThrownBy(() -> attachmentService.getAttachment(response.getId()))
				.isInstanceOf(AttachmentNotFoundException.class);
	}

	@Test
	void dueDateUpdateBeforeCreationIsRejected() {
		Task task = taskService.createTask(new Task(null, "Future created task", "Reject old due date", false,
				LocalDateTime.now().plusDays(2), LocalDate.now().plusDays(3), Priority.MEDIUM, Set.of("date")));
		TaskUpdateDto updateDto = new TaskUpdateDto();
		updateDto.setDueDate(LocalDate.now());

		assertThatThrownBy(() -> taskService.validateUpdate(task, updateDto))
				.isInstanceOf(ConstraintViolationException.class);
	}
}
