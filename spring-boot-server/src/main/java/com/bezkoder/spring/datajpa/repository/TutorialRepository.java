package com.bezkoder.spring.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.datajpa.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
	// FIX ERROR #1: Removed duplicate method signature.
	// Original had two 'findByTitleContaining' methods with different parameters,
	// causing compilation error. Kept the simple version that Spring Data JPA
	// automatically implements with LIKE query.
	List<Tutorial> findByPublished(boolean published);
	List<Tutorial> findByTitleContaining(String title);
}
