package de.tum.cit.aet.usermanagement.service;

import de.tum.cit.aet.core.config.StaffPlanProperties;
import de.tum.cit.aet.usermanagement.domain.User;
import de.tum.cit.aet.usermanagement.domain.UserGroup;
import de.tum.cit.aet.usermanagement.domain.key.UserGroupId;
import de.tum.cit.aet.usermanagement.repository.UserGroupRepository;
import de.tum.cit.aet.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final StaffPlanProperties staffPlanProperties;

    @Autowired
    public AuthenticationService(UserRepository userRepository, UserGroupRepository userGroupRepository, StaffPlanProperties staffPlanProperties) {
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.staffPlanProperties = staffPlanProperties;
    }

    /**
     * Gets the authenticated user from JWT token, syncing roles from Keycloak.
     *
     * @param jwt the JWT authentication token
     * @return the authenticated user
     */
    @Transactional
    public User getAuthenticatedUser(JwtAuthenticationToken jwt) {
        // Always update to sync roles from JWT
        return updateAuthenticatedUser(jwt);
    }

    /**
     * Gets the authenticated user with research group eagerly loaded.
     *
     * @param jwt the JWT authentication token
     * @return the authenticated user with research group
     */
    @Transactional
    public User getAuthenticatedUserWithResearchGroup(JwtAuthenticationToken jwt) {
        // Always update to sync roles from JWT
        User user = updateAuthenticatedUser(jwt);
        // Re-fetch with research group to ensure it's loaded
        return userRepository.findByUniversityIdWithResearchGroup(user.getUniversityId())
                .orElse(user);
    }

    /**
     * Creates the authenticated user from JWT token data on first login.
     * Updates basic profile info but preserves existing roles.
     *
     * @param jwt the JWT authentication token
     * @return the created or existing user
     */
    @Transactional
    public User updateAuthenticatedUser(JwtAuthenticationToken jwt) {
        Map<String, Object> attributes = jwt.getTokenAttributes();
        String universityId = getUniversityId(jwt);

        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");

        User user = userRepository.findByUniversityId(universityId).orElseGet(() -> {
            User newUser = new User();
            Instant currentTime = Instant.now();

            newUser.setJoinedAt(currentTime);
            newUser.setUpdatedAt(currentTime);

            return newUser;
        });

        user.setUniversityId(universityId);

        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }

        if (firstName != null && !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            user.setLastName(lastName);
        }

        user = userRepository.save(user);

        // Sync roles from JWT token
        syncRolesFromJwt(user, jwt);

        return user;
    }

    /**
     * Syncs user roles from the JWT token to the database.
     * Extracts roles from both realm_access and resource_access (client roles).
     */
    private void syncRolesFromJwt(User user, JwtAuthenticationToken jwt) {
        Map<String, Object> attributes = jwt.getTokenAttributes();
        Set<String> jwtRoles = new HashSet<>();

        // Extract realm roles
        @SuppressWarnings("unchecked")
        Map<String, Object> realmAccess = (Map<String, Object>) attributes.get("realm_access");
        if (realmAccess != null) {
            @SuppressWarnings("unchecked")
            java.util.List<String> roles = (java.util.List<String>) realmAccess.get("roles");
            if (roles != null) {
                jwtRoles.addAll(roles);
            }
        }

        // Extract client roles for staffplan-client
        @SuppressWarnings("unchecked")
        Map<String, Object> resourceAccess = (Map<String, Object>) attributes.get("resource_access");
        if (resourceAccess != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(staffPlanProperties.getKeycloak().getClientId());
            if (clientAccess != null) {
                @SuppressWarnings("unchecked")
                java.util.List<String> clientRoles = (java.util.List<String>) clientAccess.get("roles");
                if (clientRoles != null) {
                    jwtRoles.addAll(clientRoles);
                }
            }
        }

        // Filter to only known application roles
        Set<String> knownRoles = Set.of("admin", "job_manager", "professor", "employee");
        Set<String> rolesToSync = new HashSet<>();
        for (String role : jwtRoles) {
            if (knownRoles.contains(role)) {
                rolesToSync.add(role);
            }
        }

        // Get current roles from database
        Set<String> currentRoles = new HashSet<>();
        for (UserGroup group : user.getGroups()) {
            currentRoles.add(group.getId().getRole());
        }

        // Add new roles
        for (String role : rolesToSync) {
            if (!currentRoles.contains(role)) {
                UserGroup newGroup = new UserGroup();
                UserGroupId groupId = new UserGroupId();
                groupId.setUserId(user.getId());
                groupId.setRole(role);
                newGroup.setUser(user);
                newGroup.setId(groupId);
                userGroupRepository.save(newGroup);
                user.getGroups().add(newGroup);
            }
        }

        // Remove roles that are no longer in JWT
        Set<UserGroup> groupsToRemove = new HashSet<>();
        for (UserGroup group : user.getGroups()) {
            if (!rolesToSync.contains(group.getId().getRole())) {
                groupsToRemove.add(group);
            }
        }
        for (UserGroup group : groupsToRemove) {
            user.getGroups().remove(group);
            userGroupRepository.delete(group);
        }
    }

    private String getUniversityId(JwtAuthenticationToken jwt) {
        return jwt.getName();
    }
}
