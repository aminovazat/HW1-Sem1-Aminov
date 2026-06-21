package com.azat.h1.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a persisted to-do task.
 */
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Internal task entity")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 500)
	private String description;

	@Column(nullable = false)
	private boolean completed;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Priority priority;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "task_tags", joinColumns = @JoinColumn(name = "task_id"))
	@Column(name = "tag", nullable = false, length = 100)
	private Set<String> tags = new LinkedHashSet<>();

	@OneToMany(mappedBy = "task", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<TaskAttachment> attachments = new ArrayList<>();

	public Task() {
	}

	public Task(Long id, String title, String description, boolean completed) {
		this(id, title, description, completed, null, null, null, null);
	}

	public Task(Long id, String title, String description, boolean completed, LocalDateTime createdAt,
			LocalDate dueDate, Priority priority, Set<String> tags) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.completed = completed;
		this.createdAt = createdAt;
		this.dueDate = dueDate;
		this.priority = priority;
		setTags(tags);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags);
	}

	public List<TaskAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<TaskAttachment> attachments) {
		this.attachments = attachments == null ? new ArrayList<>() : new ArrayList<>(attachments);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Task task)) {
			return false;
		}
		return id != null && Objects.equals(id, task.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "Task{"
				+ "id=" + id
				+ ", title='" + title + '\''
				+ ", description='" + description + '\''
				+ ", completed=" + completed
				+ ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt
				+ ", dueDate=" + dueDate
				+ ", priority=" + priority
				+ ", tags=" + tags
				+ '}';
	}
}
