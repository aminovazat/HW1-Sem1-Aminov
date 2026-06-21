package com.azat.h1.service;

import com.azat.h1.config.JpaConfig;
import com.azat.h1.dto.PriorityTaskCountDto;
import com.azat.h1.model.Priority;
import com.azat.h1.model.Task;
import com.azat.h1.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class, TaskStatisticsJdbcService.class})
class TaskStatisticsJdbcServiceTests {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TaskStatisticsJdbcService taskStatisticsJdbcService;

	@Test
	void getTasksCountByPriorityUsesGroupBy() {
		taskRepository.saveAll(List.of(
				task("Low task", Priority.LOW),
				task("High task one", Priority.HIGH),
				task("High task two", Priority.HIGH)
		));

		List<PriorityTaskCountDto> counts = taskStatisticsJdbcService.getTasksCountByPriority();

		assertThat(counts).anySatisfy(count -> {
			assertThat(count.getPriority()).isEqualTo(Priority.HIGH);
			assertThat(count.getCount()).isEqualTo(2L);
		});
		assertThat(counts).anySatisfy(count -> {
			assertThat(count.getPriority()).isEqualTo(Priority.LOW);
			assertThat(count.getCount()).isEqualTo(1L);
		});
	}

	private Task task(String title, Priority priority) {
		return new Task(null, title, "JDBC statistics test", false, null, LocalDate.now().plusDays(1),
				priority, Set.of("jdbc"));
	}
}
