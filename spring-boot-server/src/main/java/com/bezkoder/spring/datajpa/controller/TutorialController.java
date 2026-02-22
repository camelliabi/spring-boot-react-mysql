package com.bezkoder.spring.datajpa.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.bezkoder.spring.datajpa.model.Tutorial;
import com.bezkoder.spring.datajpa.repository.TutorialRepository;

// FIX #13: Changed from hard-coded localhost:8081 to use property placeholder
// This allows configuration via application.properties or environment variables
// For production, set CORS_ALLOWED_ORIGINS environment variable
@CrossOrigin(origins = "${cors.allowed.origins:http://localhost:8081}")
@RestController
@RequestMapping("/api")
public class TutorialController {

	// FIX #11: Added logger for proper exception tracking
	// This helps debug production issues instead of silently swallowing exceptions
	private static final Logger logger = LoggerFactory.getLogger(TutorialController.class);

	@Autowired
	TutorialRepository tutorialRepository;

	// FIX #12: Added optional 'published' parameter to support filtering by published status
	// Now supports: /api/tutorials, /api/tutorials?title=X, /api/tutorials?published=true
	// and /api/tutorials?title=X&published=true
	@GetMapping("/tutorials")
	public ResponseEntity<List<Tutorial>> getAllTutorials(
			@RequestParam(required = false) String title,
			@RequestParam(required = false) Boolean published) {
		try {
			List<Tutorial> tutorials = new ArrayList<Tutorial>();

			if (title == null && published == null) {
				tutorialRepository.findAll().forEach(tutorials::add);
			} else if (title != null && published != null) {
				// Use combined query when both filters are provided
				tutorialRepository.findByTitleContainingAndPublished(title, published).forEach(tutorials::add);
			} else if (title != null) {
				tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);
			} else {
				// Filter by published status only
				tutorialRepository.findByPublished(published).forEach(tutorials::add);
			}

			// FIX #10: Changed from size() < 1 to isEmpty() for consistency and readability
			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			// FIX #11: Added logging to track errors in production
			logger.error("Error retrieving tutorials with title: {} and published: {}", title, published, e);
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

	// FIX #8: Added @Valid annotation to enable automatic validation
	// This triggers validation of @NotBlank and @Size constraints from Tutorial entity
	@PostMapping("/tutorials")
	public ResponseEntity<Tutorial> createTutorial(@Valid @RequestBody Tutorial tutorial) {
		try {
			// FIX #9: Changed from hard-coded 'false' to tutorial.isPublished()
			// Now respects the published value sent in the request body
			// This allows creating tutorials that are already published
			Tutorial _tutorial = tutorialRepository
					.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), tutorial.isPublished()));
			return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);
		} catch (Exception e) {
			// FIX #11: Added logging for create errors
			logger.error("Error creating tutorial: {}", tutorial, e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// FIX #8: Added @Valid annotation for update validation
	@PutMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @Valid @RequestBody Tutorial tutorial) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			Tutorial _tutorial = tutorialData.get();
			_tutorial.setTitle(tutorial.getTitle());
			_tutorial.setDescription(tutorial.getDescription());
			
			// FIX #5: Removed the condition 'if (tutorial.isPublished() == true)'
			// Previous code only updated published status when true, preventing unpublishing
			// Now always updates published status, allowing both publish and unpublish operations
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
			// FIX #11: Added logging for delete errors
			logger.error("Error deleting tutorial with id: {}", id, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		try {
			tutorialRepository.deleteAll();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			// FIX #11: Added logging for bulk delete errors
			logger.error("Error deleting all tutorials", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		try {
			// FIX #1: CRITICAL - Changed from findByPublished(false) to findByPublished(true)
			// The endpoint /tutorials/published should return PUBLISHED tutorials, not unpublished ones
			// This was a major logic error causing the API to return the opposite dataset
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

			// FIX #10: Changed to isEmpty() for consistency
			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			// FIX #11: Added logging for published query errors
			logger.error("Error retrieving published tutorials", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
