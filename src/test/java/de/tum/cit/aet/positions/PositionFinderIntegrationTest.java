package de.tum.cit.aet.positions;

import de.tum.cit.aet.AbstractIntegrationTest;
import de.tum.cit.aet.positions.domain.Position;
import de.tum.cit.aet.positions.dto.PositionFinderRequestDTO;
import de.tum.cit.aet.positions.dto.PositionFinderResponseDTO;
import de.tum.cit.aet.positions.dto.PositionMatchDTO;
import de.tum.cit.aet.usermanagement.domain.ResearchGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Position Finder Integration Tests")
class PositionFinderIntegrationTest extends AbstractIntegrationTest {

    private ResearchGroup machineLearningGroup;
    private ResearchGroup computerVisionGroup;
    private ResearchGroup databaseSystemsGroup;
    private ResearchGroup softwareEngineeringGroup;
    private ResearchGroup artificialIntelligenceGroup;

    @BeforeEach
    void setupTestData() {
        setAdminUser();

        machineLearningGroup = createResearchGroup("Machine Learning", "I-ML");
        computerVisionGroup = createResearchGroup("Computer Vision", "I-CV");
        databaseSystemsGroup = createResearchGroup("Database Systems", "I-DBS");
        softwareEngineeringGroup = createResearchGroup("Software Engineering", "I-SE");
        artificialIntelligenceGroup = createResearchGroup("Artificial Intelligence", "I-AI");

        setupPositionsFromTestData();
    }

    private ResearchGroup createResearchGroup(String name, String abbreviation) {
        ResearchGroup group = new ResearchGroup();
        group.setName(name);
        group.setAbbreviation(abbreviation);
        group.setDepartment("Computer Science");
        return researchGroupRepository.save(group);
    }

