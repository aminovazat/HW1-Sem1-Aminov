package com.azat.h1.service;

import com.azat.h1.model.Priority;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

/**
 * Reads task statistics with plain JDBC queries.
 */
@Service
public class TaskStatisticsJdbcService {

	private final JdbcTemplate jdbcTemplate;
	private final RowMapper<PriorityCount> priorityCountRowMapper = (resultSet, rowNum) -> new PriorityCount(
			Priority.valueOf(resultSet.getString("priority")),
			resultSet.getLong("task_count")
	);

	public TaskStatisticsJdbcService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Map<Priority, Long> getTasksCountByPriority() {
		Map<Priority, Long> counts = new EnumMap<>(Priority.class);
		jdbcTemplate.query("""
				select priority, count(*) as task_count
				from tasks
				group by priority
				""", priorityCountRowMapper).forEach(priorityCount ->
				counts.put(priorityCount.priority(), priorityCount.count()));
		return counts;
	}

	private record PriorityCount(Priority priority, long count) {
	}
}
