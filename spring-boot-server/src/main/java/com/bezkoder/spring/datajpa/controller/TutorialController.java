package com.bezkoder.spring.datajpa.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

// FIX ERROR-015: Removed hard-coded @CrossOrigin annotation
// CORS configuration is now externalized to WebConfig.java and application.properties
// This allows different origins for dev, staging, and production environments
@RestController
@RequestMapping("/api")
public class TutorialController {

	// FIX ERROR-013: Added SLF4J logger for proper exception logging
	// Enables debugging and monitoring in production environments
	private static final Logger logger = LoggerFactory.getLogger(TutorialController.class);

	@Autowired
	TutorialRepository tutorialRepository;

	@GetMapping("/tutorials")
	public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
		try {
			List<Tutorial> tutorials = new ArrayList<Tutorial>();

			if (title == null)
				tutorialRepository.findAll().forEach(tutorials::add);
			else
				tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);

			if (tutorials.size() < 1) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			// FIX ERROR-013: Added comprehensive error logging with stack trace
			logger.error("Error retrieving tutorials with title filter '{}': {}", title, e.getMessage(), e);
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
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			// FIX ERROR-013: Log exceptions with context (tutorial ID)
			logger.error("Error retrieving tutorial by id {}: {}", id, e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/tutorials")
	public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
		try {
			// FIX ERROR-008: Added input validation for title
			// Prevents null or empty titles from being persisted to database
			if (tutorial.getTitle() == null || tutorial.getTitle().trim().isEmpty()) {
				logger.warn("Attempt to create tutorial with null or empty title");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
			// FIX ERROR-008: Added input validation for description
			// Prevents null or empty descriptions from being persisted
			if (tutorial.getDescription() == null || tutorial.getDescription().trim().isEmpty()) {
				logger.warn("Attempt to create tutorial with null or empty description");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
			// FIX ERROR-007: Changed from hard-coded 'false' to tutorial.isPublished()
			// Now respects the published status from the request body
			// Allows creating tutorials that are already published
			Tutorial tutorial1 = tutorialRepository
					.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), tutorial.isPublished()));
			return new ResponseEntity<>(tutorial1, HttpStatus.CREATED);
		} catch (Exception e) {
			// FIX ERROR-013: Log creation failures with full stack trace
			logger.error("Error creating tutorial: {}", e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
		try {
			Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

			if (tutorialData.isPresent()) {
				// FIX ERROR-008: Added validation for empty title during update
				// Allows null (meaning "don't update") but rejects empty strings
				if (tutorial.getTitle() != null && tutorial.getTitle().trim().isEmpty()) {
					logger.warn("Attempt to update tutorial {} with empty title", id);
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
				
				// FIX ERROR-008: Added validation for empty description during update
				if (tutorial.getDescription() != null && tutorial.getDescription().trim().isEmpty()) {
					logger.warn("Attempt to update tutorial {} with empty description", id);
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
				
				Tutorial _tutorial = tutorialData.get();
				
				// Only update non-null values to support partial updates
				if (tutorial.getTitle() != null) {
					_tutorial.setTitle(tutorial.getTitle());
				}
				if (tutorial.getDescription() != null) {
					_tutorial.setDescription(tutorial.getDescription());
				}
				
				// FIX ERROR-006: Removed conditional check that prevented unpublishing
				// Previous code: if (tutorial.isPublished()) { _tutorial.setPublished(...) }
				// This blocked setting published=false (unpublishing tutorials)
				// Now always updates published status, enabling bidirectional state transitions
				_tutorial.setPublished(tutorial.isPublished());
				
				return new ResponseEntity<>(tutorialRepository.save(_tutorial), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			// FIX ERROR-013: Log update failures with tutorial ID context
			logger.error("Error updating tutorial {}: {}", id, e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials/{id}")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		try {
			tutorialRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			// FIX ERROR-013: Log deletion failures
			logger.error("Error deleting tutorial {}: {}", id, e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		try {
			tutorialRepository.deleteAll();
			logger.info("All tutorials deleted successfully");
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			// FIX ERROR-013: Log bulk deletion failures
			logger.error("Error deleting all tutorials: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		try {
			// Already fixed: Changed from findByPublished(false) to findByPublished(true)
			// The /tutorials/published endpoint should return published tutorials, not unpublished ones
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			// FIX ERROR-013: Log query failures
			logger.error("Error finding published tutorials: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
