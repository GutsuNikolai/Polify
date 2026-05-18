package org.example.polify.user;

import org.example.polify.common.error.ForbiddenOperationException;
import org.springframework.stereotype.Service;

@Service
public class UserRoleGuard {
    private final UserRepository userRepository;

    public UserRoleGuard(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void requireModerator(long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        Role role = user.getRole();
        if (role != Role.MODERATOR && role != Role.ADMIN) {
            throw new ForbiddenOperationException("Moderator role required");
        }
    }
}
