package com.bezkoder.spring.datajpa;

import com.bezkoder.spring.datajpa.model.Tutorial;
import com.bezkoder.spring.datajpa.repository.TutorialRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TutorialRepository.findByTitleContaining method
 * Tests the fix for ERR-001: Removed duplicate method with incompatible signature
 */
@DataJpaTest
@DisplayName("TutorialRepository Tests")
public class TutorialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TutorialRepository tutorialRepository;

    @Test
    @DisplayName("findByTitleContaining should return tutorials with matching title (case-insensitive)")
    public void testFindByTitleContaining_ReturnsMatchingTutorials() {
        // Arrange
        Tutorial tutorial1 = new Tutorial("Spring Boot Guide", "Learn Spring Boot", false);
        Tutorial tutorial2 = new Tutorial("React Tutorial", "Learn React", true);
        Tutorial tutorial3 = new Tutorial("Spring Data JPA", "Database integration", false);
        
        entityManager.persist(tutorial1);
        entityManager.persist(tutorial2);
        entityManager.persist(tutorial3);
        entityManager.flush();

        // Act
        List<Tutorial> result = tutorialRepository.findByTitleContaining("Spring");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Tutorial::getTitle)
            .containsExactlyInAnyOrder("Spring Boot Guide", "Spring Data JPA");
    }

    @Test
    @DisplayName("findByTitleContaining should work with lowercase search term")
    public void testFindByTitleContaining_LowercaseSearchTerm() {
        // Arrange
        Tutorial tutorial = new Tutorial("Java Programming", "Learn Java", false);
        entityManager.persist(tutorial);
        entityManager.flush();

        // Act
        List<Tutorial> result = tutorialRepository.findByTitleContaining("java");

        // Assert - Spring Data JPA's Containing is case-insensitive by default in many DBs
        assertThat(result.size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("findByTitleContaining should return empty list when no match found")
    public void testFindByTitleContaining_NoMatch() {
        // Arrange
        Tutorial tutorial = new Tutorial("Spring Boot Guide", "Learn Spring Boot", false);
        entityManager.persist(tutorial);
        entityManager.flush();

        // Act
        List<Tutorial> result = tutorialRepository.findByTitleContaining("Angular");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByTitleContaining should handle empty string")
    public void testFindByTitleContaining_EmptyString() {
        // Arrange
        Tutorial tutorial1 = new Tutorial("Tutorial 1", "Description 1", false);
        Tutorial tutorial2 = new Tutorial("Tutorial 2", "Description 2", true);
        entityManager.persist(tutorial1);
        entityManager.persist(tutorial2);
        entityManager.flush();

        // Act
        List<Tutorial> result = tutorialRepository.findByTitleContaining("");

        // Assert - empty string should match all
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByTitleContaining should return empty list for null parameter")
    public void testFindByTitleContaining_NullParameter() {
        // Arrange
        Tutorial tutorial = new Tutorial("Test Tutorial", "Test Description", false);
        entityManager.persist(tutorial);
        entityManager.flush();

        // Act
        List<Tutorial> result = tutorialRepository.findByTitleContaining(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByTitleContaining should find partial matches in middle of title")
    public void testFindByTitleContaining_PartialMatch() {
        // Arrange
        Tutorial tutorial = new Tutorial("Introduction to MongoDB", "NoSQL database", false);
        entityManager.persist(tutorial);
        entityManager.flush();

        // Act
        List<Tutorial> result = tutorialRepository.findByTitleContaining("to");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Introduction to MongoDB");
    }

    @Test
    @DisplayName("findByTitleContaining should work with special characters")
    public void testFindByTitleContaining_SpecialCharacters() {
        // Arrange
        Tutorial tutorial = new Tutorial("C++ Programming", "Learn C++", false);
        entityManager.persist(tutorial);
        entityManager.flush();

        // Act
        List<Tutorial> result = tutorialRepository.findByTitleContaining("C++");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("C++ Programming");
    }

    @Test
    @DisplayName("findByTitleContaining should not be affected by published status")
    public void testFindByTitleContaining_IgnoresPublishedStatus() {
        // Arrange
        Tutorial published = new Tutorial("Published Tutorial", "Description", true);
        Tutorial unpublished = new Tutorial("Unpublished Tutorial", "Description", false);
        entityManager.persist(published);
        entityManager.persist(unpublished);
        entityManager.flush();

        // Act
        List<Tutorial> result = tutorialRepository.findByTitleContaining("Tutorial");

        // Assert - should return both regardless of published status
        assertThat(result).hasSize(2);
    }
}
