package com.bezkoder.spring.datajpa.controller;

import com.bezkoder.spring.datajpa.model.Tutorial;
import com.bezkoder.spring.datajpa.repository.TutorialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TutorialController
 * Tests the bug fixes for:
 * - ERROR-002: Redundant boolean comparison in updateTutorial
 * - ERROR-003: Incorrect published status filter in findByPublished
 */
@WebMvcTest(TutorialController.class)
class TutorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TutorialRepository tutorialRepository;

    /**
     * TEST: Validates getAllTutorials without title filter
     */
    @Test
    void testGetAllTutorials_WithoutTitle_ReturnsAllTutorials() throws Exception {
        // Given
        Tutorial tutorial1 = new Tutorial("Spring Boot", "Description 1", true);
        Tutorial tutorial2 = new Tutorial("React Tutorial", "Description 2", false);
        when(tutorialRepository.findAll()).thenReturn(Arrays.asList(tutorial1, tutorial2));

        // When & Then
        mockMvc.perform(get("/api/tutorials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(tutorialRepository, times(1)).findAll();
    }

    /**
     * TEST: Validates getAllTutorials with title filter
     */
    @Test
    void testGetAllTutorials_WithTitle_ReturnsFilteredTutorials() throws Exception {
        // Given
        Tutorial tutorial1 = new Tutorial("Spring Boot", "Description 1", true);
        when(tutorialRepository.findByTitleContaining("Spring")).thenReturn(Collections.singletonList(tutorial1));

        // When & Then
        mockMvc.perform(get("/api/tutorials").param("title", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Spring Boot"));

        verify(tutorialRepository, times(1)).findByTitleContaining("Spring");
    }

    /**
     * TEST: Validates getTutorialById for existing tutorial
     */
    @Test
    void testGetTutorialById_ExistingId_ReturnsTutorial() throws Exception {
        // Given
        Tutorial tutorial = new Tutorial("Spring Boot", "Description", true);
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));

        // When & Then
        mockMvc.perform(get("/api/tutorials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Boot"));

        verify(tutorialRepository, times(1)).findById(1L);
    }

    /**
     * TEST: Validates getTutorialById for non-existing tutorial
     */
    @Test
    void testGetTutorialById_NonExistingId_ReturnsNotFound() throws Exception {
        // Given
        when(tutorialRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/tutorials/999"))
                .andExpect(status().isNotFound());

        verify(tutorialRepository, times(1)).findById(999L);
    }

    /**
     * TEST: Validates createTutorial
     */
    @Test
    void testCreateTutorial_ValidTutorial_ReturnsCreated() throws Exception {
        // Given
        Tutorial inputTutorial = new Tutorial("New Tutorial", "New Description", false);
        Tutorial savedTutorial = new Tutorial("New Tutorial", "New Description", false);
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(savedTutorial);

        // When & Then
        mockMvc.perform(post("/api/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputTutorial)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Tutorial"));

        verify(tutorialRepository, times(1)).save(any(Tutorial.class));
    }

    /**
     * TEST: Validates ERROR-002 fix - updateTutorial should properly handle boolean without == true
     */
    @Test
    void testUpdateTutorial_PublishedFieldUpdate_WorksCorrectly() throws Exception {
        // Given: Existing unpublished tutorial
        Tutorial existingTutorial = new Tutorial("Old Title", "Old Description", false);
        Tutorial updateRequest = new Tutorial("New Title", "New Description", true);
        
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingTutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(existingTutorial);

        // When & Then
        mockMvc.perform(put("/api/tutorials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Verify that setPublished was called with the correct value (validates ERROR-002 fix)
        verify(tutorialRepository, times(1)).save(argThat(tutorial -> 
            tutorial.isPublished() == true && 
            tutorial.getTitle().equals("New Title") &&
            tutorial.getDescription().equals("New Description")
        ));
    }

    /**
     * TEST: Validates updateTutorial for non-existing tutorial
     */
    @Test
    void testUpdateTutorial_NonExistingId_ReturnsNotFound() throws Exception {
        // Given
        Tutorial updateRequest = new Tutorial("Title", "Description", true);
        when(tutorialRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/tutorials/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(tutorialRepository, never()).save(any(Tutorial.class));
    }

    /**
     * TEST: Validates deleteTutorial
     */
    @Test
    void testDeleteTutorial_ValidId_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(tutorialRepository).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/tutorials/1"))
                .andExpect(status().isNoContent());

        verify(tutorialRepository, times(1)).deleteById(1L);
    }

    /**
     * TEST: Validates deleteAllTutorials
     */
    @Test
    void testDeleteAllTutorials_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(tutorialRepository).deleteAll();

        // When & Then
        mockMvc.perform(delete("/api/tutorials"))
                .andExpect(status().isNoContent());

        verify(tutorialRepository, times(1)).deleteAll();
    }

    /**
     * TEST: Validates ERROR-003 fix - findByPublished should return published=true tutorials
     */
    @Test
    void testFindByPublished_ReturnsPublishedTutorials() throws Exception {
        // Given: Published tutorials
        Tutorial published1 = new Tutorial("Published 1", "Description 1", true);
        Tutorial published2 = new Tutorial("Published 2", "Description 2", true);
        when(tutorialRepository.findByPublished(true)).thenReturn(Arrays.asList(published1, published2));

        // When & Then
        mockMvc.perform(get("/api/tutorials/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].published").value(true))
                .andExpect(jsonPath("$[1].published").value(true));

        // CRITICAL: Validates ERROR-003 fix - should call findByPublished(true), NOT false
        verify(tutorialRepository, times(1)).findByPublished(true);
        verify(tutorialRepository, never()).findByPublished(false);
    }

    /**
     * TEST: Validates findByPublished when no published tutorials exist
     */
    @Test
    void testFindByPublished_NoPublishedTutorials_ReturnsNoContent() throws Exception {
        // Given
        when(tutorialRepository.findByPublished(true)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/tutorials/published"))
                .andExpect(status().isNoContent());

        verify(tutorialRepository, times(1)).findByPublished(true);
    }
}
