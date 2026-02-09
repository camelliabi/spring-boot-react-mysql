package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX #1: Removed duplicate findByTitleContaining method declaration
	// ISSUE: Two methods with the same name but different signatures caused method signature conflict
	// - First method: findByTitleContaining(String title) - auto-implemented by Spring Data JPA
	// - Second method (REMOVED): findByTitleContaining(String title, boolean published) with @Query
	// SOLUTION: Keep only the first method; Spring Data JPA auto-generates the correct LIKE query
	// IMPACT: Eliminates compilation errors and ambiguity
	List<Tutorial> findByPublished(boolean published);
	
	// FIX #2: Removed custom @Query that used exact match (=) instead of LIKE
	// ISSUE: Method name "findByTitleContaining" implies LIKE query (partial match)
	//        but @Query used "t.title = :title" (exact match only)
	// SOLUTION: Let Spring Data JPA auto-generate the query from method name
	//           Spring interprets "Containing" as SQL LIKE '%value%'
	// IMPACT: Search now works with partial matches as users expect
	//         e.g., searching "Spr" will find "Spring Tutorial"
	List<Tutorial> findByTitleContaining(String title);
}
