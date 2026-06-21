package com.azat.h1.security;

import com.azat.h1.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Authenticates requests that contain a valid Bearer JWT.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtUtils jwtUtils;
	private final ObjectMapper objectMapper;

	public JwtAuthFilter(JwtUtils jwtUtils, ObjectMapper objectMapper) {
		this.jwtUtils = jwtUtils;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || authorization.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		if (!authorization.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorization.substring(BEARER_PREFIX.length());
		try {
			String username = jwtUtils.getUsername(token);
			var authorities = jwtUtils.getAuthorities(token).stream()
					.map(SimpleGrantedAuthority::new)
					.toList();
			var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch (InvalidJwtException ex) {
			logger.warn("Rejected invalid JWT {}", maskToken(token));
			SecurityContextHolder.clearContext();
			writeUnauthorized(request, response);
		}
	}

	public String maskToken(String token) {
		if (token == null || token.length() < 12) {
			return "***";
		}
		return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
	}

	private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				HttpStatus.UNAUTHORIZED.value(),
				HttpStatus.UNAUTHORIZED.getReasonPhrase(),
				"Invalid or expired token",
				request.getRequestURI(),
				Map.of()
		);
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), body);
	}
}
