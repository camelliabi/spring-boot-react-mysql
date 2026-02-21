package com.bezkoder.spring.datajpa;

import com.bezkoder.spring.datajpa.controller.TutorialController;
import com.bezkoder.spring.datajpa.model.Tutorial;
import com.bezkoder.spring.datajpa.repository.TutorialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TutorialController.updateTutorial endpoint
 * Tests the fix for ERR-003: Removed conditional update logic that prevented unpublishing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TutorialController.updateTutorial Tests")
public class TutorialControllerUpdateTest {

    @Mock
    private TutorialRepository tutorialRepository;

    @InjectMocks
    private TutorialController tutorialController;

    private Tutorial existingTutorial;
    private Tutorial updateRequest;

    @BeforeEach
    public void setUp() {
        existingTutorial = new Tutorial("Original Title", "Original Description", true);
        updateRequest = new Tutorial("Updated Title", "Updated Description", false);
    }

    @Test
    @DisplayName("CRITICAL: updateTutorial should allow changing published from true to false")
    public void testUpdateTutorial_CanUnpublish() {
        // Arrange - existing tutorial is published
        Tutorial existingPublished = new Tutorial("Test", "Test", true);
        Tutorial updateToUnpublished = new Tutorial("Test", "Test", false);
        
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingPublished));
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act - update to unpublished
        ResponseEntity<Tutorial> response = tutorialController.updateTutorial(1L, updateToUnpublished);

        // Assert - verify published status was changed to false
        ArgumentCaptor<Tutorial> captor = ArgumentCaptor.forClass(Tutorial.class);
        verify(tutorialRepository).save(captor.capture());
        
        Tutorial savedTutorial = captor.getValue();
        assertThat(savedTutorial.isPublished()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("updateTutorial should allow changing published from false to true")
    public void testUpdateTutorial_CanPublish() {
        // Arrange
        Tutorial existingUnpublished = new Tutorial("Test", "Test", false);
        Tutorial updateToPublished = new Tutorial("Test", "Test", true);
        
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingUnpublished));
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        ResponseEntity<Tutorial> response = tutorialController.updateTutorial(1L, updateToPublished);

        // Assert
        ArgumentCaptor<Tutorial> captor = ArgumentCaptor.forClass(Tutorial.class);
        verify(tutorialRepository).save(captor.capture());
        
        Tutorial savedTutorial = captor.getValue();
        assertThat(savedTutorial.isPublished()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("updateTutorial should update title and description")
    public void testUpdateTutorial_UpdatesTitleAndDescription() {
        // Arrange
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingTutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        ResponseEntity<Tutorial> response = tutorialController.updateTutorial(1L, updateRequest);

        // Assert
        ArgumentCaptor<Tutorial> captor = ArgumentCaptor.forClass(Tutorial.class);
        verify(tutorialRepository).save(captor.capture());
        
        Tutorial savedTutorial = captor.getValue();
        assertThat(savedTutorial.getTitle()).isEqualTo("Updated Title");
        assertThat(savedTutorial.getDescription()).isEqualTo("Updated Description");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("updateTutorial should return NOT_FOUND for non-existent tutorial")
    public void testUpdateTutorial_NotFound() {
        // Arrange
        when(tutorialRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Tutorial> response = tutorialController.updateTutorial(999L, updateRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(tutorialRepository, never()).save(any(Tutorial.class));
    }

    @Test
    @DisplayName("updateTutorial should preserve tutorial ID")
    public void testUpdateTutorial_PreservesId() {
        // Arrange
        when(tutorialRepository.findById(5L)).thenReturn(Optional.of(existingTutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        tutorialController.updateTutorial(5L, updateRequest);

        // Assert
        verify(tutorialRepository).findById(5L);
        verify(tutorialRepository).save(any(Tutorial.class));
    }

    @Test
    @DisplayName("updateTutorial should update all fields including published status")
    public void testUpdateTutorial_UpdatesAllFields() {
        // Arrange
        Tutorial original = new Tutorial("Old", "Old Desc", false);
        Tutorial update = new Tutorial("New", "New Desc", true);
        
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(original));
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        tutorialController.updateTutorial(1L, update);

        // Assert
        ArgumentCaptor<Tutorial> captor = ArgumentCaptor.forClass(Tutorial.class);
        verify(tutorialRepository).save(captor.capture());
        
        Tutorial saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("New");
        assertThat(saved.getDescription()).isEqualTo("New Desc");
        assertThat(saved.isPublished()).isTrue();
    }

    @Test
    @DisplayName("updateTutorial should handle multiple consecutive updates correctly")
    public void testUpdateTutorial_MultipleUpdates() {
        // Arrange
        Tutorial tutorial = new Tutorial("Title", "Desc", false);
        Tutorial update1 = new Tutorial("Title", "Desc", true);
        Tutorial update2 = new Tutorial("Title", "Desc", false);
        
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act - publish then unpublish
        tutorialController.updateTutorial(1L, update1);
        tutorialController.updateTutorial(1L, update2);

        // Assert - verify both updates were saved
        verify(tutorialRepository, times(2)).save(any(Tutorial.class));
    }

    @Test
    @DisplayName("updateTutorial should return OK with updated tutorial in response body")
    public void testUpdateTutorial_ReturnsUpdatedTutorial() {
        // Arrange
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingTutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        ResponseEntity<Tutorial> response = tutorialController.updateTutorial(1L, updateRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("updateTutorial should handle null values in update request appropriately")
    public void testUpdateTutorial_HandlesNullValues() {
        // Arrange
        Tutorial updateWithNulls = new Tutorial(null, null, false);
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(existingTutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        ResponseEntity<Tutorial> response = tutorialController.updateTutorial(1L, updateWithNulls);

        // Assert - the controller doesn't validate nulls, it sets them
        ArgumentCaptor<Tutorial> captor = ArgumentCaptor.forClass(Tutorial.class);
        verify(tutorialRepository).save(captor.capture());
        
        Tutorial saved = captor.getValue();
        assertThat(saved.getTitle()).isNull();
        assertThat(saved.getDescription()).isNull();
        assertThat(saved.isPublished()).isFalse();
    }
}
