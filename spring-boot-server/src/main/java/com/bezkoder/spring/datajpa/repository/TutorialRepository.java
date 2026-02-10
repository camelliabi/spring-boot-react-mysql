package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX ERROR_001, ERROR_002, ERROR_003: Removed duplicate method and fixed query
	// Original had duplicate method name 'findByTitleContaining' causing compilation error
	// Query was using exact match (=) instead of LIKE for partial matching
	// Removed unused 'published' parameter that wasn't in the query
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
	
	// New method with corrected query using LIKE for partial match and proper parameter usage
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published")
	List<Tutorial> findByTitleAndPublished(@Param("title") String title, @Param("published") boolean published);
}
