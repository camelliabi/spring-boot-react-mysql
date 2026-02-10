package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Removed duplicate method signature and incorrect @Query annotation
	// The findByTitleContaining method is auto-implemented by Spring Data JPA
	// based on naming convention - no need for @Query or duplicate declaration
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
}
