package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Method for finding tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Method for finding tutorials by title containing search term (partial match)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #3: Removed custom @Query with invalid JPQL syntax
	// Spring Data JPA automatically implements this method using method naming convention
	// This is safer and cleaner than using CONCAT in JPQL
	// The method name tells Spring Data to:
	// - findBy: Start a query
	// - TitleContaining: WHERE title LIKE %?%
	// - And: Combine conditions with AND
	// - Published: WHERE published = ?
	List<Tutorial> findByTitleContainingAndPublished(String title, boolean published);
}
