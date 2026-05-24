package com.azat.h1.controller;

import com.azat.h1.dto.AttachmentResponseDto;
import com.azat.h1.model.TaskAttachment;
import com.azat.h1.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Exposes REST endpoints for task attachments.
 */
@RestController
public class AttachmentController {

	private final AttachmentService attachmentService;

	public AttachmentController(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	@Operation(summary = "Upload task attachment")
	@ApiResponse(responseCode = "200", description = "Attachment uploaded")
	@PostMapping(value = "/api/tasks/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<AttachmentResponseDto> uploadAttachment(
			@PathVariable Long taskId,
			@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(attachmentService.storeAttachment(taskId, file));
	}

	@Operation(summary = "Download attachment")
	@GetMapping("/api/attachments/{attachmentId}")
	public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
		TaskAttachment attachment = attachmentService.getAttachment(attachmentId);
		Resource resource = attachmentService.loadAsResource(attachmentId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(attachment.getContentType() == null
						? MediaType.APPLICATION_OCTET_STREAM_VALUE : attachment.getContentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(attachment.getFileName()).build().toString())
				.body(resource);
	}

	@Operation(summary = "Delete attachment")
	@DeleteMapping("/api/attachments/{attachmentId}")
	public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
		attachmentService.deleteAttachment(attachmentId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Get task attachments")
	@GetMapping("/api/tasks/{taskId}/attachments")
	public ResponseEntity<List<AttachmentResponseDto>> getTaskAttachments(@PathVariable Long taskId) {
		return ResponseEntity.ok(attachmentService.getTaskAttachments(taskId));
	}
}
