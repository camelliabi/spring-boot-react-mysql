package com.bezkoder.spring.datajpa.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {

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

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error retrieving tutorials: {}", e.getMessage(), e);
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
			// FIX: Added input validation for required fields
			if (tutorial.getTitle() == null || tutorial.getTitle().trim().isEmpty()) {
				logger.warn("Attempt to create tutorial with null or empty title");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
			if (tutorial.getDescription() == null || tutorial.getDescription().trim().isEmpty()) {
				logger.warn("Attempt to create tutorial with null or empty description");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
			// FIX: Changed from hardcoded false to respect request body's published value
			// This allows creating tutorials that are already published
			Tutorial tutorial1 = tutorialRepository
					.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), tutorial.isPublished()));
			
			logger.info("Created tutorial with ID: {}", tutorial1.getId());
			return new ResponseEntity<>(tutorial1, HttpStatus.CREATED);
		} catch (Exception e) {
			logger.error("Error creating tutorial: {}", e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			try {
				// FIX: Added input validation for update operations
				if (tutorial.getTitle() == null || tutorial.getTitle().trim().isEmpty()) {
					logger.warn("Attempt to update tutorial {} with null or empty title", id);
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
				
				if (tutorial.getDescription() == null || tutorial.getDescription().trim().isEmpty()) {
					logger.warn("Attempt to update tutorial {} with null or empty description", id);
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
				
				Tutorial _tutorial = tutorialData.get();
				_tutorial.setTitle(tutorial.getTitle());
				_tutorial.setDescription(tutorial.getDescription());
				
				// FIX: Removed conditional check that prevented unpublishing tutorials
				// Previously: if (tutorial.isPublished()) { _tutorial.setPublished(...) }
				// This created a one-way door - tutorials could be published but never unpublished
				// Now: Always update the published status from request, enabling bidirectional toggle
				_tutorial.setPublished(tutorial.isPublished());
				
				Tutorial updatedTutorial = tutorialRepository.save(_tutorial);
				logger.info("Updated tutorial with ID: {}", id);
				return new ResponseEntity<>(updatedTutorial, HttpStatus.OK);
			} catch (Exception e) {
				logger.error("Error updating tutorial {}: {}", id, e.getMessage(), e);
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.warn("Tutorial not found with ID: {}", id);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/tutorials/{id}")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		try {
			tutorialRepository.deleteById(id);
			logger.info("Deleted tutorial with ID: {}", id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			logger.error("Error deleting tutorial {}: {}", id, e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		try {
			tutorialRepository.deleteAll();
			logger.info("Deleted all tutorials");
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			logger.error("Error deleting all tutorials: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		try {
			// FIX #2: Changed from findByPublished(false) to findByPublished(true)
			// The /tutorials/published endpoint should return published tutorials, not unpublished ones
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error retrieving published tutorials: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
