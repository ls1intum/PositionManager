package de.tum.cit.aet.usermanagement.service;

import de.tum.cit.aet.core.exceptions.ResourceNotFoundException;
import de.tum.cit.aet.usermanagement.domain.User;
import de.tum.cit.aet.usermanagement.domain.UserGroup;
import de.tum.cit.aet.usermanagement.domain.key.UserGroupId;
import de.tum.cit.aet.usermanagement.dto.CreateUserDTO;
import de.tum.cit.aet.usermanagement.dto.UserDTO;
import de.tum.cit.aet.usermanagement.repository.ResearchGroupRepository;
import de.tum.cit.aet.usermanagement.repository.UserGroupRepository;
import de.tum.cit.aet.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> VALID_ROLES = Set.of("admin", "job_manager", "professor", "employee");

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final ResearchGroupRepository researchGroupRepository;

    /**
     * Searches users with pagination and optional filters.
     *
     * @param search optional search term for name, email, or university ID
     * @param role optional role filter
     * @param pageable pagination parameters
     * @return page of matching users
     */
    public Page<UserDTO> searchUsers(String search, String role, Pageable pageable) {
        return userRepository.searchUsers(search, role, pageable)
                .map(UserDTO::fromEntity);
    }

    /**
     * Creates a new user.
     *
     * @param dto the user creation data
     * @return the created user DTO
     * @throws IllegalArgumentException if a user with the same university ID already exists or roles are invalid
     */
    public UserDTO createUser(CreateUserDTO dto) {
        if (userRepository.existsByUniversityId(dto.universityId())) {
            throw new IllegalArgumentException("User with university ID '" + dto.universityId() + "' already exists");
        }

        validateRoles(dto.roles());

        User user = new User();
        user.setUniversityId(dto.universityId());
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setJoinedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setGroups(new HashSet<>());
        user = userRepository.save(user);

        if (dto.roles() != null) {
            for (String role : dto.roles()) {
                UserGroup group = new UserGroup();
                UserGroupId groupId = new UserGroupId();
                groupId.setUserId(user.getId());
                groupId.setRole(role);
                group.setId(groupId);
                group.setUser(user);
                user.getGroups().add(group);
                userGroupRepository.save(group);
            }
        }

        return UserDTO.fromEntity(user);
    }

    /**
     * Deletes a user by ID, cleaning up all references.
     *
     * @param userId the user ID
     * @throws ResourceNotFoundException if the user does not exist
     */
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Clear FK references in research_groups using bulk updates
        researchGroupRepository.clearHeadByUserId(userId);
        researchGroupRepository.clearCreatedByUserId(userId);
        researchGroupRepository.clearUpdatedByUserId(userId);

        // Clear user's researchGroup reference
        userRepository.clearResearchGroup(userId);

        // Delete user's group entries using bulk delete
        userGroupRepository.deleteAllByUserId(userId);

        // Delete the user
        userRepository.deleteById(userId);
    }

    /**
     * Updates the roles for a specific user.
     *
     * @param userId the user ID
     * @param roles the new list of roles
     * @return the updated user DTO
     * @throws IllegalArgumentException if any role is invalid
     */
    public UserDTO updateUserRoles(UUID userId, List<String> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        validateRoles(roles);

        // Get current roles
        Set<String> currentRoles = user.getGroups().stream()
                .map(ug -> ug.getId().getRole())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> newRoles = new HashSet<>(roles);

        // Remove roles that are no longer needed
        for (UserGroup group : new HashSet<>(user.getGroups())) {
            if (!newRoles.contains(group.getId().getRole())) {
                user.getGroups().remove(group);
                userGroupRepository.delete(group);
            }
        }

        // Add new roles
        for (String role : newRoles) {
            if (!currentRoles.contains(role)) {
                UserGroup group = new UserGroup();
                UserGroupId groupId = new UserGroupId();
                groupId.setUserId(userId);
                groupId.setRole(role);
                group.setId(groupId);
                group.setUser(user);
                user.getGroups().add(group);
                userGroupRepository.save(group);
            }
        }

        return UserDTO.fromEntity(user);
    }

    private void validateRoles(List<String> roles) {
        if (roles == null) {
            return;
        }
        for (String role : roles) {
            if (!VALID_ROLES.contains(role)) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }
    }
}
