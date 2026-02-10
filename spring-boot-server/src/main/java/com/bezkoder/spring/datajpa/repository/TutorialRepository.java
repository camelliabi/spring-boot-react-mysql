package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX ERROR-001: Removed duplicate method definition with incorrect @Query annotation
	// The second findByTitleContaining method had wrong query logic (exact match instead of LIKE)
	// and conflicting parameter signature. Spring Data JPA auto-generates the correct implementation.
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
}
