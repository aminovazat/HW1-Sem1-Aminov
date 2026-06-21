package com.azat.h1.mapper;

import com.azat.h1.dto.TaskCreateDto;
import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.dto.TaskUpdateDto;
import com.azat.h1.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Maps task DTOs and internal task models.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "completed", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	Task toEntity(TaskCreateDto dto);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	void updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

	TaskResponseDto toResponseDto(Task task);

	List<TaskResponseDto> toResponseDtos(List<Task> tasks);
}
