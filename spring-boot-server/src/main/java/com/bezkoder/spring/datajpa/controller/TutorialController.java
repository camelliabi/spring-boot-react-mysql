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

import com.bezkoder.spring.datajpa.model.Tutorial;
import com.bezkoder.spring.datajpa.repository.TutorialRepository;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {

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
	public ResponseEntity<?> createTutorial(@RequestBody Tutorial tutorial) {
		try {
			// BUG #2 FIX: Add validation for null/empty title and description
			if (tutorial.getTitle() == null || tutorial.getTitle().trim().isEmpty()) {
				return new ResponseEntity<>("Title is required and cannot be empty", HttpStatus.BAD_REQUEST);
			}
			
			if (tutorial.getDescription() == null || tutorial.getDescription().trim().isEmpty()) {
				return new ResponseEntity<>("Description is required and cannot be empty", HttpStatus.BAD_REQUEST);
			}

			// Trim whitespace from inputs to prevent storage of whitespace-only strings
			tutorial.setTitle(tutorial.getTitle().trim());
			tutorial.setDescription(tutorial.getDescription().trim());

			Tutorial _tutorial = tutorialRepository.save(new Tutorial(
				tutorial.getTitle(), 
				tutorial.getDescription(), 
				false
			));
			
			return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/tutorials/{id}")
	public ResponseEntity<?> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

		if (tutorialData.isPresent()) {
			// BUG #4 FIX: Add validation for null/empty title and description in update
			if (tutorial.getTitle() == null || tutorial.getTitle().trim().isEmpty()) {
				return new ResponseEntity<>("Title is required and cannot be empty", HttpStatus.BAD_REQUEST);
			}
			
			if (tutorial.getDescription() == null || tutorial.getDescription().trim().isEmpty()) {
				return new ResponseEntity<>("Description is required and cannot be empty", HttpStatus.BAD_REQUEST);
			}

			Tutorial _tutorial = tutorialData.get();
			
			// Trim whitespace from inputs
			_tutorial.setTitle(tutorial.getTitle().trim());
			_tutorial.setDescription(tutorial.getDescription().trim());
			
			// BUG #3 FIX: Remove conditional check - always update published status
			// Previous code: if (tutorial.isPublished()) { _tutorial.setPublished(tutorial.isPublished()); }
			// This caused published status to be lost when tutorial.isPublished() was false
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
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		try {
			tutorialRepository.deleteAll();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		try {
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

			if (tutorials.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			
			return new ResponseEntity<>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
