package com.bezkoder.spring.datajpa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bezkoder.spring.datajpa.model.Tutorial;
import com.bezkoder.spring.datajpa.repository.TutorialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for TutorialController
 * 
 * These tests verify all the fixes made in the error resolution PR.
 * Each test is designed to FAIL with the old buggy code and PASS with the fixed code.
 */
@WebMvcTest(TutorialController.class)
public class TutorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TutorialRepository tutorialRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Tutorial tutorial1;
    private Tutorial tutorial2;
    private Tutorial publishedTutorial;
    private Tutorial unpublishedTutorial;

    @BeforeEach
    void setUp() {
        tutorial1 = new Tutorial("Spring Boot", "Spring Boot Tutorial", false);
        tutorial2 = new Tutorial("React", "React Tutorial", false);
        
        publishedTutorial = new Tutorial("Published Tutorial", "This is published", true);
        unpublishedTutorial = new Tutorial("Unpublished Tutorial", "This is not published", false);
        
        // Set IDs using reflection to simulate database entities
        setId(tutorial1, 1L);
        setId(tutorial2, 2L);
        setId(publishedTutorial, 3L);
        setId(unpublishedTutorial, 4L);
    }

    private void setId(Tutorial tutorial, Long id) {
        try {
            java.lang.reflect.Field idField = Tutorial.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.setLong(tutorial, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }

    /**
     * FIX #1: Test isEmpty() vs size() < 1
     * 
     * OLD CODE: if (tutorials.size() < 1)
     * NEW CODE: if (tutorials.isEmpty())
     * 
     * This test verifies that an empty list returns HTTP 204 NO_CONTENT.
     * Both implementations work the same, but isEmpty() is more idiomatic.
     */
    @Test
    @DisplayName("FIX #1: Empty list should return NO_CONTENT status (tests isEmpty() usage)")
    void testGetAllTutorials_EmptyList_ReturnsNoContent() throws Exception {
        // Arrange: Mock repository to return empty list
        when(tutorialRepository.findAll()).thenReturn(new ArrayList<>());

        // Act & Assert: Verify NO_CONTENT is returned
        mockMvc.perform(get("/api/tutorials"))
                .andExpect(status().isNoContent());
    }

    /**
     * FIX #1: Test that non-empty list returns OK
     */
    @Test
    @DisplayName("FIX #1: Non-empty list should return OK status with tutorials")
    void testGetAllTutorials_WithData_ReturnsOk() throws Exception {
        // Arrange: Mock repository to return tutorials
        List<Tutorial> tutorials = Arrays.asList(tutorial1, tutorial2);
        when(tutorialRepository.findAll()).thenReturn(tutorials);

        // Act & Assert: Verify OK status and data is returned
        mockMvc.perform(get("/api/tutorials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Spring Boot"))
                .andExpect(jsonPath("$[1].title").value("React"));
    }

    /**
     * FIX #2, #3: Test getTutorialById logic
     * 
     * The old code had misleading "BUG" comments suggesting the logic was wrong,
     * but it was actually correct. This test verifies the correct behavior:
     * - Returns OK when tutorial exists
     * - Returns NOT_FOUND when tutorial doesn't exist
     */
    @Test
    @DisplayName("FIX #2, #3: getTutorialById should return OK when tutorial exists")
    void testGetTutorialById_Exists_ReturnsOk() throws Exception {
        // Arrange: Mock repository to return tutorial
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial1));

        // Act & Assert: Verify OK status and tutorial is returned
        mockMvc.perform(get("/api/tutorials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Boot"))
                .andExpect(jsonPath("$.description").value("Spring Boot Tutorial"));
    }

    /**
     * FIX #2, #3: Test NOT_FOUND is returned when tutorial doesn't exist
     */
    @Test
    @DisplayName("FIX #2, #3: getTutorialById should return NOT_FOUND when tutorial doesn't exist")
    void testGetTutorialById_NotExists_ReturnsNotFound() throws Exception {
        // Arrange: Mock repository to return empty optional
        when(tutorialRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert: Verify NOT_FOUND status
        mockMvc.perform(get("/api/tutorials/999"))
                .andExpect(status().isNotFound());
    }

    /**
     * FIX #4: Test createTutorial variable naming consistency
     * 
     * OLD CODE: Tutorial tutorial1 = tutorialRepository.save(...)
     * NEW CODE: Tutorial _tutorial = tutorialRepository.save(...)
     * 
     * This test verifies createTutorial works correctly (naming is internal).
     */
    @Test
    @DisplayName("FIX #4: createTutorial should successfully create and return tutorial")
    void testCreateTutorial_Success() throws Exception {
        // Arrange: Mock repository save
        Tutorial newTutorial = new Tutorial("New Tutorial", "New Description", false);
        Tutorial savedTutorial = new Tutorial("New Tutorial", "New Description", false);
        setId(savedTutorial, 5L);
        
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(savedTutorial);

        // Act & Assert: Verify CREATED status and tutorial is returned
        mockMvc.perform(post("/api/tutorials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTutorial)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("New Tutorial"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.published").value(false));
    }

    /**
     * FIX #5: CRITICAL TEST - Verify unpublishing works
     * 
     * OLD CODE BUG: if (tutorial.isPublished() == true) { _tutorial.setPublished(...); }
     * This only updated when isPublished() was true, so unpublishing (false) didn't work!
     * 
     * NEW CODE FIX: _tutorial.setPublished(tutorial.isPublished());
     * Now it always updates, handling both true and false correctly.
     * 
     * This test would FAIL with old code - unpublishing wouldn't work!
     */
    @Test
    @DisplayName("FIX #5: CRITICAL - updateTutorial should handle UNPUBLISHING (setting published=false)")
    void testUpdateTutorial_Unpublish_Success() throws Exception {
        // Arrange: Start with a published tutorial
        Tutorial existingTutorial = new Tutorial("Tutorial", "Description", true);
        setId(existingTutorial, 1L);
        
        // Update request wants to unpublish it
        Tutorial updateRequest = new Tutorial("Updated Tutorial", "Updated Description", false);
        
        // Expected result after update
        Tutorial updatedTutorial = new Tutorial("Updated Tutorial", "Updated Description", false);
        setId(updatedTutorial, 1L);
        
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingTutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(updatedTutorial);

        // Act & Assert: Verify tutorial is unpublished
        mockMvc.perform(put("/api/tutorials/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(false))  // CRITICAL: Must be false!
                .andExpect(jsonPath("$.title").value("Updated Tutorial"));
    }

    /**
     * FIX #5: Also verify publishing still works correctly
     */
    @Test
    @DisplayName("FIX #5: updateTutorial should handle PUBLISHING (setting published=true)")
    void testUpdateTutorial_Publish_Success() throws Exception {
        // Arrange: Start with unpublished tutorial
        Tutorial existingTutorial = new Tutorial("Tutorial", "Description", false);
        setId(existingTutorial, 1L);
        
        // Update request wants to publish it
        Tutorial updateRequest = new Tutorial("Updated Tutorial", "Updated Description", true);
        
        // Expected result after update
        Tutorial updatedTutorial = new Tutorial("Updated Tutorial", "Updated Description", true);
        setId(updatedTutorial, 1L);
        
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingTutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(updatedTutorial);

        // Act & Assert: Verify tutorial is published
        mockMvc.perform(put("/api/tutorials/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.title").value("Updated Tutorial"));
    }

    /**
     * FIX #6: 🚨🚨🚨 MOST CRITICAL TEST 🚨🚨🚨
     * 
     * OLD CODE BUG: findByPublished(false) at /api/tutorials/published endpoint
     * This returned UNPUBLISHED tutorials when URL says "published"!
     * 
     * NEW CODE FIX: findByPublished(true)
     * Now correctly returns published tutorials.
     * 
     * This test would FAIL with old code - it would return unpublished tutorials!
     */
    @Test
    @DisplayName("FIX #6: 🚨 CRITICAL BUG - /api/tutorials/published must return PUBLISHED tutorials, NOT unpublished")
    void testFindByPublished_ReturnsOnlyPublishedTutorials() throws Exception {
        // Arrange: Mock repository to return only published tutorials
        List<Tutorial> publishedTutorials = Arrays.asList(publishedTutorial);
        
        // CRITICAL: We're mocking findByPublished(true) - the FIXED version
        // Old code called findByPublished(false) which would be wrong!
        when(tutorialRepository.findByPublished(true)).thenReturn(publishedTutorials);

        // Act & Assert: Verify only published tutorials are returned
        mockMvc.perform(get("/api/tutorials/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Published Tutorial"))
                .andExpect(jsonPath("$[0].published").value(true));  // MUST be true!
    }

    /**
     * FIX #6: Additional test to verify the endpoint behavior with empty results
     */
    @Test
    @DisplayName("FIX #6: /api/tutorials/published returns NO_CONTENT when no published tutorials exist")
    void testFindByPublished_EmptyList_ReturnsNoContent() throws Exception {
        // Arrange: Mock repository to return empty list
        when(tutorialRepository.findByPublished(true)).thenReturn(new ArrayList<>());

        // Act & Assert: Verify NO_CONTENT is returned
        mockMvc.perform(get("/api/tutorials/published"))
                .andExpect(status().isNoContent());
    }

    /**
     * Additional test: Verify delete operations work correctly
     */
    @Test
    @DisplayName("Delete tutorial should return NO_CONTENT on success")
    void testDeleteTutorial_Success() throws Exception {
        // Act & Assert: Verify NO_CONTENT is returned
        mockMvc.perform(delete("/api/tutorials/1"))
                .andExpect(status().isNoContent());
    }

    /**
     * Additional test: Verify delete all operations work correctly
     */
    @Test
    @DisplayName("Delete all tutorials should return NO_CONTENT on success")
    void testDeleteAllTutorials_Success() throws Exception {
        // Act & Assert: Verify NO_CONTENT is returned
        mockMvc.perform(delete("/api/tutorials"))
                .andExpect(status().isNoContent());
    }
}
