package com.azat.h1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response body with attachment metadata.
 */
@Schema(description = "Attachment response")
public class AttachmentResponseDto {

	private Long id;
	private String fileName;
	private long size;
	private LocalDateTime uploadedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}
}
