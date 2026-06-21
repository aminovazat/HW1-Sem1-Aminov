package com.azat.h1.config;

import com.azat.h1.dto.ErrorResponse;
import com.azat.h1.security.JwtAuthFilter;
import com.azat.h1.security.PasswordService;
import com.azat.h1.security.RestAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.Map;

/**
 * Configures stateless JWT security for the internal API.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
			RestAuthenticationEntryPoint authenticationEntryPoint, ObjectMapper objectMapper) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							ErrorResponse body = new ErrorResponse(
									Instant.now(),
									HttpStatus.FORBIDDEN.value(),
									HttpStatus.FORBIDDEN.getReasonPhrase(),
									"Access denied",
									request.getRequestURI(),
									Map.of()
							);
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							objectMapper.writeValue(response.getOutputStream(), body);
						}))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/auth/login").permitAll()
						.requestMatchers("/external/**").permitAll()
						.requestMatchers("/actuator/health", "/actuator/metrics", "/actuator/metrics/**",
								"/actuator/circuitbreakers/**", "/actuator/ratelimiters/**").permitAll()
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/api/tasks/**", "/api/attachments/**",
								"/api/favorites/**", "/api/preferences/**").permitAll()
						.requestMatchers("/api/v1/profile").hasRole("USER")
						.requestMatchers("/api/v1/docs").hasAuthority("READ_PRIVILEGE")
						.requestMatchers("/api/v1/tasks", "/api/v1/tasks/**").hasRole("USER")
						.anyRequest().permitAll())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder(@Value("${security.password-pepper:${PASSWORD_PEPPER:local-pepper}}")
			String pepper) {
		return new PasswordService(pepper);
	}

	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
		return new InMemoryUserDetailsManager(
				User.withUsername("user")
						.password(passwordEncoder.encode("password"))
						.authorities("ROLE_USER")
						.build(),
				User.withUsername("reader")
						.password(passwordEncoder.encode("password"))
						.authorities("ROLE_USER", "READ_PRIVILEGE")
						.build()
		);
	}

	@Bean
	public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter jwtAuthFilter) {
		FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(jwtAuthFilter);
		registration.setEnabled(false);
		return registration;
	}
}
