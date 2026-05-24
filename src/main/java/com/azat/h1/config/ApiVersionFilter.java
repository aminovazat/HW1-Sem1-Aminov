package com.azat.h1.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds the API version header to every HTTP response.
 */
@Component
public class ApiVersionFilter extends OncePerRequestFilter {

	private final String apiVersion;

	public ApiVersionFilter(@Value("${api.version}") String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		response.setHeader("X-API-Version", apiVersion);
		filterChain.doFilter(request, response);
	}
}
