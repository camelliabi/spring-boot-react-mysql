package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX ERR-001: Removed duplicate findByTitleContaining method with incompatible signature
	// Spring Data JPA automatically implements this method with LIKE '%title%' behavior
	List<Tutorial> findByTitleContaining(String title);
	
	// Query method to find tutorials by published status
	List<Tutorial> findByPublished(boolean published);
}
