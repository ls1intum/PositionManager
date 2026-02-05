package de.tum.cit.aet.staffplan.web;

import de.tum.cit.aet.AbstractRestIntegrationTest;
import de.tum.cit.aet.staffplan.domain.Position;
import de.tum.cit.aet.staffplan.dto.PositionFinderRequestDTO;
import de.tum.cit.aet.usermanagement.domain.ResearchGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Position Finder REST API Tests")
class PositionFinderResourceTest extends AbstractRestIntegrationTest {

    private static final String SEARCH_URL = "/v2/position-finder/search";
    private static final String RELEVANCE_TYPES_URL = "/v2/position-finder/relevance-types";

    private ResearchGroup machineLearningGroup;

    @BeforeEach
    void setupTestData() {
        setAdminUser();
        machineLearningGroup = createResearchGroup("Machine Learning", "I-ML");
        setupTestPositions();
    }

    private ResearchGroup createResearchGroup(String name, String abbreviation) {
        ResearchGroup group = new ResearchGroup();
        group.setName(name);
        group.setAbbreviation(abbreviation);
        group.setDepartment("Computer Science");
        return researchGroupRepository.save(group);
    }

    private void setupTestPositions() {
        createPosition("30000001", "E14", machineLearningGroup, "Postdoc ML",
                LocalDate.of(2022, 3, 1), LocalDate.of(2025, 2, 28), 100, "00100001", "Haushaltsstelle");
        createPosition("30000002", "E13", machineLearningGroup, "PhD Student ML",
                LocalDate.of(2023, 9, 1), LocalDate.of(2027, 8, 31), 65, "00100002", "Haushaltsstelle");
        createPosition("30000003", "E13", machineLearningGroup, "Research Assistant ML",
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31), 50, "00100003", "Drittmittelstelle");
    }

    private void createPosition(String objectId, String tariffGroup, ResearchGroup researchGroup,
                                 String description, LocalDate startDate, LocalDate endDate,
                                 int percentage, String personnelNumber, String relevanceType) {
        Position position = new Position();
        position.setObjectId(objectId);
        position.setTariffGroup(tariffGroup);
        position.setResearchGroup(researchGroup);
        position.setObjectDescription(description);
        position.setStartDate(startDate);
        position.setEndDate(endDate);
        position.setPercentage(BigDecimal.valueOf(percentage));
        position.setPersonnelNumber(personnelNumber);
        position.setPositionRelevanceType(relevanceType);
        position.setObjectCode("BU402" + objectId.substring(5));
        position.setOrganizationUnit(researchGroup.getName());
        positionRepository.save(position);
    }

    @Nested
    @DisplayName("POST /v2/position-finder/search - Authorization Tests")
    class SearchAuthorizationTests {

        private PositionFinderRequestDTO validRequest;

        @BeforeEach
        void setup() {
            validRequest = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E14",
                    100,
                    null,
                    null
            );
        }

        @Test
        @DisplayName("Admin can search positions")
        void searchPositions_asAdmin_returnsMatches() throws Exception {
            setAdminUser();

            postJson(SEARCH_URL, validRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employeeGrade").value("E14"))
                    .andExpect(jsonPath("$.fillPercentage").value(100));
        }

        @Test
        @DisplayName("Job manager can search positions")
        void searchPositions_asJobManager_returnsMatches() throws Exception {
            setJobManagerUser();

            postJson(SEARCH_URL, validRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employeeGrade").value("E14"));
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void searchPositions_asProfessor_returns403() throws Exception {
            setProfessorUser();

            postJson(SEARCH_URL, validRequest)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Employee gets 403 forbidden")
        void searchPositions_asEmployee_returns403() throws Exception {
            setEmployeeUser();

            postJson(SEARCH_URL, validRequest)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("User without role gets 403 forbidden")
        void searchPositions_withoutRole_returns403() throws Exception {
            setUserWithNoRoles();

            postJson(SEARCH_URL, validRequest)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /v2/position-finder/search - Validation Tests")
    class SearchValidationTests {

        @Test
        @DisplayName("Invalid grade returns 400")
        void searchPositions_invalidGrade_returns400() throws Exception {
            setAdminUser();
            PositionFinderRequestDTO invalidRequest = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    "INVALID_GRADE",
                    100,
                    null,
                    null
            );

            // IllegalArgumentException is caught by GlobalExceptionHandler and returns 400
            postJson(SEARCH_URL, invalidRequest)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Missing start date returns 400")
        void searchPositions_missingStartDate_returns400() throws Exception {
            setAdminUser();
            PositionFinderRequestDTO invalidRequest = new PositionFinderRequestDTO(
                    null,
                    LocalDate.of(2025, 12, 31),
                    "E13",
                    100,
                    null,
                    null
            );

            postJson(SEARCH_URL, invalidRequest)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Missing end date returns 400")
        void searchPositions_missingEndDate_returns400() throws Exception {
            setAdminUser();
            PositionFinderRequestDTO invalidRequest = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 1, 1),
                    null,
                    "E13",
                    100,
                    null,
                    null
            );

            postJson(SEARCH_URL, invalidRequest)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Invalid fill percentage returns 400")
        void searchPositions_invalidFillPercentage_returns400() throws Exception {
            setAdminUser();
            PositionFinderRequestDTO invalidRequest = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    "E13",
                    150, // Invalid: > 100
                    null,
                    null
            );

            postJson(SEARCH_URL, invalidRequest)
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /v2/position-finder/search - Response Structure Tests")
    class SearchResponseStructureTests {

        @Test
        @DisplayName("Response contains all expected fields")
        void searchPositions_verifyResponseStructure() throws Exception {
            setAdminUser();
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E14",
                    100,
                    null,
                    null
            );

            postJson(SEARCH_URL, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employeeMonthlyCost").isNumber())
                    .andExpect(jsonPath("$.employeeGrade").value("E14"))
                    .andExpect(jsonPath("$.fillPercentage").value(100))
                    .andExpect(jsonPath("$.totalMatchesFound").isNumber())
                    .andExpect(jsonPath("$.matches").isArray())
                    .andExpect(jsonPath("$.splitSuggestions").isArray());
        }

        @Test
        @DisplayName("Matches array exists in response")
        void searchPositions_verifyMatchArrayExists() throws Exception {
            setAdminUser();
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E14",
                    100,
                    null,
                    null
            );

            // Verify the matches array exists (may be empty if no positions match)
            postJson(SEARCH_URL, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.matches").isArray());
        }

        @Test
        @DisplayName("Filter by relevance type works")
        void searchPositions_filterByRelevanceType() throws Exception {
            setAdminUser();
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2027, 1, 1),
                    LocalDate.of(2027, 12, 31),
                    "E13",
                    100,
                    null,
                    List.of("Haushaltsstelle")
            );

            postJson(SEARCH_URL, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.matches[*].positionRelevanceType",
                            everyItem(equalTo("Haushaltsstelle"))));
        }

        @Test
        @DisplayName("Filter by research group works")
        void searchPositions_filterByResearchGroup() throws Exception {
            setAdminUser();
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E14",
                    100,
                    machineLearningGroup.getId(),
                    null
            );

            postJson(SEARCH_URL, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.matches[*].researchGroupName",
                            everyItem(equalTo("Machine Learning"))));
        }
    }

    @Nested
    @DisplayName("GET /v2/position-finder/relevance-types - Tests")
    class RelevanceTypesTests {

        @Test
        @DisplayName("Admin can get relevance types")
        void getRelevanceTypes_asAdmin_returnsList() throws Exception {
            setAdminUser();

            get(RELEVANCE_TYPES_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasItems("Haushaltsstelle", "Drittmittelstelle")));
        }

        @Test
        @DisplayName("Job manager can get relevance types")
        void getRelevanceTypes_asJobManager_returnsList() throws Exception {
            setJobManagerUser();

            get(RELEVANCE_TYPES_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void getRelevanceTypes_asProfessor_returns403() throws Exception {
            setProfessorUser();

            get(RELEVANCE_TYPES_URL)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Employee gets 403 forbidden")
        void getRelevanceTypes_asEmployee_returns403() throws Exception {
            setEmployeeUser();

            get(RELEVANCE_TYPES_URL)
                    .andExpect(status().isForbidden());
        }
    }
}
