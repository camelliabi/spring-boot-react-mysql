package com.bezkoder.spring.datajpa;

import com.bezkoder.spring.datajpa.controller.TutorialController;
import com.bezkoder.spring.datajpa.model.Tutorial;
import com.bezkoder.spring.datajpa.repository.TutorialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TutorialController.findByPublished endpoint
 * Tests the fix for ERR-002: Changed findByPublished(false) to findByPublished(true)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TutorialController.findByPublished Tests")
public class TutorialControllerFindByPublishedTest {

    @Mock
    private TutorialRepository tutorialRepository;

    @InjectMocks
    private TutorialController tutorialController;

    private List<Tutorial> publishedTutorials;
    private List<Tutorial> unpublishedTutorials;

    @BeforeEach
    public void setUp() {
        publishedTutorials = Arrays.asList(
            new Tutorial("Published Tutorial 1", "Description 1", true),
            new Tutorial("Published Tutorial 2", "Description 2", true)
        );

        unpublishedTutorials = Arrays.asList(
            new Tutorial("Unpublished Tutorial 1", "Description 1", false),
            new Tutorial("Unpublished Tutorial 2", "Description 2", false)
        );
    }

    @Test
    @DisplayName("CRITICAL: findByPublished should call repository with true, not false")
    public void testFindByPublished_CallsRepositoryWithTrue() {
        // Arrange
        when(tutorialRepository.findByPublished(true)).thenReturn(publishedTutorials);

        // Act
        tutorialController.findByPublished();

        // Assert - CRITICAL: Verify it calls findByPublished(true), NOT findByPublished(false)
        verify(tutorialRepository, times(1)).findByPublished(true);
        verify(tutorialRepository, never()).findByPublished(false);
    }

    @Test
    @DisplayName("findByPublished should return only published tutorials with status OK")
    public void testFindByPublished_ReturnsPublishedTutorials() {
        // Arrange
        when(tutorialRepository.findByPublished(true)).thenReturn(publishedTutorials);

        // Act
        ResponseEntity<List<Tutorial>> response = tutorialController.findByPublished();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).allMatch(Tutorial::isPublished);
    }

    @Test
    @DisplayName("findByPublished should return NO_CONTENT when no published tutorials exist")
    public void testFindByPublished_NoPublishedTutorials() {
        // Arrange
        when(tutorialRepository.findByPublished(true)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<Tutorial>> response = tutorialController.findByPublished();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("findByPublished should handle repository exception gracefully")
    public void testFindByPublished_HandlesException() {
        // Arrange
        when(tutorialRepository.findByPublished(true)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<Tutorial>> response = tutorialController.findByPublished();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("findByPublished should never return unpublished tutorials")
    public void testFindByPublished_NeverReturnsUnpublished() {
        // Arrange
        when(tutorialRepository.findByPublished(true)).thenReturn(publishedTutorials);

        // Act
        ResponseEntity<List<Tutorial>> response = tutorialController.findByPublished();

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).noneMatch(t -> !t.isPublished());
    }

    @Test
    @DisplayName("findByPublished should return empty body with NO_CONTENT for empty list")
    public void testFindByPublished_EmptyListReturnsNoContent() {
        // Arrange
        when(tutorialRepository.findByPublished(true)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<Tutorial>> response = tutorialController.findByPublished();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.hasBody()).isFalse();
    }
}
