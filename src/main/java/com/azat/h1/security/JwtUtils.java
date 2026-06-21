package com.azat.h1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Generates and validates JWT access tokens.
 */
@Component
public class JwtUtils {

	private final SecretKey secretKey;
	private final long expirationMinutes;

	public JwtUtils(@Value("${jwt.secret:${JWT_SECRET:local-dev-secret-local-dev-secret-local-dev-secret}}") String secret,
			@Value("${jwt.expiration-minutes:60}") long expirationMinutes) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMinutes = expirationMinutes;
	}

	public String generateToken(UserDetails userDetails) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusSeconds(expirationMinutes * 60);
		List<String> authorities = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();
		List<String> roles = authorities.stream()
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring("ROLE_".length()))
				.toList();

		return Jwts.builder()
				.subject(userDetails.getUsername())
				.claim("roles", roles)
				.claim("authorities", authorities)
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiresAt))
				.signWith(secretKey)
				.compact();
	}

	public Claims parseClaims(String token) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (JwtException | IllegalArgumentException ex) {
			throw new InvalidJwtException("JWT token is invalid or expired", ex);
		}
	}

	public String getUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public List<String> getAuthorities(String token) {
		return getStringListClaim(parseClaims(token), "authorities");
	}

	public List<String> getRoles(String token) {
		return getStringListClaim(parseClaims(token), "roles");
	}

	public long getExpirationMinutes() {
		return expirationMinutes;
	}

	private List<String> getStringListClaim(Claims claims, String claimName) {
		Object value = claims.get(claimName);
		if (!(value instanceof List<?> values)) {
			return List.of();
		}
		return values.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.toList();
	}
}
