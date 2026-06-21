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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceTransactionTests {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

	@Test
	void bulkCompleteTasksCompletesAllExistingTasks() {
		Task firstTask = taskService.createTask(task("Bulk complete one"));
		Task secondTask = taskService.createTask(task("Bulk complete two"));

		List<Task> completedTasks = taskService.bulkCompleteTasks(List.of(firstTask.getId(), secondTask.getId()));

		assertThat(completedTasks).extracting(Task::isCompleted).containsOnly(true);
		assertThat(taskRepository.findById(firstTask.getId())).get().extracting(Task::isCompleted).isEqualTo(true);
		assertThat(taskRepository.findById(secondTask.getId())).get().extracting(Task::isCompleted).isEqualTo(true);
	}

	@Test
	void bulkCompleteTasksRollsBackWhenAnyTaskIsMissing() {
		Task firstTask = taskService.createTask(task("Rollback one"));
		Task secondTask = taskService.createTask(task("Rollback two"));

		assertThatThrownBy(() -> taskService.bulkCompleteTasks(List.of(firstTask.getId(), 999999L, secondTask.getId())))
				.isInstanceOf(BulkTaskUpdateException.class);

		assertThat(taskRepository.findById(firstTask.getId())).get().extracting(Task::isCompleted).isEqualTo(false);
		assertThat(taskRepository.findById(secondTask.getId())).get().extracting(Task::isCompleted).isEqualTo(false);
	}

	private Task task(String title) {
		return new Task(null, title, "Transaction test", false, null, LocalDate.now().plusDays(1),
				Priority.MEDIUM, Set.of("transaction"));
	}
}
