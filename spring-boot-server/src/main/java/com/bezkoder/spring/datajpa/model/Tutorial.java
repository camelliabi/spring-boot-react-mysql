package com.bezkoder.spring.datajpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tutorials")
public class Tutorial {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	// FIX #15: Added validation annotations for better data integrity
	@Column(name = "title")
	@NotBlank(message = "Title is required")
	@Size(max = 255, message = "Title must be less than 255 characters")
	private String title;

	@Column(name = "description")
	@NotBlank(message = "Description is required")
	@Size(max = 1000, message = "Description must be less than 1000 characters")
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
