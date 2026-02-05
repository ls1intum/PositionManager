package de.tum.cit.aet.usermanagement.web;

import de.tum.cit.aet.AbstractRestIntegrationTest;
import de.tum.cit.aet.usermanagement.domain.User;
import de.tum.cit.aet.usermanagement.domain.UserGroup;
import de.tum.cit.aet.usermanagement.domain.key.UserGroupId;
import de.tum.cit.aet.usermanagement.repository.UserGroupRepository;
import de.tum.cit.aet.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("User REST API Tests")
class UserResourceTest extends AbstractRestIntegrationTest {

    private static final String BASE_URL = "/v2/users";
    private static final String ME_URL = "/v2/users/me";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setupTestData() {
        setAdminUser();

        // Create test users in the database (AbstractIntegrationTest.cleanupBefore handles cleanup)
        testUser1 = createDbUser("db_admin", "Admin", "User", "dbadmin@test.tum.de", "admin");
        testUser2 = createDbUser("db_professor", "Professor", "User", "dbprofessor@test.tum.de", "professor");
        testUser3 = createDbUser("db_employee", "Employee", "User", "dbemployee@test.tum.de", "employee");
        createDbUser("search_user", "Search", "Target", "search@test.tum.de");
    }

    private User createDbUser(String universityId, String firstName, String lastName, String email, String... roles) {
        User user = new User();
        user.setUniversityId(universityId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setJoinedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setGroups(new HashSet<>());
        user = userRepository.save(user);

        for (String role : roles) {
            UserGroup group = new UserGroup();
            UserGroupId groupId = new UserGroupId();
            groupId.setUserId(user.getId());
            groupId.setRole(role);
            group.setId(groupId);
            group.setUser(user);
            userGroupRepository.save(group);
            user.getGroups().add(group);
        }

        return user;
    }

    @Nested
    @DisplayName("GET /v2/users/me - Current User Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Authenticated user can get current user")
        void getCurrentUser_authenticated_returnsUser() throws Exception {
            setAdminUser();

            get(ME_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.universityId").value("admin"))
                    .andExpect(jsonPath("$.roles").isArray());
        }

        @Test
        @DisplayName("Professor can get current user")
        void getCurrentUser_asProfessor_returnsUser() throws Exception {
            setProfessorUser();

            get(ME_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.universityId").value("professor"))
                    .andExpect(jsonPath("$.roles", hasItem("professor")));
        }

        @Test
        @DisplayName("Employee can get current user")
        void getCurrentUser_asEmployee_returnsUser() throws Exception {
            setEmployeeUser();

            get(ME_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.universityId").value("employee"));
        }

        @Test
        @DisplayName("Job manager can get current user")
        void getCurrentUser_asJobManager_returnsUser() throws Exception {
            setJobManagerUser();

            get(ME_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.universityId").value("jobmanager"));
        }

        @Test
        @DisplayName("User without roles can get current user")
        void getCurrentUser_withoutRoles_returnsUser() throws Exception {
            setUserWithNoRoles();

            get(ME_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.universityId").value("user"))
                    .andExpect(jsonPath("$.roles", hasSize(0)));
        }

        @Test
        @DisplayName("Current user response contains all expected fields")
        void getCurrentUser_responseContainsAllFields() throws Exception {
            setAdminUser();

            get(ME_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.universityId").exists())
                    .andExpect(jsonPath("$.email").exists())
                    .andExpect(jsonPath("$.firstName").exists())
                    .andExpect(jsonPath("$.lastName").exists())
                    .andExpect(jsonPath("$.roles").isArray());
        }
    }

    @Nested
    @DisplayName("GET /v2/users - Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Admin can get all users")
        void getAllUsers_asAdmin_returnsPage() throws Exception {
            setAdminUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber());
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void getAllUsers_asProfessor_returns403() throws Exception {
            setProfessorUser();

            get(BASE_URL)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Job manager gets 403 forbidden")
        void getAllUsers_asJobManager_returns403() throws Exception {
            setJobManagerUser();

            get(BASE_URL)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Employee gets 403 forbidden")
        void getAllUsers_asEmployee_returns403() throws Exception {
            setEmployeeUser();

            get(BASE_URL)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("User without role gets 403 forbidden")
        void getAllUsers_withoutRole_returns403() throws Exception {
            setUserWithNoRoles();

            get(BASE_URL)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /v2/users - Filter Tests")
    class GetAllUsersFilterTests {

        @Test
        @DisplayName("Search filter by name")
        void getAllUsers_withSearchFilter() throws Exception {
            setAdminUser();

            get(BASE_URL + "?search=Search")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].firstName", hasItem("Search")));
        }

        @Test
        @DisplayName("Search filter by email")
        void getAllUsers_withEmailSearch() throws Exception {
            setAdminUser();

            get(BASE_URL + "?search=search@test")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].email", hasItem("search@test.tum.de")));
        }

        @Test
        @DisplayName("Role filter")
        void getAllUsers_withRoleFilter() throws Exception {
            setAdminUser();

            get(BASE_URL + "?role=admin")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].roles[*]", hasItem("admin")));
        }

        @Test
        @DisplayName("Pagination parameters")
        void getAllUsers_withPagination() throws Exception {
            setAdminUser();

            get(BASE_URL + "?page=0&size=2")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(2))))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        @DisplayName("Combined search and role filter")
        void getAllUsers_withCombinedFilters() throws Exception {
            setAdminUser();

            get(BASE_URL + "?search=Admin&role=admin")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /v2/users/{id}/roles - Update User Roles Tests")
    class UpdateUserRolesTests {

        @Test
        @DisplayName("Admin can update user roles")
        void updateUserRoles_asAdmin_succeeds() throws Exception {
            setAdminUser();

            List<String> newRoles = List.of("professor", "job_manager");

            putJson(BASE_URL + "/" + testUser3.getId() + "/roles", newRoles)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roles", containsInAnyOrder("professor", "job_manager")));

            // Verify persisted to database
            User updated = userRepository.findById(testUser3.getId()).orElseThrow();
            assertThat(updated.getGroups())
                    .extracting(g -> g.getId().getRole())
                    .containsExactlyInAnyOrder("professor", "job_manager");
        }

        @Test
        @DisplayName("Admin can add roles to user")
        void updateUserRoles_addRoles() throws Exception {
            setAdminUser();

            List<String> newRoles = List.of("professor", "job_manager");

            putJson(BASE_URL + "/" + testUser2.getId() + "/roles", newRoles)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roles", hasSize(2)))
                    .andExpect(jsonPath("$.roles", containsInAnyOrder("professor", "job_manager")));
        }

        @Test
        @DisplayName("Admin can remove all roles from user")
        void updateUserRoles_removeAllRoles() throws Exception {
            setAdminUser();

            List<String> emptyRoles = List.of();

            putJson(BASE_URL + "/" + testUser2.getId() + "/roles", emptyRoles)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roles", hasSize(0)));
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void updateUserRoles_asProfessor_returns403() throws Exception {
            setProfessorUser();

            List<String> newRoles = List.of("admin");

            putJson(BASE_URL + "/" + testUser3.getId() + "/roles", newRoles)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Job manager gets 403 forbidden")
        void updateUserRoles_asJobManager_returns403() throws Exception {
            setJobManagerUser();

            List<String> newRoles = List.of("admin");

            putJson(BASE_URL + "/" + testUser3.getId() + "/roles", newRoles)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Employee gets 403 forbidden")
        void updateUserRoles_asEmployee_returns403() throws Exception {
            setEmployeeUser();

            List<String> newRoles = List.of("admin");

            putJson(BASE_URL + "/" + testUser1.getId() + "/roles", newRoles)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Non-existent user throws exception")
        void updateUserRoles_notFound_throwsException() {
            setAdminUser();

            List<String> newRoles = List.of("professor");

            // ResourceNotFoundException is thrown but not handled globally
            assertThrows(Exception.class, () ->
                    putJson(BASE_URL + "/" + UUID.randomUUID() + "/roles", newRoles));
        }
    }

    @Nested
    @DisplayName("Response Structure Tests")
    class ResponseStructureTests {

        @Test
        @DisplayName("User list response contains paginated structure")
        void userListResponse_containsPaginatedStructure() throws Exception {
            setAdminUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber())
                    .andExpect(jsonPath("$.size").isNumber())
                    .andExpect(jsonPath("$.number").isNumber());
        }

        @Test
        @DisplayName("User in list contains all expected fields")
        void userInList_containsAllFields() throws Exception {
            setAdminUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").exists())
                    .andExpect(jsonPath("$.content[0].universityId").exists())
                    .andExpect(jsonPath("$.content[0].email").exists())
                    .andExpect(jsonPath("$.content[0].firstName").exists())
                    .andExpect(jsonPath("$.content[0].lastName").exists())
                    .andExpect(jsonPath("$.content[0].roles").isArray());
        }
    }

    @Nested
    @DisplayName("Database State Verification Tests")
    class DatabaseStateVerificationTests {

        @Test
        @DisplayName("Role updates persist to database correctly")
        void updateUserRoles_persistsCorrectly() throws Exception {
            setAdminUser();

            // Start with a fresh user with employee role
            User testUser = createDbUser("role_test_user", "Role", "Test", "roletest@test.tum.de", "employee");

            // Update to professor + job_manager
            List<String> newRoles = List.of("professor", "job_manager");

            putJson(BASE_URL + "/" + testUser.getId() + "/roles", newRoles)
                    .andExpect(status().isOk());

            // Verify database state
            User updated = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updated.getGroups()).hasSize(2);
            assertThat(updated.getGroups())
                    .extracting(g -> g.getId().getRole())
                    .containsExactlyInAnyOrder("professor", "job_manager");

            // Update to only admin
            putJson(BASE_URL + "/" + testUser.getId() + "/roles", List.of("admin"))
                    .andExpect(status().isOk());

            // Verify final state
            User finalState = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(finalState.getGroups()).hasSize(1);
            assertThat(finalState.getGroups())
                    .extracting(g -> g.getId().getRole())
                    .containsExactly("admin");
        }
    }
}
