package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Removed duplicate method signature
	// The original code had two methods with the same name 'findByTitleContaining'
	// but different parameters. One was using Spring Data JPA naming convention,
	// the other had a custom @Query. This caused method signature conflicts.
	// Keeping only the simple Spring Data JPA method for title search.
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
}
