package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Find tutorials by published status
	// This method returns all tutorials that match the published status (true for published, false for unpublished)
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Find tutorials by title containing search string (case-insensitive partial match)
	// Spring Data JPA automatically implements this method based on naming convention
	// No need for @Query annotation - the method name defines the query
	List<Tutorial> findByTitleContaining(String title);
}
