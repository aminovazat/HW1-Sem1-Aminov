package com.azat.h1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing for created and updated timestamps.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
