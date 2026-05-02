package org.example.polify.auth;

import java.time.Instant;
import org.example.polify.auth.dto.AuthResponse;
import org.example.polify.auth.dto.LoginRequest;
import org.example.polify.auth.dto.RegisterRequest;
import org.example.polify.user.UserEntity;
import org.example.polify.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.polify.common.log.SecurityLogger;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            SecurityLogger.warn("REGISTER_FAILED", "Registration failed: login already exists", null, request.getEmail(), "FAILED");
            throw new DuplicateFieldException("login");
        }
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            SecurityLogger.warn("REGISTER_FAILED", "Registration failed: phone already exists", null, request.getEmail(), "FAILED");
            throw new DuplicateFieldException("phoneNumber");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                SecurityLogger.warn("REGISTER_FAILED", "Registration failed: email already exists", null, request.getEmail(), "FAILED");
                throw new DuplicateFieldException("email");
            }
        }

        Instant now = Instant.now();

        UserEntity user = new UserEntity();
        user.setLogin(request.getLogin());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(normalizeOptional(request.getEmail()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setVerified(false);
        user.setLastActiveAt(now);
        user.setCreatedAt(now);

        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            // Race condition fallback: DB unique constraints are source of truth.
            SecurityLogger.warn("REGISTER_FAILED", "Registration failed: unique constraint", null, request.getEmail(), "FAILED");
            throw new DuplicateFieldException("unique");
        }

        SecurityLogger.info("REGISTER_SUCCESS", "User registered", user.getId(), user.getEmail(), "SUCCESS");
        return new AuthResponse(user.getId(), jwtService.issueAccessToken(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByLogin(request.getLogin())
            .orElseThrow(() -> {
                SecurityLogger.warn("LOGIN_FAILED", "Login failed: invalid credentials", null, null, "FAILED");
                return new InvalidCredentialsException();
            });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            SecurityLogger.warn("LOGIN_FAILED", "Login failed: invalid credentials", user.getId(), user.getEmail(), "FAILED");
            throw new InvalidCredentialsException();
        }

        user.setLastActiveAt(Instant.now());
        userRepository.save(user);

        SecurityLogger.info("LOGIN_SUCCESS", "Login success", user.getId(), user.getEmail(), "SUCCESS");
        return new AuthResponse(user.getId(), jwtService.issueAccessToken(user));
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
