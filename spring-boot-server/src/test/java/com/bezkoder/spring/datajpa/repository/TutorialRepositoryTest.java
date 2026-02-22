package com.bezkoder.spring.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.bezkoder.spring.datajpa.model.Tutorial;

/**
 * Comprehensive test suite for TutorialRepository
 * Tests FIX #3: Spring Data JPA method naming convention
 */
@DataJpaTest
@DisplayName("TutorialRepository Tests")
class TutorialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TutorialRepository tutorialRepository;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        tutorialRepository.deleteAll();
    }

    // ========== FIX #3: findByTitleContainingAndPublished tests ==========

    @Test
    @DisplayName("FIX #3: findByTitleContainingAndPublished should find matching published tutorials")
    void testFindByTitleContainingAndPublished_MatchingPublished() {
        // Given: Tutorials with various titles and published states
        Tutorial published1 = new Tutorial("Spring Boot Tutorial", "Learn Spring Boot", true);
        Tutorial published2 = new Tutorial("Spring Data JPA", "Learn JPA", true);
        Tutorial unpublished = new Tutorial("Spring Security", "Learn Security", false);

        entityManager.persist(published1);
        entityManager.persist(published2);
        entityManager.persist(unpublished);
        entityManager.flush();

        // When: Search for "Spring" with published=true
        List<Tutorial> results = tutorialRepository.findByTitleContainingAndPublished("Spring", true);

        // Then: Should return only published tutorials containing "Spring"
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Tutorial::getTitle)
                .containsExactlyInAnyOrder("Spring Boot Tutorial", "Spring Data JPA");
        assertThat(results).allMatch(Tutorial::isPublished);
    }

    @Test
    @DisplayName("FIX #3: findByTitleContainingAndPublished should find matching unpublished tutorials")
    void testFindByTitleContainingAndPublished_MatchingUnpublished() {
        // Given: Mix of published and unpublished tutorials
        Tutorial published = new Tutorial("React Tutorial", "Learn React", true);
        Tutorial unpublished1 = new Tutorial("React Hooks", "Learn Hooks", false);
        Tutorial unpublished2 = new Tutorial("React Router", "Learn Router", false);

        entityManager.persist(published);
        entityManager.persist(unpublished1);
        entityManager.persist(unpublished2);
        entityManager.flush();

        // When: Search for "React" with published=false
        List<Tutorial> results = tutorialRepository.findByTitleContainingAndPublished("React", false);

        // Then: Should return only unpublished tutorials containing "React"
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Tutorial::getTitle)
                .containsExactlyInAnyOrder("React Hooks", "React Router");
        assertThat(results).noneMatch(Tutorial::isPublished);
    }

    @Test
    @DisplayName("FIX #3: findByTitleContainingAndPublished should return empty list when no matches")
    void testFindByTitleContainingAndPublished_NoMatches() {
        // Given: Tutorials that don't match search criteria
        Tutorial tutorial = new Tutorial("Java Tutorial", "Learn Java", true);
        entityManager.persist(tutorial);
        entityManager.flush();

        // When: Search for non-existent title
        List<Tutorial> results = tutorialRepository.findByTitleContainingAndPublished("Python", true);

        // Then: Should return empty list
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("FIX #3: findByTitleContainingAndPublished should be case-insensitive")
    void testFindByTitleContainingAndPublished_CaseInsensitive() {
        // Given: Tutorial with mixed case title
        Tutorial tutorial = new Tutorial("Spring Boot Tutorial", "Description", true);
        entityManager.persist(tutorial);
        entityManager.flush();

        // When: Search with different case
        List<Tutorial> results = tutorialRepository.findByTitleContainingAndPublished("spring", true);

        // Then: Should find the tutorial (case-insensitive)
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Spring Boot Tutorial");
    }

    @Test
    @DisplayName("FIX #3: findByTitleContainingAndPublished should handle partial matches")
    void testFindByTitleContainingAndPublished_PartialMatch() {
        // Given: Tutorial with longer title
        Tutorial tutorial = new Tutorial("Complete Spring Boot Guide", "Comprehensive guide", true);
        entityManager.persist(tutorial);
        entityManager.flush();

        // When: Search with partial title
        List<Tutorial> results = tutorialRepository.findByTitleContainingAndPublished("Boot", true);

        // Then: Should find tutorial with partial match
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("Boot");
    }

    // ========== Additional repository method tests ==========

    @Test
    @DisplayName("findByPublished should return only published tutorials")
    void testFindByPublished_OnlyPublished() {
        // Given: Mix of published and unpublished
        Tutorial published1 = new Tutorial("Tutorial 1", "Desc 1", true);
        Tutorial published2 = new Tutorial("Tutorial 2", "Desc 2", true);
        Tutorial unpublished = new Tutorial("Tutorial 3", "Desc 3", false);

        entityManager.persist(published1);
        entityManager.persist(published2);
        entityManager.persist(unpublished);
        entityManager.flush();

        // When: Find published tutorials
        List<Tutorial> results = tutorialRepository.findByPublished(true);

        // Then: Should return only published
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(Tutorial::isPublished);
    }

    @Test
    @DisplayName("findByPublished should return only unpublished tutorials")
    void testFindByPublished_OnlyUnpublished() {
        // Given: Mix of published and unpublished
        Tutorial published = new Tutorial("Tutorial 1", "Desc 1", true);
        Tutorial unpublished1 = new Tutorial("Tutorial 2", "Desc 2", false);
        Tutorial unpublished2 = new Tutorial("Tutorial 3", "Desc 3", false);

        entityManager.persist(published);
        entityManager.persist(unpublished1);
        entityManager.persist(unpublished2);
        entityManager.flush();

        // When: Find unpublished tutorials
        List<Tutorial> results = tutorialRepository.findByPublished(false);

        // Then: Should return only unpublished
        assertThat(results).hasSize(2);
        assertThat(results).noneMatch(Tutorial::isPublished);
    }

    @Test
    @DisplayName("findByTitleContaining should find tutorials with matching title substring")
    void testFindByTitleContaining_MatchingSubstring() {
        // Given: Tutorials with various titles
        Tutorial tutorial1 = new Tutorial("Spring Boot Basics", "Desc", true);
        Tutorial tutorial2 = new Tutorial("Advanced Spring", "Desc", false);
        Tutorial tutorial3 = new Tutorial("React Tutorial", "Desc", true);

        entityManager.persist(tutorial1);
        entityManager.persist(tutorial2);
        entityManager.persist(tutorial3);
        entityManager.flush();

        // When: Search by title substring
        List<Tutorial> results = tutorialRepository.findByTitleContaining("Spring");

        // Then: Should return tutorials containing "Spring"
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Tutorial::getTitle)
                .allMatch(title -> title.contains("Spring"));
    }

    @Test
    @DisplayName("findByTitleContaining should return empty list when no matches")
    void testFindByTitleContaining_NoMatches() {
        // Given: Tutorial with specific title
        Tutorial tutorial = new Tutorial("Java Tutorial", "Desc", true);
        entityManager.persist(tutorial);
        entityManager.flush();

        // When: Search for non-matching title
        List<Tutorial> results = tutorialRepository.findByTitleContaining("Python");

        // Then: Should return empty list
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Repository should save and retrieve tutorial correctly")
    void testSaveAndRetrieve() {
        // Given: New tutorial
        Tutorial tutorial = new Tutorial("Test Tutorial", "Test Description", true);

        // When: Save tutorial
        Tutorial saved = tutorialRepository.save(tutorial);

        // Then: Should be retrievable with same data
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Test Tutorial");
        assertThat(saved.getDescription()).isEqualTo("Test Description");
        assertThat(saved.isPublished()).isTrue();
    }

    @Test
    @DisplayName("Repository should update tutorial correctly")
    void testUpdate() {
        // Given: Existing tutorial
        Tutorial tutorial = new Tutorial("Original Title", "Original Desc", false);
        Tutorial saved = entityManager.persist(tutorial);
        entityManager.flush();

        // When: Update tutorial
        saved.setTitle("Updated Title");
        saved.setPublished(true);
        Tutorial updated = tutorialRepository.save(saved);

        // Then: Changes should be persisted
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.isPublished()).isTrue();
    }

    @Test
    @DisplayName("Repository should delete tutorial correctly")
    void testDelete() {
        // Given: Existing tutorial
        Tutorial tutorial = new Tutorial("To Delete", "Desc", true);
        Tutorial saved = entityManager.persist(tutorial);
        entityManager.flush();
        Long id = saved.getId();

        // When: Delete tutorial
        tutorialRepository.deleteById(id);

        // Then: Should not be found
        assertThat(tutorialRepository.findById(id)).isEmpty();
    }
}
