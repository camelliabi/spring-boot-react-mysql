package com.bezkoder.spring.datajpa.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// FIX ERR-017: Add SLF4J logger for exception tracking
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bezkoder.spring.datajpa.model.Tutorial;
import com.bezkoder.spring.datajpa.repository.TutorialRepository;

// FIX ERR-006: Removed hardcoded @CrossOrigin - now configured in WebConfig.java
@RestController
@RequestMapping("/api")
public class TutorialController {

	// FIX ERR-017: Add logger instance for exception logging
	private static final Logger logger = LoggerFactory.getLogger(TutorialController.class);

	@Autowired
	TutorialRepository tutorialRepository;

	@GetMapping("/tutorials")
	public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
		try {
			List<Tutorial> tutorials = new ArrayList<Tutorial>();

			// FIX ERR-021: Check for both null and empty string to handle edge cases
			// Empty string or whitespace-only strings should return all tutorials
			if (title == null || title.trim().isEmpty())
				tutorialRepository.findAll().forEach(tutorials::add);
			else
				tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			// FIX ERR-017: Log exception details before returning error response
			logger.error("Error retrieving tutorials with title filter '{}': {}", title, e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/tutorials")
	public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
		try {
			// FIX ERR-007: Add backend validation for required fields
			// Prevent creation of tutorials with empty or null title
			if (tutorial.getTitle() == null || tutorial.getTitle().trim().isEmpty()) {
				logger.warn("Attempted to create tutorial with empty title");
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
			// FIX ERR-007: Validate description is not empty
			if (tutorial.getDescription() == null || tutorial.getDescription().trim().isEmpty()) {
				logger.warn("Attempted to create tutorial with empty description");
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}

			// FIX ERR-008: Use actual published value from request instead of hardcoded false
			// This allows creating tutorials in published state directly
			Tutorial _tutorial = tutorialRepository
					.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), tutorial.isPublished()));
			return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);
		} catch (Exception e) {
			// FIX ERR-017: Log exception details for debugging
			logger.error("Error creating tutorial: {}", e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			Tutorial _tutorial = tutorialData.get();
			_tutorial.setTitle(tutorial.getTitle());
			_tutorial.setDescription(tutorial.getDescription());
			
			// FIX ERR-001: Removed conditional check to allow unpublishing tutorials
			// Previous code: if (tutorial.isPublished()) { _tutorial.setPublished(...); }
			// This prevented changing published from true to false
			// Now always updates published status to match request
			_tutorial.setPublished(tutorial.isPublished());
			
			return new ResponseEntity<>(tutorialRepository.save(_tutorial), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/tutorials/{id}")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		try {
			tutorialRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			// FIX ERR-017: Log exception with tutorial ID context
			logger.error("Error deleting tutorial with id {}: {}", id, e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		try {
			tutorialRepository.deleteAll();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			// FIX ERR-017: Log exception for bulk delete failures
			logger.error("Error deleting all tutorials: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		try {
			// Returns published tutorials (published=true)
			// This endpoint semantically should return published items
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			// FIX ERR-017: Log exception for published tutorials query
			logger.error("Error retrieving published tutorials: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
