package com.azat.h1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body for completing several tasks in one transaction.
 */
@Schema(description = "Bulk complete request")
public class BulkCompleteRequestDto {

	@NotEmpty
	@Schema(description = "Task ids to complete", example = "[1, 2, 3]")
	private List<Long> ids;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}
}
