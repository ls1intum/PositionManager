package de.tum.cit.aet.usermanagement.dto;

import de.tum.cit.aet.usermanagement.domain.User;

import java.util.UUID;

/**
 * A lightweight DTO for user summary information, used when embedding user data
 * in other DTOs (e.g., research group head).
 */
public record UserSummaryDTO(
        UUID id,
        String universityId,
        String firstName,
        String lastName,
        String email
) {
    /**
     * Creates a UserSummaryDTO from a User entity.
     */
    public static UserSummaryDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDTO(
                user.getId(),
                user.getUniversityId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }
}
