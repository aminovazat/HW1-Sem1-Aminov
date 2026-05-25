package com.azat.h1.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes protected endpoints used to verify JWT authorization.
 */
@RestController
@RequestMapping("/api/v1")
public class ProtectedApiController {

	@GetMapping("/profile")
	public ResponseEntity<Map<String, String>> profile(Authentication authentication) {
		return ResponseEntity.ok(Map.of(
				"username", authentication.getName(),
				"message", "Profile is available"
		));
	}

	@GetMapping("/docs")
	public ResponseEntity<Map<String, String>> docs() {
		return ResponseEntity.ok(Map.of("message", "Docs are available"));
	}
}
