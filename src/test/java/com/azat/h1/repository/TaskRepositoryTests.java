package com.azat.h1.repository;

import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import com.azat.h1.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTests {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TaskAttachmentRepository attachmentRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void findByCompletedAndPriorityReturnsMatchingTasks() {
		Task task = saveTask("High completed", true, Priority.HIGH, LocalDate.now().plusDays(2));

		assertThat(taskRepository.findByCompletedAndPriority(true, Priority.HIGH))
				.extracting(Task::getId)
				.contains(task.getId());
	}

	@Test
	void findTasksDueBetweenReturnsTasksDueInNextSevenDays() {
		Task dueSoon = saveTask("Due soon", false, Priority.MEDIUM, LocalDate.now().plusDays(3));
		Task dueLater = saveTask("Due later", false, Priority.MEDIUM, LocalDate.now().plusDays(9));

		assertThat(taskRepository.findTasksDueBetween(LocalDate.now(), LocalDate.now().plusDays(7)))
				.extracting(Task::getId)
				.contains(dueSoon.getId())
				.doesNotContain(dueLater.getId());
	}

	@Test
	void savesAttachmentAndLoadsTaskWithAttachments() {
		Task task = saveTask("Attached task", false, Priority.LOW, LocalDate.now().plusDays(1));
		TaskAttachment attachment = new TaskAttachment(
				null,
				task.getId(),
				"note.txt",
				"stored-note.txt",
				"text/plain",
				12L,
				LocalDateTime.now()
		);
		attachmentRepository.saveAndFlush(attachment);
		entityManager.clear();

		Task fetchedTask = taskRepository.findAllWithAttachments().stream()
				.filter(candidate -> candidate.getId().equals(task.getId()))
				.findFirst()
				.orElseThrow();

		assertThat(fetchedTask.getAttachments())
				.extracting(TaskAttachment::getFileName)
				.containsExactly("note.txt");
		assertThat(attachmentRepository.findByTaskId(task.getId())).hasSize(1);
	}

	private Task saveTask(String title, boolean completed, Priority priority, LocalDate dueDate) {
		Task task = new Task(null, title, "Repository test task", completed,
				LocalDateTime.now(), dueDate, priority, Set.of("jpa"));
		return taskRepository.saveAndFlush(task);
	}
}
