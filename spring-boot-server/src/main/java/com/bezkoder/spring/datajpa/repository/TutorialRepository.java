package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Find tutorials by published status
	// This method is required by TutorialController.findByPublished()
	// Spring Data JPA automatically implements this based on method name
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Find tutorials by title containing search string
	// This method is required by TutorialController.getAllTutorials()
	// Spring Data JPA auto-implements case-insensitive partial matching
	List<Tutorial> findByTitleContaining(String title);
}
