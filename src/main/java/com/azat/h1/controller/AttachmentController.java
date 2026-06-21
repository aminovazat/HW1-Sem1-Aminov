package com.azat.h1.controller;

import com.azat.h1.dto.AttachmentResponseDto;
import com.azat.h1.model.TaskAttachment;
import com.azat.h1.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Exposes REST endpoints for task attachments.
 */
@RestController
@RequestMapping
public class AttachmentController {

	private final AttachmentService attachmentService;

	public AttachmentController(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	@Operation(summary = "Upload a task attachment")
	@ApiResponse(responseCode = "201", description = "Attachment uploaded")
	@ApiResponse(responseCode = "400", description = "File is empty")
	@ApiResponse(responseCode = "404", description = "Task not found")
	@PostMapping(value = "/api/tasks/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<AttachmentResponseDto> uploadAttachment(@PathVariable Long taskId,
			@RequestPart("file") MultipartFile file) {
		return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.storeAttachment(taskId, file));
	}

	@Operation(summary = "Download an attachment")
	@ApiResponse(responseCode = "200", description = "Attachment downloaded")
	@ApiResponse(responseCode = "404", description = "Attachment not found")
	@GetMapping("/api/attachments/{attachmentId}")
	public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
		TaskAttachment attachment = attachmentService.getAttachment(attachmentId);
		Resource resource = attachmentService.loadAsResource(attachmentId);
		MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
		if (attachment.getContentType() != null) {
			contentType = MediaType.parseMediaType(attachment.getContentType());
		}

		return ResponseEntity.ok()
				.contentType(contentType)
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(attachment.getFileName())
						.build()
						.toString())
				.body(resource);
	}

	@Operation(summary = "Delete an attachment")
	@ApiResponse(responseCode = "204", description = "Attachment deleted")
	@ApiResponse(responseCode = "404", description = "Attachment not found")
	@DeleteMapping("/api/attachments/{attachmentId}")
	public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
		attachmentService.deleteAttachment(attachmentId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "List task attachments")
	@ApiResponse(responseCode = "200", description = "Attachments returned")
	@ApiResponse(responseCode = "404", description = "Task not found")
	@GetMapping("/api/tasks/{taskId}/attachments")
	public ResponseEntity<List<AttachmentResponseDto>> getTaskAttachments(@PathVariable Long taskId) {
		return ResponseEntity.ok(attachmentService.getTaskAttachments(taskId));
	}
}
