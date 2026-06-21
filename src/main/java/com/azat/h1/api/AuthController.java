package com.azat.h1.api;

import com.azat.h1.dto.security.LoginRequest;
import com.azat.h1.dto.security.LoginResponse;
import com.azat.h1.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Issues JWT access tokens for the protected internal API.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final JwtUtils jwtUtils;

	public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
			JwtUtils jwtUtils) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.jwtUtils = jwtUtils;
	}

	@Operation(summary = "Login and receive a JWT")
	@ApiResponse(responseCode = "200", description = "Token issued")
	@ApiResponse(responseCode = "401", description = "Invalid credentials")
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
		);
		String token = jwtUtils.generateToken(userDetailsService.loadUserByUsername(request.getUsername()));
		return ResponseEntity.ok(new LoginResponse(token, "Bearer", jwtUtils.getExpirationMinutes()));
	}
}
