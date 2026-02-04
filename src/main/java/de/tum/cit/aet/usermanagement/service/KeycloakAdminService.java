package de.tum.cit.aet.usermanagement.service;

import de.tum.cit.aet.core.config.KeycloakServiceConfig;
import de.tum.cit.aet.usermanagement.dto.KeycloakUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for interacting with Keycloak Admin API to search users in LDAP.
 * Uses a service account client with client credentials grant for authentication.
 */
@Slf4j
@Service
public class KeycloakAdminService {

    private final KeycloakServiceConfig serviceConfig;
    private final String issuerUri;

    public KeycloakAdminService(
            KeycloakServiceConfig serviceConfig,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        this.serviceConfig = serviceConfig;
        this.issuerUri = issuerUri;
    }

    /**
     * Searches users by email address (supports multiple email addresses in LDAP).
     *
     * @param email the email to search for
     * @return list of matching users (empty if none found or service not configured)
     */
    public List<KeycloakUserDTO> searchByEmail(String email) {
        if (!isConfigured()) {
            log.warn("Keycloak service client secret not configured, skipping search");
            return List.of();
        }

        try (Keycloak keycloak = getKeycloakInstance()) {
            String realm = extractRealm(issuerUri);
            return keycloak.realm(realm)
                    .users()
                    .searchByEmail(email, true) // exact match
                    .stream()
                    .map(this::toDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to search Keycloak by email: {}", email, e);
            return List.of();
        }
    }

    /**
     * Searches users by first and last name.
     *
     * @param firstName the first name to search for
     * @param lastName  the last name to search for
     * @return list of matching users (should ideally be 0 or 1)
     */
    public List<KeycloakUserDTO> searchByName(String firstName, String lastName) {
        if (!isConfigured()) {
            log.warn("Keycloak service client secret not configured, skipping search");
            return List.of();
        }

        try (Keycloak keycloak = getKeycloakInstance()) {
            String realm = extractRealm(issuerUri);
            return keycloak.realm(realm)
                    .users()
                    .search(null, firstName, lastName, null, null, null)
                    .stream()
                    .map(this::toDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to search Keycloak by name: {} {}", firstName, lastName, e);
            return List.of();
        }
    }

    /**
     * Checks if the Keycloak admin service is properly configured.
     *
     * @return true if the service client secret is configured
     */
    public boolean isConfigured() {
        return serviceConfig.getClientSecret() != null && !serviceConfig.getClientSecret().isBlank();
    }

    private Keycloak getKeycloakInstance() {
        String serverUrl = extractServerUrl(issuerUri);
        String realm = extractRealm(issuerUri);
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(serviceConfig.getClientId())
                .clientSecret(serviceConfig.getClientSecret())
                .build();
    }

    private String extractServerUrl(String issuerUri) {
        // http://localhost:8081/realms/staffplan -> http://localhost:8081
        return issuerUri.replaceAll("/realms/.*$", "");
    }

    private String extractRealm(String issuerUri) {
        // http://localhost:8081/realms/staffplan -> staffplan
        return issuerUri.replaceAll("^.*/realms/", "");
    }

    private KeycloakUserDTO toDTO(UserRepresentation user) {
        return new KeycloakUserDTO(
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
