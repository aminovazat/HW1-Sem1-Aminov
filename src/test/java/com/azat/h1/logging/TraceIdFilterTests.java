package com.azat.h1.logging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TraceIdFilterTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void requestWithoutTraceIdGetsGeneratedTraceIdHeader() throws Exception {
		mockMvc.perform(get("/external/v1/tasks"))
				.andExpect(status().isOk())
				.andExpect(header().exists("X-Trace-Id"));
	}

	@Test
	void requestWithTraceIdGetsSameTraceIdHeader() throws Exception {
		mockMvc.perform(get("/external/v1/tasks").header("X-Trace-Id", "trace-123"))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Trace-Id", "trace-123"));
	}
}
