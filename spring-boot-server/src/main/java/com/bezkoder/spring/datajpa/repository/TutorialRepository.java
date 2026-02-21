package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Find tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Find tutorials by title containing search string (Spring Data JPA auto-implementation)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1: Custom query to find tutorials by exact title and published status
	// Renamed method to avoid duplicate method signature conflict
	@Query("SELECT t FROM Tutorial t WHERE t.title = :title AND t.published = :published")
	List<Tutorial> findByTitleAndPublished(@Param("title") String title, @Param("published") boolean published);
}
