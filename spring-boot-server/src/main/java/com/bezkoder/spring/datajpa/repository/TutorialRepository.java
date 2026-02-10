package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// Find tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// Find tutorials by title containing the search string (case-insensitive partial match)
	List<Tutorial> findByTitleContaining(String title);
	
	// REMOVED: Duplicate method with @Query annotation that had incorrect implementation
	// The method signature conflicted with the auto-generated findByTitleContaining method above
	// and the @Query was using exact match (=) instead of LIKE for partial matching
}
