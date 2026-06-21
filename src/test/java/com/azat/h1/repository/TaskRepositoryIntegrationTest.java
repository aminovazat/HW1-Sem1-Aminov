package com.azat.h1.repository;

import com.azat.h1.config.JpaConfig;
import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class TaskRepositoryIntegrationTest {

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("todo_test")
			.withUsername("todo_test")
			.withPassword("todo_test");

	@Autowired
	private TaskRepository taskRepository;

	@DynamicPropertySource
	static void registerPostgresProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
		registry.add("spring.flyway.enabled", () -> "true");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
		registry.add("spring.jpa.open-in-view", () -> "false");
	}

	@Test
	void findDueBetweenReturnsOnlyTasksInsideRequestedRange() {
		LocalDate today = LocalDate.now();
		Task dueSoon = task("Due soon", today.plusDays(2));
		Task dueLater = task("Due later", today.plusDays(20));
		taskRepository.saveAll(List.of(dueSoon, dueLater));

		List<Task> tasks = taskRepository.findDueBetween(today, today.plusDays(7));

		assertThat(tasks).extracting(Task::getTitle)
				.contains("Due soon")
				.doesNotContain("Due later");
	}

	private Task task(String title, LocalDate dueDate) {
		return new Task(null, title, "PostgreSQL Testcontainers test", false, null, dueDate,
				Priority.MEDIUM, Set.of("testcontainers"));
	}
}
