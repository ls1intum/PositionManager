package de.tum.cit.aet.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * DTO for creating a new user.
 *
 * @param universityId the university ID (required)
 * @param email the email address
 * @param firstName the first name (required)
 * @param lastName the last name (required)
 * @param roles the list of roles to assign
 */
public record CreateUserDTO(
        @NotBlank String universityId,
        String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        List<String> roles
) {
}
