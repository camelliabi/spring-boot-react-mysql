package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
	
	// BUG #18: Custom query with incorrect JPQL - using wrong operator
	// This will cause runtime errors when executed
	@Query("SELECT t FROM Tutorial t WHERE t.title = :title")
	List<Tutorial> findByTitleContaining(@Param("title") String title, @Param("published") boolean published);
}
