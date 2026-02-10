package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Find tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Find tutorials by title containing the search string (case-insensitive partial match)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1: Removed duplicate method with incorrect JPQL query
	// The original method had:
	// - Same name as findByTitleContaining(String) causing method signature conflict
	// - JPQL query using exact match (=) instead of LIKE for partial matching
	// - Unused 'published' parameter
	// Spring Data JPA's naming convention already provides the correct implementation
}
