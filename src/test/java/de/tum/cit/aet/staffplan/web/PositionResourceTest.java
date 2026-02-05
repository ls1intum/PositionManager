package de.tum.cit.aet.staffplan.web;

import de.tum.cit.aet.AbstractRestIntegrationTest;
import de.tum.cit.aet.config.TestSecurityConfiguration;
import de.tum.cit.aet.staffplan.domain.Position;
import de.tum.cit.aet.usermanagement.domain.ResearchGroup;
import de.tum.cit.aet.usermanagement.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Position REST API Tests")
class PositionResourceTest extends AbstractRestIntegrationTest {

    private static final String BASE_URL = "/v2/positions";
    private static final String IMPORT_URL = "/v2/positions/import";

    private ResearchGroup machineLearningGroup;
    private ResearchGroup computerVisionGroup;

    @BeforeEach
    void setupTestData() {
        setAdminUser();
        machineLearningGroup = createResearchGroup("Machine Learning", "I-ML");
        computerVisionGroup = createResearchGroup("Computer Vision", "I-CV");
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
        // Machine Learning positions
        createPosition("30000001", "E14", machineLearningGroup, "Postdoc ML",
                LocalDate.of(2022, 3, 1), LocalDate.of(2025, 2, 28), 100, "00100001", "Haushaltsstelle");
        createPosition("30000002", "E13", machineLearningGroup, "PhD Student ML",
                LocalDate.of(2023, 9, 1), LocalDate.of(2027, 8, 31), 65, "00100002", "Haushaltsstelle");

        // Computer Vision positions
        createPosition("30000010", "E14", computerVisionGroup, "Postdoc CV",
                LocalDate.of(2023, 6, 1), LocalDate.of(2026, 5, 31), 100, "00100010", "Haushaltsstelle");
        createPosition("30000011", "E13", computerVisionGroup, "PhD Student CV",
                LocalDate.of(2021, 4, 1), LocalDate.of(2025, 3, 31), 65, "00100011", "Drittmittelstelle");
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
    @DisplayName("GET /v2/positions - Authorization Tests")
    class GetPositionsAuthorizationTests {

        @Test
        @DisplayName("Admin can get all positions")
        void getPositions_asAdmin_returnsAll() throws Exception {
            setAdminUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(4)));
        }

