package com.azat.h1.api;

import com.azat.h1.dto.LoginRequest;
import com.azat.h1.dto.LoginResponse;
import com.azat.h1.security.JwtUtils;
import com.azat.h1.security.PasswordService;
import com.azat.h1.security.RestAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Authenticates users and returns JWT access tokens.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final UserDetailsService userDetailsService;
	private final PasswordService passwordService;
	private final JwtUtils jwtUtils;
	private final RestAuthenticationEntryPoint authenticationEntryPoint;

	public AuthController(UserDetailsService userDetailsService, PasswordService passwordService,
			JwtUtils jwtUtils, RestAuthenticationEntryPoint authenticationEntryPoint) {
		this.userDetailsService = userDetailsService;
		this.passwordService = passwordService;
		this.jwtUtils = jwtUtils;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
		if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
			return unauthorized(request);
		}

		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
			if (!passwordService.matches(loginRequest.getPassword(), userDetails.getPassword())) {
				return unauthorized(request);
			}

			return ResponseEntity.ok(new LoginResponse(
					jwtUtils.generateToken(userDetails),
					"Bearer",
					jwtUtils.getExpirationMinutes()
			));
		} catch (UsernameNotFoundException ex) {
			return unauthorized(request);
		}
	}

	private ResponseEntity<Map<String, Object>> unauthorized(HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(authenticationEntryPoint.body(request, "Invalid username or password"));
	}
}
