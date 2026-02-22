package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Removed conflicting method overload that had:
	// - Incorrect @Query using exact match (=) instead of LIKE for title search
	// - Unused @Param("published") parameter not referenced in query
	// - Method signature conflict with Spring Data JPA derived query method
	// The single-parameter findByTitleContaining method below handles partial title matching correctly
	
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
}
