package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Removed duplicate method - Spring Data JPA generates this automatically
	// Original had two methods with same name but different signatures causing compilation error
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #2: Removed the @Query annotation and extra parameters
	// Spring Data JPA auto-generates LIKE query from method name 'findByTitleContaining'
	// Original had conflicting @Query with exact match (=) instead of LIKE and unused 'published' parameter
	List<Tutorial> findByTitleContaining(String title);
}
