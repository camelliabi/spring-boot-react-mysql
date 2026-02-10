package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Removed duplicate method 'findByTitleContaining' with @Query annotation
	// ISSUE: Two methods with same name but different signatures caused method signature conflict
	// The custom @Query used exact match (=) instead of LIKE for "Containing" semantics
	// SOLUTION: Keep only the standard Spring Data JPA method which auto-generates proper LIKE query
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
}
