package com.azat.h1.service;

import com.azat.h1.exception.BulkTaskUpdateException;
import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import com.azat.h1.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TaskJpaIntegrationTests {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TaskStatisticsJdbcService statisticsJdbcService;

	@Test
	void bulkCompleteTasksMarksAllExistingTasksCompleted() {
		Task first = taskService.createTask(newTask("Bulk first", Priority.LOW));
		Task second = taskService.createTask(newTask("Bulk second", Priority.HIGH));

		List<Task> updatedTasks = taskService.bulkCompleteTasks(List.of(first.getId(), second.getId()));

		assertThat(updatedTasks).extracting(Task::isCompleted).containsOnly(true);
		assertThat(taskRepository.findById(first.getId())).get().extracting(Task::isCompleted).isEqualTo(true);
		assertThat(taskRepository.findById(second.getId())).get().extracting(Task::isCompleted).isEqualTo(true);
	}

	@Test
	void bulkCompleteTasksRollsBackWhenAnyTaskIsMissing() {
		Task first = taskService.createTask(newTask("Rollback first", Priority.LOW));
		Task second = taskService.createTask(newTask("Rollback second", Priority.HIGH));

		assertThatThrownBy(() -> taskService.bulkCompleteTasks(List.of(first.getId(), 999999L, second.getId())))
				.isInstanceOf(BulkTaskUpdateException.class);

		assertThat(taskRepository.findById(first.getId())).get().extracting(Task::isCompleted).isEqualTo(false);
		assertThat(taskRepository.findById(second.getId())).get().extracting(Task::isCompleted).isEqualTo(false);
	}

	@Test
	void jdbcStatisticsReturnsTaskCountsByPriority() {
		taskService.createTask(newTask("Stats low", Priority.LOW));
		taskService.createTask(newTask("Stats high", Priority.HIGH));

		Map<Priority, Long> counts = statisticsJdbcService.getTasksCountByPriority();

		assertThat(counts.get(Priority.LOW)).isGreaterThanOrEqualTo(1);
		assertThat(counts.get(Priority.HIGH)).isGreaterThanOrEqualTo(1);
	}

	private Task newTask(String title, Priority priority) {
		Task task = new Task();
		task.setTitle(title);
		task.setDescription("Integration test task");
		task.setDueDate(LocalDate.now().plusDays(1));
		task.setPriority(priority);
		task.setTags(Set.of("integration"));
		return task;
	}
}
