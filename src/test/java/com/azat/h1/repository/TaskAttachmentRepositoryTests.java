package com.azat.h1.repository;

import com.azat.h1.config.JpaConfig;
import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import com.azat.h1.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class TaskAttachmentRepositoryTests {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TaskAttachmentRepository attachmentRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void findByTaskIdReturnsSavedAttachments() {
		Task task = taskRepository.save(new Task(null, "Attachment owner", "Repository test", false, null,
				LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of("attachment")));
		attachmentRepository.save(new TaskAttachment(null, task, "file.txt", "stored-file.txt", "text/plain",
				4L, LocalDateTime.now()));

		assertThat(attachmentRepository.findByTaskId(task.getId()))
				.hasSize(1)
				.first()
				.extracting(TaskAttachment::getFileName)
				.isEqualTo("file.txt");
	}

	@Test
	void deletingTaskRemovesAttachmentsByCascade() {
		Task task = taskRepository.save(new Task(null, "Cascade owner", "Repository test", false, null,
				LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of("attachment")));
		TaskAttachment attachment = attachmentRepository.save(new TaskAttachment(null, task, "delete.txt",
				"stored-delete.txt", "text/plain", 6L, LocalDateTime.now()));
		entityManager.flush();
		entityManager.clear();

		taskRepository.deleteById(task.getId());
		entityManager.flush();
		entityManager.clear();

		assertThat(attachmentRepository.findById(attachment.getId())).isEmpty();
	}
}
