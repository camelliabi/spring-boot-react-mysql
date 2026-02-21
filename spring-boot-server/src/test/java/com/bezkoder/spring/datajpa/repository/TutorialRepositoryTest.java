package com.bezkoder.spring.datajpa.repository;

import com.bezkoder.spring.datajpa.model.Tutorial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TutorialRepository
 * Tests the bug fixes for:
 * - ERROR-001: Method signature conflict and query pattern
 * - Validates findByPublished, findByTitleContaining, and findByTitleContainingAndPublished methods
 */
@DataJpaTest
class TutorialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TutorialRepository tutorialRepository;

    private Tutorial publishedTutorial1;
    private Tutorial publishedTutorial2;
    private Tutorial unpublishedTutorial;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        tutorialRepository.deleteAll();
        
        // Create test data
        publishedTutorial1 = new Tutorial("Spring Boot Tutorial", "Learn Spring Boot basics", true);
        publishedTutorial2 = new Tutorial("Spring Data JPA Guide", "Master Spring Data JPA", true);
        unpublishedTutorial = new Tutorial("Draft Tutorial", "Work in progress", false);

        entityManager.persist(publishedTutorial1);
        entityManager.persist(publishedTutorial2);
        entityManager.persist(unpublishedTutorial);
        entityManager.flush();
    }

    /**
     * TEST: Validates ERROR-003 fix - findByPublished should correctly filter by published status
     */
    @Test
    void testFindByPublished_ReturnsOnlyPublishedTutorials() {
        // When: Finding published tutorials
        List<Tutorial> publishedTutorials = tutorialRepository.findByPublished(true);

        // Then: Should return only published tutorials
        assertThat(publishedTutorials).hasSize(2);
        assertThat(publishedTutorials)
                .extracting(Tutorial::isPublished)
                .containsOnly(true);
        assertThat(publishedTutorials)
                .extracting(Tutorial::getTitle)
                .containsExactlyInAnyOrder("Spring Boot Tutorial", "Spring Data JPA Guide");
    }

    @Test
    void testFindByPublished_ReturnsOnlyUnpublishedTutorials() {
        // When: Finding unpublished tutorials
        List<Tutorial> unpublishedTutorials = tutorialRepository.findByPublished(false);

        // Then: Should return only unpublished tutorials
        assertThat(unpublishedTutorials).hasSize(1);
        assertThat(unpublishedTutorials.get(0).isPublished()).isFalse();
        assertThat(unpublishedTutorials.get(0).getTitle()).isEqualTo("Draft Tutorial");
    }

    /**
     * TEST: Validates ERROR-001 fix - findByTitleContaining should perform partial match
     */
    @Test
    void testFindByTitleContaining_ReturnsMatchingTutorials() {
        // When: Searching for "Spring"
        List<Tutorial> springTutorials = tutorialRepository.findByTitleContaining("Spring");

        // Then: Should return both Spring tutorials
        assertThat(springTutorials).hasSize(2);
        assertThat(springTutorials)
                .extracting(Tutorial::getTitle)
                .allMatch(title -> title.contains("Spring"));
    }

    /**
     * TEST: Validates ERROR-001 fix - Custom query with LIKE pattern and published filter
     */
    @Test
    void testFindByTitleContainingAndPublished_BothConditions() {
        // When: Searching for published tutorials containing "Spring"
        List<Tutorial> tutorials = tutorialRepository.findByTitleContainingAndPublished("Spring", true);

        // Then: Should return only published Spring tutorials
        assertThat(tutorials).hasSize(2);
        assertThat(tutorials).allMatch(t -> t.isPublished() && t.getTitle().contains("Spring"));
    }
}
