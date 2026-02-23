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
	
	// FIX: Fixed JPQL syntax error - changed from %:title% to proper CONCAT function
	// JPQL requires CONCAT to build LIKE patterns, cannot use wildcards directly in parameters
	// This prevents QuerySyntaxException: unexpected char: '%' errors at runtime
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE CONCAT('%', :title, '%') AND t.published = :published")
	List<Tutorial> findByTitleContainingAndPublished(@Param("title") String title, @Param("published") boolean published);
}
