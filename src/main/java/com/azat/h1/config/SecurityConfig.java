package com.azat.h1.config;

import com.azat.h1.security.JwtAuthFilter;
import com.azat.h1.security.PasswordService;
import com.azat.h1.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures stateless JWT security for API v1 endpoints.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
			RestAuthenticationEntryPoint authenticationEntryPoint) throws Exception {
		return http
				.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/auth/login").permitAll()
						.requestMatchers("/external/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
						.requestMatchers("/actuator/health", "/actuator/metrics",
								"/actuator/circuitbreakers", "/actuator/ratelimiters").permitAll()
						.requestMatchers("/api/tasks/**", "/api/attachments/**", "/api/favorites/**",
								"/api/preferences/**").permitAll()
						.requestMatchers("/api/v1/profile").hasRole("USER")
						.requestMatchers("/api/v1/docs").hasAuthority("READ_PRIVILEGE")
						.requestMatchers("/api/v1/tasks", "/api/v1/tasks/**").authenticated()
						.anyRequest().permitAll()
				)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public UserDetailsService userDetailsService(PasswordService passwordService) {
		return new InMemoryUserDetailsManager(
				User.withUsername("user")
						.password(passwordService.encode("password"))
						.roles("USER")
						.build(),
				User.withUsername("reader")
						.password(passwordService.encode("password"))
						.roles("USER")
						.authorities("ROLE_USER", "READ_PRIVILEGE")
						.build()
		);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
