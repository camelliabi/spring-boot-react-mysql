package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Method to find tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Method to find tutorials by title containing search string
	// Spring Data JPA automatically implements this method based on naming convention
	// It will generate a query like: WHERE title LIKE %:title%
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1: Removed duplicate method signature that was causing compilation error
	// The previous @Query annotation had incorrect logic (used = instead of LIKE)
	// and conflicted with the method above. Spring Data JPA's naming convention
	// is sufficient for this use case.
}
