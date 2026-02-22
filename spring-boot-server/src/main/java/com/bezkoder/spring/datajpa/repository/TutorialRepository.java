package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	
	// Built-in Spring Data JPA method - no custom query needed
	List<Tutorial> findByPublished(boolean published);
	
	// Built-in Spring Data JPA method - no custom query needed
	List<Tutorial> findByTitleContaining(String title);
	
	// BUG #5 FIX: Fixed JPQL syntax error for custom query
	// Previous error: @Query("SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published")
	// Issue: Cannot use % directly with :parameter in JPQL
	// Solution: Use CONCAT function to properly construct LIKE pattern
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE CONCAT('%', :title, '%') AND t.published = :published")
	List<Tutorial> findByTitleContainingAndPublished(@Param("title") String title, @Param("published") boolean published);
	
	// Alternative solution without @Query (recommended - simpler and less error-prone):
	// Spring Data JPA can auto-generate this query from the method name
	// Uncomment the line below and remove the @Query method above if you prefer:
	// List<Tutorial> findByTitleContainingAndPublished(String title, boolean published);
}
