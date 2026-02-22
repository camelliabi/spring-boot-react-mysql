package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Method for finding tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Method for finding tutorials by title containing search term (partial match)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1: Custom query method for finding tutorials by title AND published status
	// Changed from exact match (=) to LIKE with CONCAT for proper partial text matching
	// JPQL requires CONCAT function to build wildcard patterns for LIKE operator
	// Added @Param annotations to properly bind method parameters to query placeholders
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE CONCAT('%', :title, '%') AND t.published = :published")
	List<Tutorial> findByTitleContainingAndPublished(@Param("title") String title, @Param("published") boolean published);
}
