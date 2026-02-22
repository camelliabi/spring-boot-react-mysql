package com.bezkoder.spring.datajpa.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.bezkoder.spring.datajpa.model.Tutorial;
import com.bezkoder.spring.datajpa.repository.TutorialRepository;

import jakarta.validation.Valid;

/**
 * TutorialController - REST API endpoints for Tutorial CRUD operations
 * Multiple fixes applied for better code quality and correctness
 */
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {

	// FIX #6: Added SLF4J logger for proper error tracking and debugging
	private static final Logger logger = LoggerFactory.getLogger(TutorialController.class);

	// FIX #3: Changed from field injection to constructor injection
	// Benefits: immutable dependency, better testability, explicit dependency requirement
	private final TutorialRepository tutorialRepository;

	// FIX #3: Constructor injection (replaces @Autowired field injection)
	public TutorialController(TutorialRepository tutorialRepository) {
		this.tutorialRepository = tutorialRepository;
	}

	// FIX #4: Changed to return 200 OK with empty list instead of 204 NO_CONTENT
	// FIX #6: Added logging for errors
	@GetMapping("/tutorials")
	public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
		try {
			List<Tutorial> tutorials = new ArrayList<>();

			if (title == null) {
				tutorials.addAll(tutorialRepository.findAll());
			} else {
				tutorials.addAll(tutorialRepository.findByTitleContaining(title));
			}

			// FIX #4: Return 200 OK with empty list (REST best practice)
			// 204 NO_CONTENT should only be used for successful operations with no response body (e.g., DELETE)
			return new ResponseEntity<>(tutorials, HttpStatus.OK);

		} catch (Exception e) {
			// FIX #6: Log the error with context for debugging
			logger.error("Error retrieving tutorials", e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id) {
		try {
			Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

			if (tutorialData.isPresent()) {
				return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
			} else {
				// FIX #6: Log warning when tutorial not found
				logger.warn("Tutorial not found with id: {}", id);
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			// FIX #6: Log error with ID context
			logger.error("Error retrieving tutorial with id: {}", id, e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// FIX #5: Added @Valid annotation to trigger validation on Tutorial entity
	// FIX #6: Added logging for successful creation
	@PostMapping("/tutorials")
	public ResponseEntity<Tutorial> createTutorial(@Valid @RequestBody Tutorial tutorial) {
		try {
			// Create new tutorial with published=false by default
			Tutorial _tutorial = tutorialRepository.save(
				new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false)
			);
			// FIX #6: Log successful creation with ID
			logger.info("Created tutorial with id: {}", _tutorial.getId());
			return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);
		} catch (Exception e) {
			// FIX #6: Log creation errors
			logger.error("Error creating tutorial", e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// FIX #2: Removed conditional check that prevented unpublishing tutorials
	// FIX #5: Added @Valid annotation to trigger validation
	// FIX #6: Added logging
	@PutMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, 
	                                               @Valid @RequestBody Tutorial tutorial) {
		try {
			Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

			if (tutorialData.isPresent()) {
				Tutorial _tutorial = tutorialData.get();
				_tutorial.setTitle(tutorial.getTitle());
				_tutorial.setDescription(tutorial.getDescription());
				
				// FIX #2: Always update published field (removed if condition)
				// Previous code: if (tutorial.isPublished()) { _tutorial.setPublished(tutorial.isPublished()); }
				// Problem: Could not unpublish tutorials (change from true to false)
				// Solution: Always set the published value from the request
				_tutorial.setPublished(tutorial.isPublished());
				
				Tutorial updatedTutorial = tutorialRepository.save(_tutorial);
				// FIX #6: Log successful update
				logger.info("Updated tutorial with id: {}", id);
				return new ResponseEntity<>(updatedTutorial, HttpStatus.OK);
			} else {
				// FIX #6: Log when tutorial not found for update
				logger.warn("Tutorial not found for update with id: {}", id);
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			// FIX #6: Log update errors with ID context
			logger.error("Error updating tutorial with id: {}", id, e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials/{id}")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		try {
			tutorialRepository.deleteById(id);
			// FIX #6: Log successful deletion
			logger.info("Deleted tutorial with id: {}", id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			// FIX #6: Log deletion errors
			logger.error("Error deleting tutorial with id: {}", id, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		try {
			tutorialRepository.deleteAll();
			// FIX #6: Log bulk deletion
			logger.info("Deleted all tutorials");
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			// FIX #6: Log bulk deletion errors
			logger.error("Error deleting all tutorials", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// FIX #4: Changed to return 200 OK with empty list instead of 204 NO_CONTENT
	// FIX #6: Added logging
	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		try {
			// Find all published tutorials (published = true)
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);
			
			// FIX #4: Return 200 OK with empty list (REST best practice)
			// Empty result is still a successful query, not "no content"
			return new ResponseEntity<>(tutorials, HttpStatus.OK);
			
		} catch (Exception e) {
			// FIX #6: Log errors when retrieving published tutorials
			logger.error("Error retrieving published tutorials", e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
