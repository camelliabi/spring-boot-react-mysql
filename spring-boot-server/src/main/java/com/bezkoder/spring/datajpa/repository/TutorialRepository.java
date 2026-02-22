package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Method to find tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Method to find tutorials by title containing (partial match)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1: Renamed method from duplicate 'findByTitleContaining' to 'findByTitleContainingAndPublished'
	// FIX #1: Changed query from exact match (=) to partial match (LIKE %:title%)
	// FIX #1: Added published parameter to WHERE clause to filter by published status
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published")
	List<Tutorial> findByTitleContainingAndPublished(@Param("title") String title, @Param("published") boolean published);
}