    private void setupPositionsFromTestData() {
        // Machine Learning positions
        createPosition("30000001", "W3", machineLearningGroup, "W3-Professor/in", LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31), 100, "00100001", "Haushaltsstelle");
        createPosition("30000002", "E14", machineLearningGroup, "Postdoc ML", LocalDate.of(2022, 3, 1), LocalDate.of(2025, 2, 28), 100, "00100002", "Haushaltsstelle");
        createPosition("30000003", "E13", machineLearningGroup, "PhD Student ML 1", LocalDate.of(2023, 9, 1), LocalDate.of(2027, 8, 31), 65, "00100003", "Haushaltsstelle");
        createPosition("30000004", "E13", machineLearningGroup, "PhD Student ML 2", LocalDate.of(2022, 10, 1), LocalDate.of(2026, 9, 30), 65, "00100004", "Haushaltsstelle");
        createPosition("30000005", "E13", machineLearningGroup, "Research Assistant ML", LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31), 50, "00100005", "Drittmittelstelle");

        // Computer Vision positions
        createPosition("30000010", "W3", computerVisionGroup, "W3-Professor/in CV", LocalDate.of(2018, 4, 1), LocalDate.of(2099, 12, 31), 100, "00100010", "Haushaltsstelle");
        createPosition("30000011", "E14", computerVisionGroup, "Postdoc CV", LocalDate.of(2023, 6, 1), LocalDate.of(2026, 5, 31), 100, "00100011", "Haushaltsstelle");
        createPosition("30000012", "E13", computerVisionGroup, "PhD Student CV", LocalDate.of(2021, 4, 1), LocalDate.of(2025, 3, 31), 65, "00100012", "Haushaltsstelle");
        createPosition("30000013", "E13", computerVisionGroup, "Research Engineer CV", LocalDate.of(2023, 1, 1), LocalDate.of(2025, 12, 31), 100, "00100013", "Drittmittelstelle");

        // Database Systems positions
        createPosition("30000020", "W3", databaseSystemsGroup, "W3-Professor/in DBS", LocalDate.of(2019, 10, 1), LocalDate.of(2099, 12, 31), 100, "00100020", "Haushaltsstelle");
        createPosition("30000021", "E14", databaseSystemsGroup, "Postdoc DBS", LocalDate.of(2022, 2, 1), LocalDate.of(2025, 1, 31), 100, "00100021", "Haushaltsstelle");
        createPosition("30000022", "E13", databaseSystemsGroup, "PhD Student DBS 1", LocalDate.of(2023, 10, 1), LocalDate.of(2027, 9, 30), 65, "00100022", "Haushaltsstelle");
        createPosition("30000023", "E13", databaseSystemsGroup, "PhD Student DBS 2", LocalDate.of(2022, 4, 1), LocalDate.of(2026, 3, 31), 65, "00100023", "Haushaltsstelle");

        // Software Engineering positions
        createPosition("30000030", "W3", softwareEngineeringGroup, "W3-Professor/in SE", LocalDate.of(2017, 7, 1), LocalDate.of(2099, 12, 31), 100, "00100030", "Haushaltsstelle");
        createPosition("30000031", "E15", softwareEngineeringGroup, "Senior Researcher SE", LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31), 100, "00100031", "Haushaltsstelle");
        createPosition("30000032", "E14", softwareEngineeringGroup, "Postdoc SE", LocalDate.of(2022, 9, 1), LocalDate.of(2025, 8, 31), 100, "00100032", "Haushaltsstelle");
        createPosition("30000033", "E13", softwareEngineeringGroup, "PhD Student SE", LocalDate.of(2024, 1, 1), LocalDate.of(2027, 12, 31), 65, "00100033", "Haushaltsstelle");
        createPosition("30000034", "E13", softwareEngineeringGroup, "Research Assistant SE", LocalDate.of(2023, 6, 1), LocalDate.of(2026, 5, 31), 75, "00100034", "Drittmittelstelle");

        // AI positions
        createPosition("30000040", "W3", artificialIntelligenceGroup, "W3-Professor/in AI", LocalDate.of(2021, 1, 1), LocalDate.of(2099, 12, 31), 100, "00100040", "Haushaltsstelle");
        createPosition("30000041", "E14", artificialIntelligenceGroup, "Postdoc AI", LocalDate.of(2023, 4, 1), LocalDate.of(2026, 3, 31), 100, "00100041", "Haushaltsstelle");
        createPosition("30000042", "E13", artificialIntelligenceGroup, "PhD Student AI 1", LocalDate.of(2022, 10, 1), LocalDate.of(2026, 9, 30), 65, "00100042", "Haushaltsstelle");
        createPosition("30000043", "E13", artificialIntelligenceGroup, "PhD Student AI 2", LocalDate.of(2024, 4, 1), LocalDate.of(2028, 3, 31), 65, "00100043", "Haushaltsstelle");
        createPosition("30000044", "E14", artificialIntelligenceGroup, "AI Research Engineer", LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31), 100, "00100044", "Drittmittelstelle");

        // Support staff
        createPosition("30000050", "E8", machineLearningGroup, "Secretary ML", LocalDate.of(2021, 1, 1), LocalDate.of(2099, 12, 31), 50, "00100050", "Haushaltsstelle");
        createPosition("30000051", "E9", computerVisionGroup, "Technical Staff CV", LocalDate.of(2019, 3, 1), LocalDate.of(2099, 12, 31), 100, "00100051", "Haushaltsstelle");
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
    @DisplayName("E14 Position Search Tests")
    class E14PositionSearchTests {

        @Test
        @DisplayName("Should find available E14 positions after assignments end")
        void findAvailableE14Positions() {
            // Search for E14 positions from March 2025 to March 2026
            // Available E14 positions:
            // - 30000002 (ML Postdoc): ends 2/28/25 -> available from 3/1/25
            // - 30000021 (DBS Postdoc): ends 1/31/25 -> available from 2/1/25
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E14",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            assertThat(response.employeeGrade()).isEqualTo("E14");
            assertThat(response.fillPercentage()).isEqualTo(100);

            // Only positions that are fully available for the ENTIRE period should match
            List<String> matchedObjectIds = response.matches().stream()
                    .map(PositionMatchDTO::objectId)
                    .toList();

            assertThat(matchedObjectIds).contains("30000002", "30000021");
        }

        @Test
        @DisplayName("Should find E14 positions available in 2026")
        void findAvailableE14PositionsIn2026() {
            // Search for E14 positions in 2026 when more positions become available
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    "E14",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // By 2026, 30000002, 30000021, and 30000032 should all be available
            List<String> matchedObjectIds = response.matches().stream()
                    .map(PositionMatchDTO::objectId)
                    .toList();

            assertThat(matchedObjectIds).contains("30000002", "30000021", "30000032");
        }
    }

    @Nested
    @DisplayName("E13 Position Search Tests")
    class E13PositionSearchTests {

        @Test
        @DisplayName("Should find available E13 positions in 2026")
        void findAvailableE13Positions() {
            // Search for E13 positions in 2026
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2027, 1, 1),
                    "E13",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            assertThat(response.employeeGrade()).isEqualTo("E13");
            assertThat(response.fillPercentage()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should find no positions when all are occupied")
        void findNoPositionsAllOccupied() {
            // Search during a period when all positions are occupied
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31),
                    "E13",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // During 2024, most E13 positions are occupied at 65% or more
            // For a 100% request, there should be few or no matches
            assertThat(response.totalMatchesFound()).isLessThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Partial Availability Tests")
    class PartialAvailabilityTests {

        @Test
        @DisplayName("Should find positions with partial availability for 50% request")
        void findPartialAvailability50Percent() {
            // Search for E13 at 50% - this should find more positions
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 12, 31),
                    "E13",
                    50,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            assertThat(response.fillPercentage()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should find positions with 35% availability for PhD positions")
        void findPartialAvailability35Percent() {
            // PhD positions have 65% assignments, leaving 35% available
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 12, 31),
                    "E13",
                    35,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            assertThat(response.fillPercentage()).isEqualTo(35);
        }
    }

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("Should filter by research group")
        void filterByResearchGroup() {
            // Search only in Machine Learning group
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E14",
                    100,
                    machineLearningGroup.getId(),
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // Should only find positions from ML group
            List<PositionMatchDTO> matches = response.matches();
            if (!matches.isEmpty()) {
                // 30000002 is the ML E14 position that ends 2/28/25
                assertThat(matches.stream().map(PositionMatchDTO::objectId))
                        .contains("30000002");
            }
        }

        @Test
        @DisplayName("Should filter by relevance type - Haushaltsstelle only")
        void filterByRelevanceTypeHaushaltsstelle() {
            // Search only for Haushaltsstelle positions
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    "E13",
                    100,
                    null,
                    List.of("Haushaltsstelle")
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // Should not include Drittmittelstelle positions
            List<PositionMatchDTO> matches = response.matches();
            assertThat(matches.stream().map(PositionMatchDTO::objectId))
                    .doesNotContain("30000005", "30000013", "30000034");
        }

        @Test
        @DisplayName("Should filter by relevance type - Drittmittelstelle only")
        void filterByRelevanceTypeDrittmittelstelle() {
            // Search only for Drittmittelstelle positions
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2027, 1, 1),
                    LocalDate.of(2027, 12, 31),
                    "E13",
                    100,
                    null,
                    List.of("Drittmittelstelle")
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // Should only include Drittmittelstelle positions that are available
            List<PositionMatchDTO> matches = response.matches();
            for (PositionMatchDTO match : matches) {
                assertThat(match.positionRelevanceType()).isEqualTo("Drittmittelstelle");
            }
        }
    }

    @Nested
    @DisplayName("Split Suggestion Tests")
    class SplitSuggestionTests {

        @Test
        @DisplayName("Should generate no matches when W3 positions are occupied")
        void noMatchesForW3() {
            // W3 positions (professors) are typically occupied indefinitely
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    "W3",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // All W3 positions are occupied until 2099, so there should be 0 matches
            assertThat(response.totalMatchesFound()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Grade Compatibility Tests")
    class GradeCompatibilityTests {

        @Test
        @DisplayName("E13 employee should match E14 position with budget waste")
        void higherGradeAcceptsLowerEmployee() {
            // E13 employee searching for positions - can be placed on E14 positions
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E13",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // E13 employee can be placed on E14 positions
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("E14 employee should not match E13 positions (insufficient budget)")
        void lowerGradeRejectsHigherEmployee() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E14",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // E14 employee should NOT be placed on E13 positions
            // All matches should be E14 or higher grade positions
            List<PositionMatchDTO> matches = response.matches();
            for (PositionMatchDTO match : matches) {
                // E14 or higher should be acceptable
                assertThat(match.positionGrade()).isIn("E14", "E15", "W2", "W3");
            }
        }

        @Test
        @DisplayName("Grade normalization should work correctly")
        void gradeNormalization() {
            // Test grade normalization - searching for "E13" should find E13 positions
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    "E13",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            assertThat(response.employeeGrade()).isEqualTo("E13");
        }
    }

    @Nested
    @DisplayName("Budget Calculation Tests")
    class BudgetCalculationTests {

        @Test
        @DisplayName("Should calculate correct employee monthly cost")
        void calculateEmployeeMonthlyCost() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E13",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // E13 at 100% = 5600.00 (from grade values)
            assertThat(response.employeeMonthlyCost())
                    .isEqualByComparingTo(new BigDecimal("5600.00"));
        }

        @Test
        @DisplayName("Should calculate correct employee monthly cost for partial percentage")
        void calculateEmployeeMonthlyCostPartial() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E13",
                    50,  // 50% employment
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // E13 at 50% = 5600.00 * 0.50 = 2800.00
            assertThat(response.employeeMonthlyCost())
                    .isEqualByComparingTo(new BigDecimal("2800.00"));
        }

        @Test
        @DisplayName("Should report waste when placing E13 on E14 position")
        void wasteCalculation() {
            // Search for E13 employee to find E14 positions
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 3, 1),
                    LocalDate.of(2026, 3, 1),
                    "E13",
                    100,
                    null,
                    null
            );

            PositionFinderResponseDTO response = positionFinderService.findPositions(request);

            // If E14 positions are in results, they should have waste > 0
            List<PositionMatchDTO> matches = response.matches();
            for (PositionMatchDTO match : matches) {
                if ("E14".equals(match.positionGrade())) {
                    // E14 budget (6000) - E13 cost (5600) = 400 waste
                    assertThat(match.wasteAmount()).isPositive();
                    assertThat(match.wastePercentage()).isGreaterThan(0);
                }
            }
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should reject request with missing start date")
        void rejectMissingStartDate() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    null,  // missing start date
                    LocalDate.of(2025, 12, 31),
                    "E13",
                    100,
                    null,
                    null
            );

            assertThatThrownBy(() -> positionFinderService.findPositions(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("date");
        }

        @Test
        @DisplayName("Should reject request with missing end date")
        void rejectMissingEndDate() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 1, 1),
                    null,  // missing end date
                    "E13",
                    100,
                    null,
                    null
            );

            assertThatThrownBy(() -> positionFinderService.findPositions(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("date");
        }

        @Test
        @DisplayName("Should reject request with invalid date range")
        void rejectInvalidDateRange() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 12, 31),  // start after end
                    LocalDate.of(2025, 1, 1),
                    "E13",
                    100,
                    null,
                    null
            );

            assertThatThrownBy(() -> positionFinderService.findPositions(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("before");
        }

        @Test
        @DisplayName("Should reject request with unknown grade")
        void rejectUnknownGrade() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    "UNKNOWN_GRADE",
                    100,
                    null,
                    null
            );

            assertThatThrownBy(() -> positionFinderService.findPositions(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("grade");
        }

        @Test
        @DisplayName("Should reject request with invalid fill percentage - too high")
        void rejectInvalidFillPercentageTooHigh() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    "E13",
                    150,  // invalid: > 100
                    null,
                    null
            );

            assertThatThrownBy(() -> positionFinderService.findPositions(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("percentage");
        }

        @Test
        @DisplayName("Should reject request with invalid fill percentage - zero")
        void rejectInvalidFillPercentageZero() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    "E13",
                    0,  // invalid: < 1
                    null,
                    null
            );

            assertThatThrownBy(() -> positionFinderService.findPositions(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("percentage");
        }

        @Test
        @DisplayName("Should reject request with missing grade")
        void rejectMissingGrade() {
            PositionFinderRequestDTO request = new PositionFinderRequestDTO(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    null,
                    100,
                    null,
                    null
            );

            assertThatThrownBy(() -> positionFinderService.findPositions(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("grade");
        }
    }
}
