package com.azat.h1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Set;

/**
 * Exposes cookie-based view preference endpoints.
 */
@RestController
@RequestMapping("/api/preferences")
public class PreferencesController {

	private static final String COOKIE_NAME = "viewPreference";
	private static final String DEFAULT_MODE = "detailed";
	private static final Set<String> ALLOWED_MODES = Set.of("compact", "detailed");

	@Operation(summary = "Get current view preference")
	@ApiResponse(responseCode = "200", description = "Preference returned")
	@GetMapping("/view")
	public ResponseEntity<String> getViewPreference(
			@CookieValue(name = COOKIE_NAME, required = false) String viewPreference) {
		String mode = isAllowed(viewPreference) ? viewPreference : DEFAULT_MODE;
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, createCookie(mode).toString())
				.body(mode);
	}

	@Operation(summary = "Set current view preference")
	@ApiResponse(responseCode = "200", description = "Preference updated")
	@ApiResponse(responseCode = "400", description = "Invalid mode")
	@PostMapping("/view")
	public ResponseEntity<String> setViewPreference(@RequestParam String mode) {
		if (!isAllowed(mode)) {
			throw new IllegalArgumentException("Invalid view preference mode: " + mode);
		}
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, createCookie(mode).toString())
				.body(mode);
	}

	private boolean isAllowed(String mode) {
		return mode != null && ALLOWED_MODES.contains(mode);
	}

	private ResponseCookie createCookie(String mode) {
		return ResponseCookie.from(COOKIE_NAME, mode)
				.path("/")
				.maxAge(Duration.ofDays(30))
				.httpOnly(false)
				.sameSite("Lax")
				.build();
	}
}
