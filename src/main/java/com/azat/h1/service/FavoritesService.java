package com.azat.h1.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stores favorite task identifiers in the HTTP session.
 */
@Service
public class FavoritesService {

	private static final String FAVORITE_TASK_IDS = "favoriteTaskIds";

	public void addFavorite(Long taskId, HttpSession session) {
		Set<Long> favoriteIds = getMutableFavoriteIds(session);
		favoriteIds.add(taskId);
		session.setAttribute(FAVORITE_TASK_IDS, favoriteIds);
	}

	public void removeFavorite(Long taskId, HttpSession session) {
		Set<Long> favoriteIds = getMutableFavoriteIds(session);
		favoriteIds.remove(taskId);
		session.setAttribute(FAVORITE_TASK_IDS, favoriteIds);
	}

	public Set<Long> getFavoriteIds(HttpSession session) {
		return getMutableFavoriteIds(session);
	}

	@SuppressWarnings("unchecked")
	private Set<Long> getMutableFavoriteIds(HttpSession session) {
		Object attribute = session.getAttribute(FAVORITE_TASK_IDS);
		if (attribute instanceof Set<?>) {
			return new LinkedHashSet<>((Set<Long>) attribute);
		}
		return new LinkedHashSet<>();
	}
}
