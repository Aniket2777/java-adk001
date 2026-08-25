package com.yourproject.config;

import com.yourproject.entity.Role;
import com.yourproject.entity.User;
import com.yourproject.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a few demo users on startup so /api/auth/login has something to authenticate against.
 * Passwords are hashed with the real PasswordEncoder bean (never store plain text),
 * which is why this happens in code rather than in a data.sql with a hardcoded hash.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seed("prachi@gmail.com", "password123", Role.EMPLOYEE);
        seed("rahul@gmail.com", "password123", Role.MANAGER);
        seed("hr@gmail.com", "password123", Role.HR);
        seed("admin@gmail.com", "password123", Role.ADMIN);
    }

    private void seed(String username, String rawPassword, Role role) {
        userRepository.findByusername(username).ifPresentOrElse(
                u -> { /* already exists, skip */ },
                () -> userRepository.save(
                        new User(username, passwordEncoder.encode(rawPassword), role))
        );
    }
}
