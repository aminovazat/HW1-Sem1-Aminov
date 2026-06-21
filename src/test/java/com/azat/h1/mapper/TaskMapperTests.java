package com.azat.h1.mapper;

import com.azat.h1.dto.TaskCreateDto;
import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskMapperTests {

	@Autowired
	private TaskMapper taskMapper;

	@Test
	void createDtoMapsToTask() {
		TaskCreateDto dto = new TaskCreateDto();
		dto.setTitle("Mapped task");
		dto.setDescription("Mapped description");
		dto.setDueDate(LocalDate.now().plusDays(1));
		dto.setPriority(Priority.HIGH);
		dto.setTags(Set.of("mapper"));

		Task task = taskMapper.toEntity(dto);

		assertThat(task.getTitle()).isEqualTo("Mapped task");
		assertThat(task.getDescription()).isEqualTo("Mapped description");
		assertThat(task.getDueDate()).isEqualTo(dto.getDueDate());
		assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
		assertThat(task.getTags()).containsExactly("mapper");
		assertThat(task.getId()).isNull();
		assertThat(task.getCreatedAt()).isNull();
	}

	@Test
	void taskMapsToResponseDto() {
		LocalDateTime createdAt = LocalDateTime.now();
		Task task = new Task(10L, "Response task", "Response description", true, createdAt,
				LocalDate.now().plusDays(2), Priority.LOW, Set.of("response"));

		TaskResponseDto dto = taskMapper.toResponseDto(task);

		assertThat(dto.getId()).isEqualTo(10L);
		assertThat(dto.getTitle()).isEqualTo("Response task");
		assertThat(dto.isCompleted()).isTrue();
		assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
		assertThat(dto.getPriority()).isEqualTo(Priority.LOW);
	}

	@Test
	void updateDtoDoesNotOverwriteExistingFieldsWithNulls() {
		Task task = new Task(1L, "Existing title", "Existing description", false, LocalDateTime.now(),
				LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of("old"));
		TaskUpdateDto dto = new TaskUpdateDto();
		dto.setCompleted(true);

		taskMapper.updateEntity(dto, task);

		assertThat(task.getTitle()).isEqualTo("Existing title");
		assertThat(task.getDescription()).isEqualTo("Existing description");
		assertThat(task.getDueDate()).isNotNull();
		assertThat(task.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(task.getTags()).containsExactly("old");
		assertThat(task.isCompleted()).isTrue();
	}
}
