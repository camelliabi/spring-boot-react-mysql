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
 * Unit tests for TutorialRepository
 * 
 * Tests validate fixes for:
 * - ERR-001: Method signature conflict resolution (findByTitleAndPublished renamed)
 * - Proper query method behavior for all repository methods
 */
@DataJpaTest
public class TutorialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TutorialRepository tutorialRepository;

    private Tutorial publishedTutorial1;
    private Tutorial publishedTutorial2;
    private Tutorial unpublishedTutorial1;
    private Tutorial unpublishedTutorial2;

    @BeforeEach
    public void setUp() {
        publishedTutorial1 = new Tutorial("Spring Boot Tutorial", "Learn Spring Boot basics", true);
        publishedTutorial2 = new Tutorial("Spring Data JPA", "Master JPA with Spring", true);
        unpublishedTutorial1 = new Tutorial("Draft Tutorial", "Work in progress", false);
        unpublishedTutorial2 = new Tutorial("Spring Security", "Security guide draft", false);

        entityManager.persist(publishedTutorial1);
        entityManager.persist(publishedTutorial2);
        entityManager.persist(unpublishedTutorial1);
        entityManager.persist(unpublishedTutorial2);
        entityManager.flush();
    }

    @Test
    public void testFindByPublished_ReturnsOnlyPublishedTutorials() {
        List<Tutorial> result = tutorialRepository.findByPublished(true);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Tutorial::isPublished).containsOnly(true);
    }

    @Test
    public void testFindByTitleAndPublished_ExactTitleAndPublishedTrue() {
        List<Tutorial> result = tutorialRepository.findByTitleAndPublished("Spring Boot Tutorial", true);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Spring Boot Tutorial");
        assertThat(result.get(0).isPublished()).isTrue();
    }

    @Test
    public void testBothMethodsWorkIndependently() {
        List<Tutorial> substringResults = tutorialRepository.findByTitleContaining("Spring");
        assertThat(substringResults).hasSizeGreaterThanOrEqualTo(3);
        
        List<Tutorial> exactResults = tutorialRepository.findByTitleAndPublished("Spring Boot Tutorial", true);
        assertThat(exactResults).hasSize(1);
    }
}
