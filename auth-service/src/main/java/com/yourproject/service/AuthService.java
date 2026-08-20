package com.yourproject.service;

import com.yourproject.config.JwtService;
import com.yourproject.dto.LoginRequest;
import com.yourproject.dto.LoginResponse;
import com.yourproject.entity.User;
import com.yourproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByusername(request.getusername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                user.getusername(),
                user.getRole().name(),
                "Login successful",
                token
        );
    }
}
