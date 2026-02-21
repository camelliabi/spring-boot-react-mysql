package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX ERR-001: Method for finding tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// FIX ERR-001: Method for finding tutorials by title containing substring (Spring Data JPA auto-generates LIKE query)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX ERR-001: Renamed method to avoid signature conflict - this searches by exact title match and published status
	// Previous issue: Had same method name 'findByTitleContaining' but different parameters, causing ambiguity
	// The @Query annotation performs exact match (t.title = :title) not LIKE, so renamed to reflect actual behavior
	@Query("SELECT t FROM Tutorial t WHERE t.title = :title AND t.published = :published")
	List<Tutorial> findByTitleAndPublished(@Param("title") String title, @Param("published") boolean published);
}
