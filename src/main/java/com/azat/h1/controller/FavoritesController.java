package com.azat.h1.controller;

import com.azat.h1.dto.TaskResponseDto;
import com.azat.h1.mapper.TaskMapper;
import com.azat.h1.service.FavoritesService;
import com.azat.h1.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
 * Exposes session-based favorite task endpoints.
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

	@Operation(summary = "Add a task to favorites")
	@ApiResponse(responseCode = "204", description = "Favorite added")
	@ApiResponse(responseCode = "404", description = "Task not found")
	@PostMapping("/{taskId}")
	public ResponseEntity<Void> addFavorite(@PathVariable Long taskId, HttpSession session) {
		favoritesService.addFavorite(taskId, session);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Remove a task from favorites")
	@ApiResponse(responseCode = "204", description = "Favorite removed")
	@DeleteMapping("/{taskId}")
	public ResponseEntity<Void> removeFavorite(@PathVariable Long taskId, HttpSession session) {
		favoritesService.removeFavorite(taskId, session);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Get favorite tasks")
	@ApiResponse(responseCode = "200", description = "Favorite tasks returned")
	@GetMapping
	public ResponseEntity<List<TaskResponseDto>> getFavorites(HttpSession session) {
		List<TaskResponseDto> favorites = favoritesService.getFavoriteIds(session).stream()
				.flatMap(taskId -> taskService.getTaskById(taskId).stream())
				.map(taskMapper::toResponseDto)
				.toList();
		return ResponseEntity.ok(favorites);
	}
}