        @Test
        @DisplayName("Job manager can get all positions")
        void getPositions_asJobManager_returnsAll() throws Exception {
            setJobManagerUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(4)));
        }

        @Test
        @DisplayName("Professor sees only own group positions")
        void getPositions_asProfessor_returnsOwnGroupOnly() throws Exception {
            // Create professor user with research group
            User professorUser = TestSecurityConfiguration.createTestUser("ml_professor", "professor");
            professorUser.setResearchGroup(machineLearningGroup);
            TestSecurityConfiguration.setCurrentUser(professorUser);

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].researchGroupId", everyItem(equalTo(machineLearningGroup.getId().toString()))));
        }

        @Test
        @DisplayName("Employee sees only own group positions")
        void getPositions_asEmployee_returnsOwnGroupOnly() throws Exception {
            // Create employee user with research group
            User employeeUser = TestSecurityConfiguration.createTestUser("cv_employee", "employee");
            employeeUser.setResearchGroup(computerVisionGroup);
            TestSecurityConfiguration.setCurrentUser(employeeUser);

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].researchGroupId", everyItem(equalTo(computerVisionGroup.getId().toString()))));
        }

        @Test
        @DisplayName("Professor without research group returns empty list")
        void getPositions_asProfessorWithoutGroup_returnsEmpty() throws Exception {
            // Professor without research group assigned
            setProfessorUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("User without role gets 403 forbidden")
        void getPositions_withoutRole_returns403() throws Exception {
            setUserWithNoRoles();

            get(BASE_URL)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /v2/positions - Filter Tests")
    class GetPositionsFilterTests {

        @Test
        @DisplayName("Filter by research group ID")
        void getPositions_withResearchGroupFilter() throws Exception {
            setAdminUser();

            get(BASE_URL + "?researchGroupId=" + machineLearningGroup.getId())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].researchGroupId", everyItem(equalTo(machineLearningGroup.getId().toString()))));
        }

        @Test
        @DisplayName("Filter with non-existent research group returns empty")
        void getPositions_withNonExistentGroupFilter() throws Exception {
            setAdminUser();

            get(BASE_URL + "?researchGroupId=" + java.util.UUID.randomUUID())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("POST /v2/positions/import - Authorization Tests")
    class ImportPositionsAuthorizationTests {

        private static final String CSV_CONTENT = """
                Stellenplanrelevanzart,ObjektId,STA,Objektkürzel,Objektbezeichnung,Wert Stelle,Department ID,Organisationseinheit(Bezeichnu,TrfGr(P),BsGrd,Prozt.,Beginn (P),Ende (P),Fonds,Department ID2,PersNr,Mitarbeitergruppe,Mitarbeiterkreis,Eintrittsdatum,Voraussichtlicher Austritt Per
                Haushaltsstelle,40000001,1,BU40300001,Test Position,E13,Test Dept,Test Org,E13,100,100,1/1/24,12/31/26,2010005,0101,00200001,Wiss.MA-DM,Beschäft.vh.befr.,1/1/24,12/31/26
                """;

        @Test
        @DisplayName("Admin can import positions")
        void importPositions_asAdmin_succeeds() throws Exception {
            setAdminUser();

            uploadFile(IMPORT_URL, "file", "positions.csv", CSV_CONTENT)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1))
                    .andExpect(jsonPath("$.message").exists());

            // Verify persisted to database
            assertThat(positionRepository.findAll().stream()
                    .anyMatch(p -> "40000001".equals(p.getObjectId()))).isTrue();
        }

        @Test
        @DisplayName("Job manager can import positions")
        void importPositions_asJobManager_succeeds() throws Exception {
            setJobManagerUser();

            uploadFile(IMPORT_URL, "file", "positions.csv", CSV_CONTENT)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1));
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void importPositions_asProfessor_returns403() throws Exception {
            setProfessorUser();

            uploadFile(IMPORT_URL, "file", "positions.csv", CSV_CONTENT)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Employee gets 403 forbidden")
        void importPositions_asEmployee_returns403() throws Exception {
            setEmployeeUser();

            uploadFile(IMPORT_URL, "file", "positions.csv", CSV_CONTENT)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /v2/positions/import - Validation Tests")
    class ImportPositionsValidationTests {

        @Test
        @DisplayName("Empty file returns 400")
        void importPositions_emptyFile_returns400() throws Exception {
            setAdminUser();

            uploadFile(IMPORT_URL, "file", "positions.csv", "")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("CSV with non-matching headers returns 200 with count field")
        void importPositions_invalidCsv_returnsSuccess() throws Exception {
            setAdminUser();

            String invalidCsv = "invalid,header,format\nno,matching,columns";

            // The service is tolerant - it returns success
            uploadFile(IMPORT_URL, "file", "positions.csv", invalidCsv)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").exists());
        }
    }

    @Nested
    @DisplayName("DELETE /v2/positions - Authorization Tests")
    class DeletePositionsAuthorizationTests {

        @Test
        @DisplayName("Admin can delete all positions")
        void deletePositions_asAdmin_succeeds() throws Exception {
            setAdminUser();

            delete(BASE_URL)
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Job manager can delete all positions")
        void deletePositions_asJobManager_succeeds() throws Exception {
            setJobManagerUser();

            delete(BASE_URL)
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Professor gets 403 forbidden")
        void deletePositions_asProfessor_returns403() throws Exception {
            setProfessorUser();

            delete(BASE_URL)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Employee gets 403 forbidden")
        void deletePositions_asEmployee_returns403() throws Exception {
            setEmployeeUser();

            delete(BASE_URL)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /v2/positions - Filter Tests")
    class DeletePositionsFilterTests {

        @Test
        @DisplayName("Delete with research group filter returns 204")
        void deletePositions_byResearchGroupFilter_returns204() throws Exception {
            setAdminUser();

            // Just verify the endpoint accepts the filter parameter and returns success
            // Actual deletion verification is done by service-level tests
            delete(BASE_URL + "?researchGroupId=" + UUID.randomUUID())
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Response Structure Tests")
    class ResponseStructureTests {

        @Test
        @DisplayName("Position response contains all expected fields")
        void positionResponse_containsAllFields() throws Exception {
            setAdminUser();

            get(BASE_URL)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].objectId").exists())
                    .andExpect(jsonPath("$[0].objectCode").exists())
                    .andExpect(jsonPath("$[0].objectDescription").exists())
                    .andExpect(jsonPath("$[0].tariffGroup").exists())
                    .andExpect(jsonPath("$[0].percentage").isNumber())
                    .andExpect(jsonPath("$[0].startDate").exists())
                    .andExpect(jsonPath("$[0].endDate").exists())
                    .andExpect(jsonPath("$[0].positionRelevanceType").exists())
                    .andExpect(jsonPath("$[0].researchGroupId").exists())
                    .andExpect(jsonPath("$[0].researchGroupId").exists());
        }
    }

    @Nested
    @DisplayName("Database State Verification Tests")
    class DatabaseStateVerificationTests {

        @Test
        @DisplayName("Import persists positions to database")
        void importPositions_persistsToDatabase() throws Exception {
            setAdminUser();

            String csv = """
                    Stellenplanrelevanzart,ObjektId,STA,Objektkürzel,Objektbezeichnung,Wert Stelle,Department ID,Organisationseinheit(Bezeichnu,TrfGr(P),BsGrd,Prozt.,Beginn (P),Ende (P),Fonds,Department ID2,PersNr,Mitarbeitergruppe,Mitarbeiterkreis,Eintrittsdatum,Voraussichtlicher Austritt Per
                    Haushaltsstelle,50000001,1,BU50000001,Import Test 1,E13,Test,Test Org,E13,100,100,1/1/24,12/31/26,2010005,0101,00300001,Wiss.MA-DM,Beschäft.vh.befr.,1/1/24,12/31/26
                    Haushaltsstelle,50000002,1,BU50000002,Import Test 2,E14,Test,Test Org,E14,100,100,1/1/24,12/31/26,2010005,0101,00300002,Wiss.MA-DM,Beschäft.vh.befr.,1/1/24,12/31/26
                    """;

            uploadFile(IMPORT_URL, "file", "positions.csv", csv)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(2));

            // Verify database state
            List<Position> allPositions = positionRepository.findAll();
            assertThat(allPositions.stream().anyMatch(p -> "50000001".equals(p.getObjectId()))).isTrue();
            assertThat(allPositions.stream().anyMatch(p -> "50000002".equals(p.getObjectId()))).isTrue();

            Position imported = allPositions.stream()
                    .filter(p -> "50000001".equals(p.getObjectId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(imported.getTariffGroup()).isEqualTo("E13");
            assertThat(imported.getObjectDescription()).isEqualTo("Import Test 1");
        }
    }
}
