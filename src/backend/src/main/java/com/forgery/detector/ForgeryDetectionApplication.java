package com.forgery.detector;

import com.forgery.detector.entity.UserEntity;
import com.forgery.detector.repository.UserRepository;
import com.forgery.detector.service.audit.HashChainAuditService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Main Spring Boot Application Entrypoint.
 * AI-Based Image Forgery Detection & Authentication System.
 * 
 * PAMCA502 Java Programming Project Review
 * Team Members: Abdul Arman Rayeen (26MCA0238), Harsh Gupta (26MCA0211)
 * Guide: Dr. John Singh K. | SCORE
 */
@SpringBootApplication
public class ForgeryDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForgeryDetectionApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          PasswordEncoder passwordEncoder,
                                          HashChainAuditService auditService) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                UserEntity admin = new UserEntity("admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN");
                userRepository.save(admin);
                auditService.recordEvent("SYSTEM_INIT", null, "Application initialized. Root admin user configured.");
            }
        };
    }
}

