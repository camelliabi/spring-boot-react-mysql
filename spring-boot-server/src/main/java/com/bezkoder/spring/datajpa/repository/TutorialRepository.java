package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #3: Added method for finding tutorials by published status
	// Spring Data JPA will auto-implement this based on the method name
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #3: Added method for finding tutorials by title containing search term (partial match)
	// Spring Data JPA will auto-generate the query with LIKE operator
	List<Tutorial> findByTitleContaining(String title);
}
