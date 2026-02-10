package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Method for finding tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #2: Method for finding tutorials by title containing (partial match)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #3: New method for finding by both title and published status
	// Changed query to use LIKE for partial matching and added published parameter to query
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published")
	List<Tutorial> findByTitleContainingAndPublished(@Param("title") String title, @Param("published") boolean published);
}
