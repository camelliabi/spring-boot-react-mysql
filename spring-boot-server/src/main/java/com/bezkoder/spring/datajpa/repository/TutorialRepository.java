package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// Find all tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// Find tutorials by title containing a substring (case-insensitive search)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1: Renamed method to avoid duplicate method name
	// FIX #1: Corrected @Query to properly filter by both title and published status
	// FIX #1: Changed from exact match (=) to LIKE for partial matching
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published")
	List<Tutorial> findByTitleContainingAndPublished(@Param("title") String title, @Param("published") boolean published);
}
