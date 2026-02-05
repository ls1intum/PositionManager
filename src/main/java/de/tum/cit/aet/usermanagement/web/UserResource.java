package de.tum.cit.aet.usermanagement.web;

import de.tum.cit.aet.core.exceptions.ResourceNotFoundException;
import de.tum.cit.aet.core.security.CurrentUserProvider;
import de.tum.cit.aet.usermanagement.dto.CreateUserDTO;
import de.tum.cit.aet.usermanagement.dto.UserDTO;
import de.tum.cit.aet.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v2/users")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Returns the current authenticated user with their roles.
     *
     * @return the current user DTO
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(UserDTO.fromEntity(currentUserProvider.getUser()));
    }

    /**
     * Returns all users with pagination and optional filters. Admin only.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @param search optional search term
     * @param role optional role filter
     * @return paginated list of users
     */
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("lastName", "firstName"));
        return ResponseEntity.ok(userService.searchUsers(search, role, pageable));
    }

    /**
     * Creates a new user. Admin only.
     *
     * @param dto the user creation data
     * @return the created user DTO with 201 Created, or 400/409 on validation/duplicate errors
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO dto) {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        try {
            UserDTO created = userService.createUser(dto);
            return ResponseEntity.created(URI.create("/v2/users/" + created.id())).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deletes a user by ID. Admin only. Cannot delete yourself.
     *
     * @param id the user ID
     * @return 204 No Content, or 400 if trying to delete self, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        if (id.equals(currentUserProvider.getUser().getId())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates the roles for a specific user. Admin only.
     *
     * @param id the user ID
     * @param roles the new list of roles
     * @return the updated user DTO, or 400 on invalid roles, or 404 if not found
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<UserDTO> updateUserRoles(
            @PathVariable UUID id,
            @RequestBody List<String> roles) {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        try {
            return ResponseEntity.ok(userService.updateUserRoles(id, roles));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
