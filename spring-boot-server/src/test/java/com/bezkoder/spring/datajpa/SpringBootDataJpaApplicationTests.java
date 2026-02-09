package com.bezkoder.spring.datajpa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// FIX #10: Added @ActiveProfiles("test") to use test configuration
// ISSUE: Test was using main application.properties which requires MySQL
// ORIGINAL CODE: Only @SpringBootTest annotation
// PROBLEM:
//   - Tests loaded production MySQL configuration
//   - Failed when MySQL wasn't running: "CONDITIONS EVALUATION REPORT" error
//   - Required developers to run MySQL locally just to run tests
// SOLUTION: 
//   - Added @ActiveProfiles("test") annotation
//   - Spring now loads application-test.properties instead
//   - Uses H2 in-memory database (no external dependencies)
// IMPACT:
//   - Tests run independently without MySQL
//   - CI/CD pipelines work without database setup
//   - Faster test execution and better developer experience
@SpringBootTest
@ActiveProfiles("test")
class SpringBootDataJpaApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring application context loads successfully
		// With the test profile, it uses H2 database instead of MySQL
	}

}
