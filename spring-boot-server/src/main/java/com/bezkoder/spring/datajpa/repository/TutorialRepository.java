package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX ERROR-001: Find tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX ERROR-001: Find tutorials by title containing search string (case-insensitive partial match)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX ERROR-001: Custom query to find tutorials by title containing search string AND published status
	// Changed from exact match (=) to LIKE for partial matching, and renamed method to avoid signature conflict
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published")
	List<Tutorial> findByTitleContainingAndPublished(@Param("title") String title, @Param("published") boolean published);
}
