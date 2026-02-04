package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// Find all tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #7: Original method - finds tutorials where title contains the search string
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #8, #9, #10: Fixed duplicate method issue
	// - Renamed from findByTitleContaining to findByTitleAndPublished to reflect actual behavior
	// - Fixed query to use LIKE instead of exact match to properly search within title
	// - This method finds tutorials where title contains search string AND matches published status
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published")
	List<Tutorial> findByTitleAndPublished(@Param("title") String title, @Param("published") boolean published);
}
