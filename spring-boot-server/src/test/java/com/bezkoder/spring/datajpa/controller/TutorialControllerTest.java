package com.bezkoder.spring.datajpa.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
 * Comprehensive test suite for TutorialController
 * Tests all fixes: #1, #5, #8, #9, #10, #11, #12
 */
@WebMvcTest(TutorialController.class)
@DisplayName("TutorialController Tests")
class TutorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TutorialRepository tutorialRepository;

    // ========== FIX #1: findByPublished endpoint tests ==========

    @Test
    @DisplayName("FIX #1: GET /api/tutorials/published should return only PUBLISHED tutorials")
    void testFindByPublished_ReturnsPublishedTutorials() throws Exception {
        // Given: Repository has published tutorials
        Tutorial published1 = new Tutorial("Published Tutorial 1", "Description 1", true);
        Tutorial published2 = new Tutorial("Published Tutorial 2", "Description 2", true);
        List<Tutorial> publishedTutorials = Arrays.asList(published1, published2);

        when(tutorialRepository.findByPublished(true)).thenReturn(publishedTutorials);

        // When & Then: GET /api/tutorials/published returns published tutorials
        mockMvc.perform(get("/api/tutorials/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Published Tutorial 1"))
                .andExpect(jsonPath("$[1].title").value("Published Tutorial 2"));

        // Verify: Repository was called with true (not false)
        verify(tutorialRepository).findByPublished(true);
        verify(tutorialRepository, never()).findByPublished(false);
    }

    @Test
    @DisplayName("FIX #1: GET /api/tutorials/published should return 204 when no published tutorials exist")
    void testFindByPublished_NoContent() throws Exception {
        // Given: No published tutorials
        when(tutorialRepository.findByPublished(true)).thenReturn(new ArrayList<>());

        // When & Then: Returns 204 NO_CONTENT
        mockMvc.perform(get("/api/tutorials/published"))
                .andExpect(status().isNoContent());

        verify(tutorialRepository).findByPublished(true);
    }

    // ========== FIX #5: updateTutorial unpublish tests ==========

    @Test
    @DisplayName("FIX #5: PUT /api/tutorials/{id} should allow unpublishing (true -> false)")
    void testUpdateTutorial_CanUnpublish() throws Exception {
        // Given: Tutorial exists and is published
        Tutorial existingTutorial = new Tutorial("Test Tutorial", "Description", true);
        existingTutorial.setPublished(true);

        Tutorial updatedTutorial = new Tutorial("Test Tutorial", "Description", false);

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingTutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(updatedTutorial);

        // When: Update with published=false
        String requestBody = objectMapper.writeValueAsString(updatedTutorial);

        // Then: Tutorial is unpublished
        mockMvc.perform(put("/api/tutorials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(false));

        verify(tutorialRepository).save(argThat(tutorial -> !tutorial.isPublished()));
    }

    @Test
    @DisplayName("FIX #5: PUT /api/tutorials/{id} should allow publishing (false -> true)")
    void testUpdateTutorial_CanPublish() throws Exception {
        // Given: Tutorial exists and is unpublished
        Tutorial existingTutorial = new Tutorial("Test Tutorial", "Description", false);

        Tutorial updatedTutorial = new Tutorial("Test Tutorial", "Description", true);

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingTutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(updatedTutorial);

        // When: Update with published=true
        String requestBody = objectMapper.writeValueAsString(updatedTutorial);

        // Then: Tutorial is published
        mockMvc.perform(put("/api/tutorials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));

        verify(tutorialRepository).save(argThat(Tutorial::isPublished));
    }

    // ========== FIX #8 & #9: createTutorial validation tests ==========

    @Test
    @DisplayName("FIX #8: POST /api/tutorials with blank title should return 400 Bad Request")
    void testCreateTutorial_BlankTitle_ReturnsBadRequest() throws Exception {
        // Given: Tutorial with blank title
        Tutorial invalidTutorial = new Tutorial("", "Valid Description", false);
        String requestBody = objectMapper.writeValueAsString(invalidTutorial);

        // When & Then: Returns 400 Bad Request due to @NotBlank validation
        mockMvc.perform(post("/api/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(tutorialRepository, never()).save(any());
    }

    @Test
    @DisplayName("FIX #9: POST /api/tutorials should respect published value from request body")
    void testCreateTutorial_RespectsPublishedValue() throws Exception {
        // Given: Tutorial with published=true in request
        Tutorial newTutorial = new Tutorial("New Tutorial", "Description", true);

        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(newTutorial);

        String requestBody = objectMapper.writeValueAsString(newTutorial);

        // When & Then: Created tutorial has published=true (not hard-coded false)
        mockMvc.perform(post("/api/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.published").value(true));

        verify(tutorialRepository).save(argThat(Tutorial::isPublished));
    }

    @Test
    @DisplayName("FIX #9: POST /api/tutorials with published=false should create unpublished tutorial")
    void testCreateTutorial_UnpublishedValue() throws Exception {
        // Given: Tutorial with published=false
        Tutorial newTutorial = new Tutorial("New Tutorial", "Description", false);

        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(newTutorial);

        String requestBody = objectMapper.writeValueAsString(newTutorial);

        // When & Then: Created tutorial has published=false
        mockMvc.perform(post("/api/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.published").value(false));

        verify(tutorialRepository).save(argThat(tutorial -> !tutorial.isPublished()));
    }

    // ========== FIX #12: getAllTutorials with filters tests ==========

    @Test
    @DisplayName("FIX #12: GET /api/tutorials should return all tutorials when no filters")
    void testGetAllTutorials_NoFilters() throws Exception {
        // Given: Repository has tutorials
        List<Tutorial> allTutorials = Arrays.asList(
                new Tutorial("Tutorial 1", "Desc 1", true),
                new Tutorial("Tutorial 2", "Desc 2", false)
        );

        when(tutorialRepository.findAll()).thenReturn(allTutorials);

        // When & Then: Returns all tutorials
        mockMvc.perform(get("/api/tutorials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(tutorialRepository).findAll();
    }

    @Test
    @DisplayName("FIX #12: GET /api/tutorials?title=X should filter by title")
    void testGetAllTutorials_FilterByTitle() throws Exception {
        // Given: Repository has tutorials matching title
        List<Tutorial> matchingTutorials = Arrays.asList(
                new Tutorial("Spring Boot Tutorial", "Desc", true)
        );

        when(tutorialRepository.findByTitleContaining("Spring")).thenReturn(matchingTutorials);

        // When & Then: Returns filtered tutorials
        mockMvc.perform(get("/api/tutorials?title=Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Spring Boot Tutorial"));

        verify(tutorialRepository).findByTitleContaining("Spring");
    }

    @Test
    @DisplayName("FIX #12: GET /api/tutorials?published=true should filter by published status")
    void testGetAllTutorials_FilterByPublished() throws Exception {
        // Given: Repository has published tutorials
        List<Tutorial> publishedTutorials = Arrays.asList(
                new Tutorial("Published Tutorial", "Desc", true)
        );

        when(tutorialRepository.findByPublished(true)).thenReturn(publishedTutorials);

        // When & Then: Returns only published tutorials
        mockMvc.perform(get("/api/tutorials?published=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(tutorialRepository).findByPublished(true);
    }

    @Test
    @DisplayName("FIX #12: GET /api/tutorials?title=X&published=true should filter by both")
    void testGetAllTutorials_FilterByTitleAndPublished() throws Exception {
        // Given: Repository has tutorials matching both criteria
        List<Tutorial> matchingTutorials = Arrays.asList(
                new Tutorial("Spring Boot Tutorial", "Desc", true)
        );

        when(tutorialRepository.findByTitleContainingAndPublished("Spring", true))
                .thenReturn(matchingTutorials);

        // When & Then: Returns tutorials matching both filters
        mockMvc.perform(get("/api/tutorials?title=Spring&published=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(tutorialRepository).findByTitleContainingAndPublished("Spring", true);
    }

    // ========== FIX #10: isEmpty() consistency tests ==========

    @Test
    @DisplayName("FIX #10: Empty result should return 204 NO_CONTENT using isEmpty()")
    void testGetAllTutorials_EmptyResult_NoContent() throws Exception {
        // Given: No tutorials
        when(tutorialRepository.findAll()).thenReturn(new ArrayList<>());

        // When & Then: Returns 204 NO_CONTENT
        mockMvc.perform(get("/api/tutorials"))
                .andExpect(status().isNoContent());
    }
}
