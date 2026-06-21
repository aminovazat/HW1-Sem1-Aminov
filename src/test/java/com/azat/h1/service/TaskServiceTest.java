package com.azat.h1.service;

import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import com.azat.h1.repository.TaskRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = TaskService.class)
@ActiveProfiles("test")
class TaskServiceTest {

	@MockitoBean
	private TaskRepository taskRepository;

	@MockitoBean
	private Validator validator;

	@Autowired
	private TaskService taskService;

	@Test
	void updateTaskChangesCompletedStatusForExistingTask() {
		// given
		Long taskId = 1L;
		LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
		Task existingTask = new Task(taskId, "Write tests", "Cover service behavior", false, createdAt,
				LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of("testing"));
		Task updateRequest = new Task(null, "Write tests", "Cover service behavior", true, createdAt,
				LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of("testing"));

		when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
		when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		Optional<Task> result = taskService.updateTask(taskId, updateRequest);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().isCompleted()).isTrue();

		ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
		verify(taskRepository).findById(taskId);
		verify(taskRepository).save(taskCaptor.capture());
		assertThat(taskCaptor.getValue().getId()).isEqualTo(taskId);
		assertThat(taskCaptor.getValue().isCompleted()).isTrue();
	}
}
