package com.azat.h1.controller;

import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.mapper.TaskMapper;
import com.azat.h1.service.FavoritesService;
import com.azat.h1.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes REST endpoints for session-based favorite tasks.
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {

	private final FavoritesService favoritesService;
	private final TaskService taskService;
	private final TaskMapper taskMapper;

	public FavoritesController(FavoritesService favoritesService, TaskService taskService, TaskMapper taskMapper) {
		this.favoritesService = favoritesService;
		this.taskService = taskService;
		this.taskMapper = taskMapper;
	}

	@Operation(summary = "Add task to favorites")
	@PostMapping("/{taskId}")
	public ResponseEntity<Void> addFavorite(@PathVariable Long taskId, HttpSession session) {
		taskService.getRequiredTask(taskId);
		favoritesService.addFavorite(taskId, session);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Remove task from favorites")
	@DeleteMapping("/{taskId}")
	public ResponseEntity<Void> removeFavorite(@PathVariable Long taskId, HttpSession session) {
		favoritesService.removeFavorite(taskId, session);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Get favorite tasks")
	@GetMapping
	public ResponseEntity<List<TaskResponseDto>> getFavorites(HttpSession session) {
		List<TaskResponseDto> favorites = favoritesService.getFavoriteIds(session).stream()
				.map(taskService::getRequiredTask)
				.map(taskMapper::toResponseDto)
				.toList();
		return ResponseEntity.ok(favorites);
	}
}
