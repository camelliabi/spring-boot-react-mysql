package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Fixed method signature mismatch
	// ORIGINAL BUG: Had duplicate method name 'findByTitleContaining' with different signatures
	// The @Query used exact match (=) but method name suggested partial match (Containing)
	// Also had unused @Param("published") parameter
	// FIXED: Renamed to clarify exact match and include published filter in method name
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
	
	@Query("SELECT t FROM Tutorial t WHERE t.title = :title AND t.published = :published")
	List<Tutorial> findByTitleExactAndPublished(@Param("title") String title, @Param("published") boolean published);
}
