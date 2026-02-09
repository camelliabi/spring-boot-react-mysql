package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1: Fixed query/parameter mismatch in overloaded findByTitleContaining method
	// The @Query annotation now includes both title and published parameters to match the method signature
	// Previous query only checked title: "SELECT t FROM Tutorial t WHERE t.title = :title"
	// This caused a mismatch because the method declared @Param("published") but the query didn't use it
	// Now the query correctly filters by both title AND published status
	@Query("SELECT t FROM Tutorial t WHERE t.title LIKE %:title% AND t.published = :published")
	List<Tutorial> findByTitleContaining(@Param("title") String title, @Param("published") boolean published);
}
