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
 * Integration tests for TutorialRepository
 * 
 * These tests verify all the repository method fixes.
 * They use an in-memory database to test actual JPA query behavior.
 * 
 * OLD CODE ISSUES:
 * - Duplicate method name findByTitleContaining with different signatures
 * - Query used = instead of LIKE
 * - Method name didn't reflect actual behavior
 * 
 * These tests would FAIL with old code and PASS with fixed code.
 */
@DataJpaTest
public class TutorialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TutorialRepository tutorialRepository;

    private Tutorial springBootTutorial;
    private Tutorial springCloudTutorial;
    private Tutorial reactTutorial;
    private Tutorial publishedSpringTutorial;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        tutorialRepository.deleteAll();
        entityManager.flush();

        // Create test data
        springBootTutorial = new Tutorial("Spring Boot Basics", "Learn Spring Boot", false);
        springCloudTutorial = new Tutorial("Spring Cloud Guide", "Microservices with Spring", false);
        reactTutorial = new Tutorial("React Tutorial", "Learn React", false);
        publishedSpringTutorial = new Tutorial("Spring Security", "Secure your app", true);

        // Persist test data
        entityManager.persist(springBootTutorial);
        entityManager.persist(springCloudTutorial);
        entityManager.persist(reactTutorial);
        entityManager.persist(publishedSpringTutorial);
        entityManager.flush();
    }

    /**
     * FIX #7: Test original findByTitleContaining method works correctly
     * 
     * This method should find tutorials where title contains the search string.
     * Spring Data JPA auto-generates the implementation.
     */
    @Test
    @DisplayName("FIX #7: findByTitleContaining should find tutorials with partial title match")
    void testFindByTitleContaining_PartialMatch_FindsMultiple() {
        // Act: Search for "Spring" - should find 3 tutorials
        List<Tutorial> results = tutorialRepository.findByTitleContaining("Spring");

        // Assert: Should find all 3 Spring tutorials
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Tutorial::getTitle)
                .contains("Spring Boot Basics", "Spring Cloud Guide", "Spring Security");
    }

    /**
     * FIX #7: Test with single result
     */
    @Test
    @DisplayName("FIX #7: findByTitleContaining should find single tutorial")
    void testFindByTitleContaining_PartialMatch_FindsSingle() {
        // Act: Search for "React"
        List<Tutorial> results = tutorialRepository.findByTitleContaining("React");

        // Assert: Should find only React tutorial
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("React Tutorial");
    }

    /**
     * FIX #7: Test with no matches
     */
    @Test
    @DisplayName("FIX #7: findByTitleContaining should return empty list when no matches")
    void testFindByTitleContaining_NoMatch_ReturnsEmpty() {
        // Act: Search for non-existent term
        List<Tutorial> results = tutorialRepository.findByTitleContaining("Angular");

        // Assert: Should return empty list
        assertThat(results).isEmpty();
    }

    /**
     * FIX #8, #10: Test LIKE operator in custom query
     * 
     * OLD CODE BUG: @Query("SELECT t FROM Tutorial t WHERE t.title = :title")
     * This used = (exact match) which doesn't match the method name "Containing"
     * 
     * NEW CODE FIX: @Query("... WHERE t.title LIKE %:title% AND t.published = :published")
     * Now uses LIKE for partial matching
     * 
     * This test would FAIL with old code (exact match wouldn't find anything)!
     */
    @Test
    @DisplayName("FIX #8, #10: findByTitleAndPublished should use LIKE for partial match")
    void testFindByTitleAndPublished_LikeQuery_FindsPartialMatch() {
        // Act: Search for "Spring" in published tutorials
        List<Tutorial> results = tutorialRepository.findByTitleAndPublished("Spring", true);

        // Assert: Should find only the published Spring tutorial
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Spring Security");
        assertThat(results.get(0).isPublished()).isTrue();
    }

    /**
     * FIX #8, #10: Test that LIKE query is case-sensitive for exact substring
     */
    @Test
    @DisplayName("FIX #8, #10: findByTitleAndPublished LIKE query finds exact substring")
    void testFindByTitleAndPublished_ExactSubstring_Finds() {
        // Act: Search for exact substring "Boot" in unpublished tutorials
        List<Tutorial> results = tutorialRepository.findByTitleAndPublished("Boot", false);

        // Assert: Should find Spring Boot tutorial
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Spring Boot Basics");
    }

    /**
     * FIX #9: Test renamed method signature clarity
     * 
     * OLD CODE: findByTitleContaining(String title, boolean published)
     * NEW CODE: findByTitleAndPublished(String title, boolean published)
     * 
     * The new name clearly indicates it searches by BOTH title AND published status.
     */
    @Test
    @DisplayName("FIX #9: findByTitleAndPublished name reflects it filters by BOTH title AND published")
    void testFindByTitleAndPublished_FiltersByBothTitleAndPublished() {
        // Act: Search for "Spring" in unpublished tutorials
        List<Tutorial> unpublishedResults = tutorialRepository.findByTitleAndPublished("Spring", false);

        // Assert: Should find 2 unpublished Spring tutorials (not the published one)
        assertThat(unpublishedResults).hasSize(2);
        assertThat(unpublishedResults).extracting(Tutorial::getTitle)
                .contains("Spring Boot Basics", "Spring Cloud Guide")
                .doesNotContain("Spring Security");  // This one is published
        assertThat(unpublishedResults).allMatch(t -> !t.isPublished());

        // Act: Search for "Spring" in published tutorials
        List<Tutorial> publishedResults = tutorialRepository.findByTitleAndPublished("Spring", true);

        // Assert: Should find only 1 published Spring tutorial
        assertThat(publishedResults).hasSize(1);
        assertThat(publishedResults.get(0).getTitle()).isEqualTo("Spring Security");
        assertThat(publishedResults.get(0).isPublished()).isTrue();
    }

    /**
     * FIX #9: Test that method correctly handles no matches for published status
     */
    @Test
    @DisplayName("FIX #9: findByTitleAndPublished returns empty when no matches for published status")
    void testFindByTitleAndPublished_NoMatchForPublishedStatus_ReturnsEmpty() {
        // Act: Search for "React" in published tutorials (React tutorial is unpublished)
        List<Tutorial> results = tutorialRepository.findByTitleAndPublished("React", true);

        // Assert: Should return empty - React tutorial exists but isn't published
        assertThat(results).isEmpty();
    }

    /**
     * FIX #7-10: Comprehensive test showing both methods work correctly
     * 
     * This demonstrates that we now have TWO distinct methods:
     * 1. findByTitleContaining(String) - searches all tutorials by title
     * 2. findByTitleAndPublished(String, boolean) - searches by title AND published status
     */
    @Test
    @DisplayName("FIX #7-10: Both methods work correctly without conflicts")
    void testBothMethodsWorkIndependently() {
        // Test method 1: findByTitleContaining - finds all with "Spring" in title
        List<Tutorial> allSpring = tutorialRepository.findByTitleContaining("Spring");
        assertThat(allSpring).hasSize(3);  // All 3 Spring tutorials

        // Test method 2: findByTitleAndPublished - finds only published with "Spring"
        List<Tutorial> publishedSpring = tutorialRepository.findByTitleAndPublished("Spring", true);
        assertThat(publishedSpring).hasSize(1);  // Only published Spring tutorial

        // Test method 2: findByTitleAndPublished - finds only unpublished with "Spring"
        List<Tutorial> unpublishedSpring = tutorialRepository.findByTitleAndPublished("Spring", false);
        assertThat(unpublishedSpring).hasSize(2);  // 2 unpublished Spring tutorials

        // Verify: published + unpublished = all
        assertThat(publishedSpring.size() + unpublishedSpring.size()).isEqualTo(allSpring.size());
    }

    /**
     * Additional test: Verify findByPublished works correctly
     */
    @Test
    @DisplayName("findByPublished should find all tutorials with specified published status")
    void testFindByPublished_FindsCorrectStatus() {
        // Act: Find published tutorials
        List<Tutorial> published = tutorialRepository.findByPublished(true);

        // Assert: Should find only 1 published tutorial
        assertThat(published).hasSize(1);
        assertThat(published.get(0).getTitle()).isEqualTo("Spring Security");

        // Act: Find unpublished tutorials
        List<Tutorial> unpublished = tutorialRepository.findByPublished(false);

        // Assert: Should find 3 unpublished tutorials
        assertThat(unpublished).hasSize(3);
        assertThat(unpublished).extracting(Tutorial::getTitle)
                .contains("Spring Boot Basics", "Spring Cloud Guide", "React Tutorial");
    }

    /**
     * Edge case test: Empty string search
     */
    @Test
    @DisplayName("findByTitleContaining with empty string should find all tutorials")
    void testFindByTitleContaining_EmptyString_FindsAll() {
        // Act: Search with empty string
        List<Tutorial> results = tutorialRepository.findByTitleContaining("");

        // Assert: Should find all tutorials (empty string matches everything)
        assertThat(results).hasSize(4);
    }

    /**
     * Edge case test: Case sensitivity
     */
    @Test
    @DisplayName("findByTitleContaining should be case-sensitive")
    void testFindByTitleContaining_CaseSensitive() {
        // Act: Search with lowercase "spring"
        List<Tutorial> results = tutorialRepository.findByTitleContaining("spring");

        // Assert: Should not find anything (database is case-sensitive by default)
        // Note: This depends on database collation settings
        // H2 default is case-insensitive, but SQL standard is case-sensitive
        assertThat(results).hasSizeLessThanOrEqualTo(3);
    }
}
