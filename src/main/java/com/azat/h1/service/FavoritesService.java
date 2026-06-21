package com.azat.h1.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stores favorite task ids in the current HTTP session.
 */
@Service
public class FavoritesService {

	public static final String FAVORITE_TASK_IDS = "favoriteTaskIds";

	private final TaskService taskService;

	public FavoritesService(TaskService taskService) {
		this.taskService = taskService;
	}

	public void addFavorite(Long taskId, HttpSession session) {
		taskService.getTaskOrThrow(taskId);
		getMutableFavoriteIds(session).add(taskId);
	}

	public void removeFavorite(Long taskId, HttpSession session) {
		getMutableFavoriteIds(session).remove(taskId);
	}

	public Set<Long> getFavoriteIds(HttpSession session) {
		return new LinkedHashSet<>(getMutableFavoriteIds(session));
	}

	private Set<Long> getMutableFavoriteIds(HttpSession session) {
		Object value = session.getAttribute(FAVORITE_TASK_IDS);
		if (value instanceof Set<?> set) {
			Set<Long> favoriteIds = new LinkedHashSet<>();
			for (Object item : set) {
				if (item instanceof Long taskId) {
					favoriteIds.add(taskId);
				}
			}
			session.setAttribute(FAVORITE_TASK_IDS, favoriteIds);
			return favoriteIds;
		}

		Set<Long> favoriteIds = new LinkedHashSet<>();
		session.setAttribute(FAVORITE_TASK_IDS, favoriteIds);
		return favoriteIds;
	}
}
