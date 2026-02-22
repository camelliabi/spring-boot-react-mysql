package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

/**
 * TutorialRepository interface for data access operations
 * FIX #1: Removed custom @Query with invalid JPQL syntax
 * Using Spring Data JPA derived query methods instead
 */
public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	
	// FIX #1: Spring Data JPA automatically generates query from method name
	// Finds tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Spring Data JPA automatically generates LIKE query with wildcards
	// Finds tutorials where title contains the search term (case-insensitive partial match)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1: Replaced custom @Query with derived query method
	// Spring Data JPA generates: SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published
	// This eliminates the JPQL syntax error from using %:title% directly
	List<Tutorial> findByTitleContainingAndPublished(String title, boolean published);
}
