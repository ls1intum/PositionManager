package de.tum.cit.aet.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Keycloak service client used for admin operations
 * such as searching users in LDAP via the Keycloak Admin API.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "staffplan.keycloak.service")
public class KeycloakServiceConfig {

    /**
     * The client ID for the service account client.
     */
    private String clientId;

    /**
     * The client secret for the service account client.
     */
    private String clientSecret;
}
