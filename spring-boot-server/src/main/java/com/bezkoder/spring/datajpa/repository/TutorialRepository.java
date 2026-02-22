package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// Method for finding tutorials by published status
	List<Tutorial> findByPublished(boolean published);
	
	// Method for finding tutorials by title containing search term (partial match)
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX ERR-014: Use proper JPQL CONCAT syntax for LIKE wildcards
	// Previous: WHERE t.title LIKE %:title% (invalid JPQL syntax)
	// Fixed: Use CONCAT function to properly build LIKE pattern with wildcards
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE CONCAT('%', :title, '%') AND t.published = :published")
	List<Tutorial> findByTitleContainingAndPublished(@Param("title") String title, @Param("published") boolean published);
}
