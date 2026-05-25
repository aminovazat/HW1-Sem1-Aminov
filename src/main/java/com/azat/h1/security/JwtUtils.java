package com.azat.h1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Generates and validates JWT access tokens.
 */
@Component
public class JwtUtils {

	private final SecretKey secretKey;
	private final long expirationMinutes;

	public JwtUtils(
			@Value("${security.jwt.secret}") String secret,
			@Value("${security.jwt.expiration-minutes}") long expirationMinutes) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMinutes = expirationMinutes;
	}

	public String generateToken(UserDetails userDetails) {
		List<String> authorities = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();
		List<String> roles = authorities.stream()
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring("ROLE_".length()))
				.toList();
		Instant now = Instant.now();

		return Jwts.builder()
				.subject(userDetails.getUsername())
				.claim("username", userDetails.getUsername())
				.claim("roles", roles)
				.claim("authorities", authorities)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
				.signWith(secretKey)
				.compact();
	}

	public boolean isTokenValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	public String getUsername(String token) {
		return parseClaims(token).get("username", String.class);
	}

	public List<String> getRoles(String token) {
		return getStringListClaim(token, "roles");
	}

	public List<String> getAuthorityNames(String token) {
		return getStringListClaim(token, "authorities");
	}

	public List<GrantedAuthority> getAuthorities(String token) {
		return getAuthorityNames(token).stream()
				.map(SimpleGrantedAuthority::new)
				.map(GrantedAuthority.class::cast)
				.toList();
	}

	public long getExpirationMinutes() {
		return expirationMinutes;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private List<String> getStringListClaim(String token, String claimName) {
		Object value = parseClaims(token).get(claimName);
		if (!(value instanceof List<?> values)) {
			return List.of();
		}
		return values.stream()
				.map(String::valueOf)
				.toList();
	}
}
