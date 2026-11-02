package com.forgery.detector.controller;

import com.forgery.detector.entity.UserEntity;
import com.forgery.detector.repository.UserRepository;
import com.forgery.detector.service.audit.HashChainAuditService;
import com.forgery.detector.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private HashChainAuditService auditService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password are required.");
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username already registered.");
        }

        UserEntity user = new UserEntity(username, passwordEncoder.encode(password), "ROLE_ANALYST");
        userRepository.save(user);

        auditService.recordEvent("USER_REGISTERED", null, "New forensic analyst registered: " + username);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "User registered successfully");
        resp.put("username", username);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());
        auditService.recordEvent("USER_LOGIN", null, "Analyst logged in: " + username);

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("username", user.getUsername());
        resp.put("role", user.getRole());
        return ResponseEntity.ok(resp);
    }
}

