package com.azat.h1.service;

import com.azat.h1.dto.PriorityTaskCountDto;
import com.azat.h1.model.Priority;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reads task statistics through JdbcTemplate.
 */
@Service
public class TaskStatisticsJdbcService {

	private final JdbcTemplate jdbcTemplate;
	private final RowMapper<PriorityTaskCountDto> priorityCountRowMapper = (rs, rowNum) ->
			new PriorityTaskCountDto(Priority.valueOf(rs.getString("priority")), rs.getLong("task_count"));

	public TaskStatisticsJdbcService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<PriorityTaskCountDto> getTasksCountByPriority() {
		return jdbcTemplate.query(
				"""
						select priority, count(*) as task_count
						from tasks
						group by priority
						order by priority
						""",
				priorityCountRowMapper
		);
	}
}
