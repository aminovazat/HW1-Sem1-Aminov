package com.azat.h1.controller;

import io.swagger.v3.oas.annotations.Operation;
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
 * Exposes REST endpoints for user preferences stored in cookies.
 */
@RestController
@RequestMapping("/api/preferences")
public class PreferencesController {

	private static final Set<String> ALLOWED_MODES = Set.of("compact", "detailed");

	@Operation(summary = "Get view preference")
	@GetMapping("/view")
	public ResponseEntity<String> getViewPreference(
			@CookieValue(name = "viewPreference", defaultValue = "compact") String mode) {
		return ResponseEntity.ok(mode);
	}

	@Operation(summary = "Set view preference")
	@PostMapping("/view")
	public ResponseEntity<String> setViewPreference(@RequestParam String mode) {
		if (!ALLOWED_MODES.contains(mode)) {
			throw new IllegalArgumentException("mode must be compact or detailed");
		}
		ResponseCookie cookie = ResponseCookie.from("viewPreference", mode)
				.path("/")
				.maxAge(Duration.ofDays(30))
				.sameSite("Lax")
				.build();
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(mode);
	}
}
