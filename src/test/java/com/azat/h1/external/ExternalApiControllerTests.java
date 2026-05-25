package com.azat.h1.external;

import com.azat.h1.dto.gateway.ExternalTaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExternalApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void postCreatesTaskAndReturnsLocation() throws Exception {
		mockMvc.perform(post("/external/v1/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request("External create", false))))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/external/v1/tasks/")))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("External create"))
				.andExpect(jsonPath("$.completed").value(false));
	}

	@Test
	void getExistingTaskReturnsBody() throws Exception {
		Long id = createTask("External get", true);

		mockMvc.perform(get("/external/v1/tasks/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.title").value("External get"))
				.andExpect(jsonPath("$.completed").value(true));
	}

	@Test
	void getMissingTaskReturnsProblemDetail() throws Exception {
		mockMvc.perform(get("/external/v1/tasks/{id}", 999999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("External task not found: 999999"));
	}

	@Test
	void deleteExistingTaskReturnsNoContent() throws Exception {
		Long id = createTask("External delete", false);

		mockMvc.perform(delete("/external/v1/tasks/{id}", id))
				.andExpect(status().isNoContent());
	}

	@Test
	void unstableHtmlReturnsTextHtmlBadGateway() throws Exception {
		mockMvc.perform(get("/external/v1/unstable").param("mode", "html"))
				.andExpect(status().isBadGateway())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
				.andExpect(content().string(containsString("Bad Gateway")));
	}

	@Test
	void unstableTooManyRequestsReturnsRetryAfter() throws Exception {
		mockMvc.perform(get("/external/v1/unstable").param("mode", "429"))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().string("Retry-After", "3"))
				.andExpect(jsonPath("$.detail").value("Too many requests"));
	}

	private Long createTask(String title, boolean completed) throws Exception {
		String body = mockMvc.perform(post("/external/v1/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request(title, completed))))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return objectMapper.readTree(body).get("id").asLong();
	}

	private ExternalTaskRequest request(String title, boolean completed) {
		ExternalTaskRequest request = new ExternalTaskRequest();
		request.setTitle(title);
		request.setDescription("External API test task");
		request.setCompleted(completed);
		return request;
	}
}
