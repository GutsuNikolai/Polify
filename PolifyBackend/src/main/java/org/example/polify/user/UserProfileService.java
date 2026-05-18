package org.example.polify.user;

import java.util.Optional;
import org.example.polify.auth.DuplicateFieldException;
import org.example.polify.common.log.AuditLogger;
import org.example.polify.user.dto.UpdateUserProfileRequest;
import org.example.polify.user.dto.UserProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {
    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(long userId, UpdateUserProfileRequest req) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        String email = normalizeOptional(req.getEmail());
        if (email != null) {
            Optional<UserEntity> existing = userRepository.findByEmail(email);
            if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
                throw new DuplicateFieldException("email");
            }
        }

        user.setEmail(email);
        user.setFullName(normalizeOptional(req.getFullName()));
        user.setGender(req.getGender());
        user.setBirthDate(req.getBirthDate());
        user.setCountry(normalizeOptional(req.getCountry()));
        user.setCity(normalizeOptional(req.getCity()));

        UserEntity saved = userRepository.save(user);
        AuditLogger.info("PROFILE_UPDATED", "User profile updated", saved.getId(), null, null, null, null, "UPDATED");
        return toResponse(saved);
    }

    private static String normalizeOptional(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static UserProfileResponse toResponse(UserEntity u) {
        return new UserProfileResponse(
            u.getId(),
            u.getLogin(),
            u.getEmail(),
            u.getPhoneNumber(),
            u.getFullName(),
            u.getGender(),
            u.getBirthDate(),
            u.getCountry(),
            u.getCity(),
            u.getRole(),
            u.isVerified(),
            u.getLastActiveAt(),
            u.getCreatedAt()
        );
    }
}
