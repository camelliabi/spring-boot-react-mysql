package com.bezkoder.spring.datajpa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * FIX ERR-006: Configurable CORS configuration
 * Moved from hardcoded @CrossOrigin annotation to centralized config
 * Allows different origins per environment without code changes
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // Read CORS origins from application.properties
    // Supports multiple origins separated by commas
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
