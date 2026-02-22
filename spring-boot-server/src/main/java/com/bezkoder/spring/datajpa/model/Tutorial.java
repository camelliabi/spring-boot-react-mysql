package com.bezkoder.spring.datajpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Tutorial entity class
 * FIX #5: Added validation annotations to enforce business rules
 */
@Entity
@Table(name = "tutorials")
public class Tutorial {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	// FIX #5: Added @NotBlank to prevent null or empty titles
	// FIX #5: Added @Size to enforce length constraints (3-100 characters)
	@NotBlank(message = "Title is required")
	@Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
	@Column(name = "title")
	private String title;

	// FIX #5: Added @NotBlank to prevent null or empty descriptions
	// FIX #5: Added @Size to enforce length constraints (10-500 characters)
	@NotBlank(message = "Description is required")
	@Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
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
