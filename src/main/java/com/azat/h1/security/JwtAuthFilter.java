package com.azat.h1.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads Bearer tokens and fills the Spring Security context.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtUtils jwtUtils;
	private final RestAuthenticationEntryPoint authenticationEntryPoint;

	public JwtAuthFilter(JwtUtils jwtUtils, RestAuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtUtils = jwtUtils;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
			rejectRequest(request, response);
			return;
		}

		String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
		if (token.isEmpty() || !jwtUtils.isTokenValid(token)) {
			rejectRequest(request, response);
			return;
		}

		Authentication authentication = new UsernamePasswordAuthenticationToken(
				jwtUtils.getUsername(token),
				null,
				jwtUtils.getAuthorities(token)
		);
		((UsernamePasswordAuthenticationToken) authentication)
				.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}

	private void rejectRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		SecurityContextHolder.clearContext();
		authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid JWT token"));
	}

	public static String maskToken(String token) {
		if (token == null || token.length() < 12) {
			return "***";
		}
		return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
	}
}
