package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Find tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #1: Find tutorials by title containing a substring (uses Spring Data JPA method naming convention)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1: Custom query to find tutorials by title AND published status
	// Changed method name from duplicate 'findByTitleContaining' to 'findByTitleAndPublished'
	// Fixed query to use LIKE operator for partial matching instead of exact equals
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published")
	List<Tutorial> findByTitleAndPublished(@Param("title") String title, @Param("published") boolean published);
}
