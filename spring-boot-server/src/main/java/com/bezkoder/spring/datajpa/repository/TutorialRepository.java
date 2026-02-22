package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// Find tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #3: CRITICAL BUG - Fixed method overloading conflict and query logic error
	// Original issue: Had duplicate method name 'findByTitleContaining' with different signatures
	// First method (line 13) took only String title - this one is kept for basic search
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #3 (continued): Renamed the second method to avoid overloading ambiguity
	// Changed query from "t.title = :title" (exact match) to "t.title LIKE CONCAT('%', :title, '%')"
	// This enables proper substring/partial matching as the method name implies
	// Also added published filter as the method signature suggests
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE CONCAT('%', :title, '%') AND t.published = :published")
	List<Tutorial> findByTitleContainingAndPublished(@Param("title") String title, @Param("published") boolean published);
}
