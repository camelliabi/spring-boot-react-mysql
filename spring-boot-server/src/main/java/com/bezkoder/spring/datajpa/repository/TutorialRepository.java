package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Removed duplicate method declaration
	// The original code had two methods with the same name but different signatures
	// This caused a method overload conflict where the @Query didn't match the method semantics
	// Keeping only the simple method that uses Spring Data JPA naming convention
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
	
	// FIX #1 (continued): Renamed the custom query method to avoid conflict
	// Changed from 'findByTitleContaining' to 'findByTitleEquals' to match the actual query behavior
	// The @Query uses exact match (=) not LIKE/CONTAINING, so the method name should reflect this
	@Query("SELECT t FROM Tutorial t WHERE t.title = :title")
	List<Tutorial> findByTitleEquals(@Param("title") String title);
}
