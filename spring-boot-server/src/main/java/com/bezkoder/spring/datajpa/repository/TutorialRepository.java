package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Removed duplicate findByTitleContaining method with incorrect @Query annotation
	// The @Query was using "=" instead of "LIKE" for title matching, and had unused 'published' parameter
	// Spring Data JPA automatically generates the correct query from the method name
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
}
