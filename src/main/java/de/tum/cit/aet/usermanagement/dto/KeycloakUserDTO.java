package de.tum.cit.aet.usermanagement.dto;

/**
 * DTO representing a user from Keycloak/LDAP lookup.
 *
 * @param username     the universityId (login) like "ne23kow"
 * @param email        the primary email address
 * @param firstName    the user's first name
 * @param lastName     the user's last name
 */
public record KeycloakUserDTO(
        String username,
        String email,
        String firstName,
        String lastName
) {
}
