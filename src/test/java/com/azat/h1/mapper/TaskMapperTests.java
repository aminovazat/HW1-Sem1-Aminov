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
	void mapsCreateDtoToEntity() {
		TaskCreateDto dto = new TaskCreateDto();
		dto.setTitle("Mapped task");
		dto.setDescription("Mapped description");
		dto.setDueDate(LocalDate.now().plusDays(1));
		dto.setPriority(Priority.HIGH);
		dto.setTags(Set.of("map"));

		Task task = taskMapper.toEntity(dto);

		assertThat(task.getTitle()).isEqualTo("Mapped task");
		assertThat(task.getDescription()).isEqualTo("Mapped description");
		assertThat(task.getDueDate()).isEqualTo(dto.getDueDate());
		assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
		assertThat(task.getTags()).containsExactly("map");
	}

	@Test
	void updateEntityIgnoresNullValues() {
		Task task = new Task(1L, "Original", "Original description", false,
				LocalDateTime.now(), LocalDate.now().plusDays(1), Priority.LOW, Set.of("old"));
		TaskUpdateDto dto = new TaskUpdateDto();
		dto.setDescription("Updated description");
		dto.setCompleted(true);

		taskMapper.updateEntity(dto, task);

		assertThat(task.getTitle()).isEqualTo("Original");
		assertThat(task.getDescription()).isEqualTo("Updated description");
		assertThat(task.isCompleted()).isTrue();
		assertThat(task.getPriority()).isEqualTo(Priority.LOW);
		assertThat(task.getTags()).containsExactly("old");
	}

	@Test
	void mapsEntityToResponseDto() {
		LocalDateTime createdAt = LocalDateTime.now();
		Task task = new Task(1L, "Response task", "Response description", true,
				createdAt, LocalDate.now().plusDays(2), Priority.MEDIUM, Set.of("response"));

		TaskResponseDto dto = taskMapper.toResponseDto(task);

		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getTitle()).isEqualTo("Response task");
		assertThat(dto.isCompleted()).isTrue();
		assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
		assertThat(dto.getPriority()).isEqualTo(Priority.MEDIUM);
		assertThat(dto.getTags()).containsExactly("response");
	}
}
