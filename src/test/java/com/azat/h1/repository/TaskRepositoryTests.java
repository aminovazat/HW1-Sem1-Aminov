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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class TaskRepositoryTests {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TaskAttachmentRepository attachmentRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void findByCompletedAndPriorityReturnsMatchingTasks() {
		Task matchingTask = task("Completed high", true, Priority.HIGH, LocalDate.now().plusDays(1));
		Task otherTask = task("Open high", false, Priority.HIGH, LocalDate.now().plusDays(1));
		taskRepository.saveAll(List.of(matchingTask, otherTask));

		List<Task> tasks = taskRepository.findByCompletedAndPriority(true, Priority.HIGH);

		assertThat(tasks).extracting(Task::getTitle).contains("Completed high");
		assertThat(tasks).extracting(Task::getTitle).doesNotContain("Open high");
	}

	@Test
	void findDueBetweenReturnsTasksDueWithinRange() {
		taskRepository.save(task("Due soon", false, Priority.MEDIUM, LocalDate.now().plusDays(3)));
		taskRepository.save(task("Due later", false, Priority.MEDIUM, LocalDate.now().plusDays(20)));

		List<Task> tasks = taskRepository.findDueBetween(LocalDate.now(), LocalDate.now().plusDays(7));

		assertThat(tasks).extracting(Task::getTitle).contains("Due soon");
		assertThat(tasks).extracting(Task::getTitle).doesNotContain("Due later");
	}

	@Test
	void findAllWithAttachmentsLoadsTaskAttachments() {
		Task task = taskRepository.save(task("Attached task", false, Priority.LOW, LocalDate.now().plusDays(1)));
		attachmentRepository.save(new TaskAttachment(null, task, "notes.txt", "stored-notes.txt", "text/plain",
				5L, LocalDateTime.now()));
		entityManager.flush();
		entityManager.clear();

		List<Task> tasks = taskRepository.findAllWithAttachments();

		Task attachedTask = tasks.stream()
				.filter(item -> "Attached task".equals(item.getTitle()))
				.findFirst()
				.orElseThrow();
		assertThat(attachedTask.getAttachments()).hasSize(1);
		assertThat(attachedTask.getAttachments().get(0).getFileName()).isEqualTo("notes.txt");
	}

	private Task task(String title, boolean completed, Priority priority, LocalDate dueDate) {
		return new Task(null, title, "Repository test", completed, null, dueDate, priority, Set.of("jpa"));
	}
}
