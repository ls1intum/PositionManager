package de.tum.cit.aet.positions.web;

import de.tum.cit.aet.AbstractRestIntegrationTest;
import de.tum.cit.aet.positions.domain.GradeValue;
import de.tum.cit.aet.positions.dto.GradeValueDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Grade Value REST API Tests")
class GradeValueResourceTest extends AbstractRestIntegrationTest {

    private static final String BASE_URL = "/v2/grade-values";

    @Nested
    @DisplayName("GET /v2/grade-values - Authorization Tests")
    class GetGradeValuesAuthorizationTests {

        @Test
        @DisplayName("Admin can get all grade values")
        void getGradeValues_asAdmin_returnsAll() throws Exception {
            setAdminUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$[*].gradeCode", hasItem("E13")));
        }

        @Test
        @DisplayName("Job manager can get all grade values")
        void getGradeValues_asJobManager_returnsAll() throws Exception {
            setJobManagerUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void getGradeValues_asProfessor_returns403() throws Exception {
            setProfessorUser();

            get(BASE_URL)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Employee gets 403 forbidden")
        void getGradeValues_asEmployee_returns403() throws Exception {
            setEmployeeUser();

            get(BASE_URL)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("User without role gets 403 forbidden")
        void getGradeValues_withoutRole_returns403() throws Exception {
            setUserWithNoRoles();

            get(BASE_URL)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /v2/grade-values - Filter Tests")
    class GetGradeValuesFilterTests {

        @Test
        @DisplayName("activeOnly=true returns only active grades")
        void getGradeValues_activeOnly() throws Exception {
            setAdminUser();

            get(BASE_URL + "?activeOnly=true")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[*].active", everyItem(equalTo(true))));
        }

        @Test
        @DisplayName("activeOnly=false returns all grades")
        void getGradeValues_includeInactive() throws Exception {
            setAdminUser();

            get(BASE_URL + "?activeOnly=false")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("GET /v2/grade-values/{id} - Tests")
    class GetSingleGradeValueTests {

        @Test
        @DisplayName("Admin can get single grade value")
        void getGradeValue_asAdmin_returnsGrade() throws Exception {
            setAdminUser();

            GradeValue gradeValue = gradeValueRepository.findByGradeCode("E13").orElseThrow();

            get(BASE_URL + "/" + gradeValue.getId())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(gradeValue.getId().toString()))
                    .andExpect(jsonPath("$.gradeCode").value("E13"))
                    .andExpect(jsonPath("$.monthlyValue").isNumber());
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void getGradeValue_asProfessor_returns403() throws Exception {
            setProfessorUser();

            GradeValue gradeValue = gradeValueRepository.findByGradeCode("E13").orElseThrow();

            get(BASE_URL + "/" + gradeValue.getId())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /v2/grade-values/in-use - Tests")
    class GetGradesInUseTests {

        @Test
        @DisplayName("Admin can get grades in use")
        void getGradesInUse_asAdmin_returnsList() throws Exception {
            setAdminUser();

            get(BASE_URL + "/in-use")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Job manager can get grades in use")
        void getGradesInUse_asJobManager_returnsList() throws Exception {
            setJobManagerUser();

            get(BASE_URL + "/in-use")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void getGradesInUse_asProfessor_returns403() throws Exception {
            setProfessorUser();

            get(BASE_URL + "/in-use")
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /v2/grade-values - Create Tests")
    class CreateGradeValueTests {

        @Test
        @DisplayName("Admin can create grade value")
        void createGradeValue_asAdmin_succeeds() throws Exception {
            setAdminUser();

            GradeValueDTO newGrade = new GradeValueDTO(
                    null,
                    "TEST_GRADE",
                    "T",
                    "Test Grade",
                    new BigDecimal("5000.00"),
                    null,
                    null,
                    999,
                    true,
                    false
            );

            postJson(BASE_URL, newGrade)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.gradeCode").value("TEST_GRADE"))
                    .andExpect(jsonPath("$.displayName").value("Test Grade"));

            // Verify persisted to database
            assertThat(gradeValueRepository.findByGradeCode("TEST_GRADE")).isPresent();
        }

        @Test
        @DisplayName("Job manager gets 403 forbidden")
        void createGradeValue_asJobManager_returns403() throws Exception {
            setJobManagerUser();

            GradeValueDTO newGrade = new GradeValueDTO(
                    null,
                    "TEST_GRADE",
                    "T",
                    "Test Grade",
                    new BigDecimal("5000.00"),
                    null,
                    null,
                    999,
                    true,
                    false
            );

            postJson(BASE_URL, newGrade)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void createGradeValue_asProfessor_returns403() throws Exception {
            setProfessorUser();

            GradeValueDTO newGrade = new GradeValueDTO(
                    null,
                    "TEST_GRADE",
                    "T",
                    "Test Grade",
                    new BigDecimal("5000.00"),
                    null,
                    null,
                    999,
                    true,
                    false
            );

            postJson(BASE_URL, newGrade)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /v2/grade-values/{id} - Update Tests")
    class UpdateGradeValueTests {

        @Test
        @DisplayName("Admin can update grade value")
        void updateGradeValue_asAdmin_succeeds() throws Exception {
            setAdminUser();

            GradeValue existing = gradeValueRepository.findByGradeCode("E13").orElseThrow();
            GradeValueDTO updateDto = new GradeValueDTO(
                    existing.getId(),
                    existing.getGradeCode(),
                    existing.getGradeType(),
                    "Updated Display Name",
                    existing.getMonthlyValue(),
                    existing.getMinSalary(),
                    existing.getMaxSalary(),
                    existing.getSortOrder(),
                    existing.getActive(),
                    false
            );

            putJson(BASE_URL + "/" + existing.getId(), updateDto)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.displayName").value("Updated Display Name"));

            // Verify persisted to database
            GradeValue updated = gradeValueRepository.findById(existing.getId()).orElseThrow();
            assertThat(updated.getDisplayName()).isEqualTo("Updated Display Name");
        }

        @Test
        @DisplayName("Job manager gets 403 forbidden")
        void updateGradeValue_asJobManager_returns403() throws Exception {
            setJobManagerUser();

            GradeValue existing = gradeValueRepository.findByGradeCode("E13").orElseThrow();
            GradeValueDTO updateDto = new GradeValueDTO(
                    existing.getId(),
                    existing.getGradeCode(),
                    existing.getGradeType(),
                    "Updated Display Name",
                    existing.getMonthlyValue(),
                    existing.getMinSalary(),
                    existing.getMaxSalary(),
                    existing.getSortOrder(),
                    existing.getActive(),
                    false
            );

            putJson(BASE_URL + "/" + existing.getId(), updateDto)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /v2/grade-values/{id} - Delete Tests")
    class DeleteGradeValueTests {

        @Test
        @DisplayName("Admin can delete unused grade value")
        void deleteGradeValue_asAdmin_succeeds() throws Exception {
            setAdminUser();

            // Create a new grade value specifically for deletion
            GradeValue gradeToDelete = new GradeValue();
            gradeToDelete.setGradeCode("DELETE_TEST");
            gradeToDelete.setGradeType("T");
            gradeToDelete.setDisplayName("Delete Test Grade");
            gradeToDelete.setMonthlyValue(new BigDecimal("1000.00"));
            gradeToDelete.setSortOrder(9999);
            gradeToDelete.setActive(true);
            gradeValueRepository.save(gradeToDelete);

            delete(BASE_URL + "/" + gradeToDelete.getId())
                    .andExpect(status().isNoContent());

            // Verify removed from database
            assertThat(gradeValueRepository.findById(gradeToDelete.getId())).isEmpty();
        }

        @Test
        @DisplayName("Job manager gets 403 forbidden")
        void deleteGradeValue_asJobManager_returns403() throws Exception {
            setJobManagerUser();

            GradeValue existing = gradeValueRepository.findByGradeCode("E13").orElseThrow();

            delete(BASE_URL + "/" + existing.getId())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void deleteGradeValue_asProfessor_returns403() throws Exception {
            setProfessorUser();

            GradeValue existing = gradeValueRepository.findByGradeCode("E13").orElseThrow();

            delete(BASE_URL + "/" + existing.getId())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deleting non-existent grade returns 400")
        void deleteGradeValue_notFound_returns400() throws Exception {
            setAdminUser();

            // The service throws IllegalArgumentException for non-existent IDs
            delete(BASE_URL + "/" + UUID.randomUUID())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Response Structure Tests")
    class ResponseStructureTests {

        @Test
        @DisplayName("Grade value response contains all expected fields")
        void gradeValueResponse_containsAllFields() throws Exception {
            setAdminUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].gradeCode").exists())
                    .andExpect(jsonPath("$[0].gradeType").exists())
                    .andExpect(jsonPath("$[0].displayName").exists())
                    .andExpect(jsonPath("$[0].monthlyValue").isNumber())
                    .andExpect(jsonPath("$[0].sortOrder").isNumber())
                    .andExpect(jsonPath("$[0].active").isBoolean());
        }
    }
}
