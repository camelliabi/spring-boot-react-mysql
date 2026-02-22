package com.bezkoder.spring.datajpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tutorials")
public class Tutorial {

	@Id
	// FIX #14: Changed from GenerationType.AUTO to IDENTITY for predictable ID generation
	// AUTO lets JPA choose strategy which varies by database and can cause inconsistent behavior
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	// FIX #15: Added validation constraints to ensure data integrity
	// @NotBlank ensures title is not null and contains at least one non-whitespace character
	@NotBlank(message = "Title is required")
	@Size(max = 255, message = "Title must not exceed 255 characters")
	@Column(name = "title")
	private String title;

	// FIX #15: Added validation constraints for description field
	@Size(max = 1000, message = "Description must not exceed 1000 characters")
	@Column(name = "description")
	private String description;

	@Column(name = "published")
	private boolean published;

	public Tutorial() {

	}

	public Tutorial(String title, String description, boolean published) {
		this.title = title;
		this.description = description;
		this.published = published;
	}

	public long getId() {
		return id;
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

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean isPublished) {
		this.published = isPublished;
	}

	@Override
	public String toString() {
		return "Tutorial [id=" + id + ", title=" + title + ", desc=" + description + ", published=" + published + "]";
	}

}
