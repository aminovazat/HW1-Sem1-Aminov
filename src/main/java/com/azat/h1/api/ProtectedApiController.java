package com.azat.h1.api;

import com.azat.h1.dto.security.MessageResponse;
import com.azat.h1.dto.security.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates protected internal API endpoints.
 */
@RestController
@RequestMapping("/api/v1")
public class ProtectedApiController {

	@Operation(summary = "Get authenticated profile")
	@ApiResponse(responseCode = "200", description = "Profile returned")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@GetMapping("/profile")
	public ResponseEntity<ProfileResponse> profile(Authentication authentication) {
		return ResponseEntity.ok(new ProfileResponse(
				authentication.getName(),
				authentication.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.toList(),
				"Authenticated profile"
		));
	}

	@Operation(summary = "Get protected docs message")
	@ApiResponse(responseCode = "200", description = "Docs returned")
	@ApiResponse(responseCode = "403", description = "Read privilege required")
	@GetMapping("/docs")
	public ResponseEntity<MessageResponse> docs() {
		return ResponseEntity.ok(new MessageResponse("Protected documentation"));
	}
}
