package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Removed duplicate method declaration
	// BEFORE: Had two methods named 'findByTitleContaining' with different signatures
	// - First: findByTitleContaining(String title) - Spring Data JPA auto-implementation
	// - Second: findByTitleContaining(String title, boolean published) with @Query annotation
	// ISSUE: Method signature conflict - same name, different parameters causes ambiguity
	// SOLUTION: Removed the duplicate @Query method. Spring Data JPA automatically generates
	//           the correct LIKE query from the method name 'findByTitleContaining'
	// IMPACT: Eliminates compilation errors and allows proper partial title matching
	
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
}
