# Position Finder Integration Tests

This document describes all test scenarios for the Position Finder algorithm.

## Overview

- **Total Tests:** 23
- **Test Class:** `PositionFinderIntegrationTest`
- **Test Data:** Simulates 26 positions across 5 research groups (based on `docker/test-data/positions-test.csv`)

## Test Data Setup

### Research Groups
| Group | Abbreviation |
|-------|--------------|
| Machine Learning | I-ML |
| Computer Vision | I-CV |
| Database Systems | I-DBS |
| Software Engineering | I-SE |
| Artificial Intelligence | I-AI |

### Positions Created
| ObjectId | Grade | Group | End Date | Percentage | Type |
|----------|-------|-------|----------|------------|------|
| 30000001 | W3 | ML | 2099-12-31 | 100% | Haushaltsstelle |
| 30000002 | E14 | ML | 2025-02-28 | 100% | Haushaltsstelle |
| 30000003 | E13 | ML | 2027-08-31 | 65% | Haushaltsstelle |
| 30000004 | E13 | ML | 2026-09-30 | 65% | Haushaltsstelle |
| 30000005 | E13 | ML | 2026-12-31 | 50% | Drittmittelstelle |
| 30000010 | W3 | CV | 2099-12-31 | 100% | Haushaltsstelle |
| 30000011 | E14 | CV | 2026-05-31 | 100% | Haushaltsstelle |
| 30000012 | E13 | CV | 2025-03-31 | 65% | Haushaltsstelle |
| 30000013 | E13 | CV | 2025-12-31 | 100% | Drittmittelstelle |
| 30000020 | W3 | DBS | 2099-12-31 | 100% | Haushaltsstelle |
| 30000021 | E14 | DBS | 2025-01-31 | 100% | Haushaltsstelle |
| 30000022 | E13 | DBS | 2027-09-30 | 65% | Haushaltsstelle |
| 30000023 | E13 | DBS | 2026-03-31 | 65% | Haushaltsstelle |
| 30000030 | W3 | SE | 2099-12-31 | 100% | Haushaltsstelle |
| 30000031 | E15 | SE | 2099-12-31 | 100% | Haushaltsstelle |
| 30000032 | E14 | SE | 2025-08-31 | 100% | Haushaltsstelle |
| 30000033 | E13 | SE | 2027-12-31 | 65% | Haushaltsstelle |
| 30000034 | E13 | SE | 2026-05-31 | 75% | Drittmittelstelle |
| 30000040 | W3 | AI | 2099-12-31 | 100% | Haushaltsstelle |
| 30000041 | E14 | AI | 2026-03-31 | 100% | Haushaltsstelle |
| 30000042 | E13 | AI | 2026-09-30 | 65% | Haushaltsstelle |
| 30000043 | E13 | AI | 2028-03-31 | 65% | Haushaltsstelle |
| 30000044 | E14 | AI | 2026-12-31 | 100% | Drittmittelstelle |
| 30000050 | E8 | ML | 2099-12-31 | 50% | Haushaltsstelle |
| 30000051 | E9 | CV | 2099-12-31 | 100% | Haushaltsstelle |

---

## Test Scenarios

### 1. E14 Position Search Tests (2 tests)

| # | Test Name | Description | Search Params | Expected Result |
|---|-----------|-------------|---------------|-----------------|
| 1 | `findAvailableE14Positions` | Find E14 positions available after assignments end | E14, 100%, 2025-03-01 to 2026-03-01 | Contains 30000002, 30000021 |
| 2 | `findAvailableE14PositionsIn2026` | Find E14 positions in 2026 when more become available | E14, 100%, 2026-01-01 to 2026-12-31 | Contains 30000002, 30000021, 30000032 |

### 2. E13 Position Search Tests (2 tests)

| # | Test Name | Description | Search Params | Expected Result |
|---|-----------|-------------|---------------|-----------------|
| 3 | `findAvailableE13Positions` | Find available E13 positions in 2026 | E13, 100%, 2026-01-01 to 2027-01-01 | Returns valid response |
| 4 | `findNoPositionsAllOccupied` | Verify no matches when all positions occupied | E13, 100%, 2024-01-01 to 2024-12-31 | 0-1 matches |

### 3. Partial Availability Tests (2 tests)

| # | Test Name | Description | Search Params | Expected Result |
|---|-----------|-------------|---------------|-----------------|
| 5 | `findPartialAvailability50Percent` | Find positions for 50% employment request | E13, 50%, 2025-06-01 to 2025-12-31 | Returns matches |
| 6 | `findPartialAvailability35Percent` | Find positions with 35% availability (PhD positions have 65% assigned) | E13, 35%, 2025-06-01 to 2025-12-31 | Returns matches |

### 4. Filter Tests (3 tests)

| # | Test Name | Description | Search Params | Expected Result |
|---|-----------|-------------|---------------|-----------------|
| 7 | `filterByResearchGroup` | Filter positions by research group | E14, 100%, ML group only | Contains 30000002 |
| 8 | `filterByRelevanceTypeHaushaltsstelle` | Filter to Haushaltsstelle positions only | E13, 100%, relevanceTypes=["Haushaltsstelle"] | Excludes 30000005, 30000013, 30000034 |
| 9 | `filterByRelevanceTypeDrittmittelstelle` | Filter to Drittmittelstelle positions only | E13, 100%, relevanceTypes=["Drittmittelstelle"] | All matches are Drittmittelstelle |

### 5. Split Suggestion Tests (1 test)

| # | Test Name | Description | Search Params | Expected Result |
|---|-----------|-------------|---------------|-----------------|
| 10 | `noMatchesForW3` | Verify no single matches for occupied W3 positions | W3, 100%, 2025-01-01 to 2025-12-31 | 0 matches |

### 6. Grade Compatibility Tests (3 tests)

| # | Test Name | Description | Search Params | Expected Result |
|---|-----------|-------------|---------------|-----------------|
| 11 | `higherGradeAcceptsLowerEmployee` | E13 employee can be placed on E14 position (with budget waste) | E13, 100%, 2025-03-01 to 2026-03-01 | Returns valid response |
| 12 | `lowerGradeRejectsHigherEmployee` | E14 employee cannot be placed on E13 positions (insufficient budget) | E14, 100%, 2025-03-01 to 2026-03-01 | All matches are E14+ grades |
| 13 | `gradeNormalization` | Grade codes are normalized correctly | E13, 100%, 2026-01-01 to 2026-12-31 | employeeGrade = "E13" |

### 7. Budget Calculation Tests (3 tests)

| # | Test Name | Description | Search Params | Expected Result |
|---|-----------|-------------|---------------|-----------------|
| 14 | `calculateEmployeeMonthlyCost` | Verify correct monthly cost for 100% E13 | E13, 100% | employeeMonthlyCost = 5600.00 |
| 15 | `calculateEmployeeMonthlyCostPartial` | Verify correct monthly cost for 50% E13 | E13, 50% | employeeMonthlyCost = 2800.00 |
| 16 | `wasteCalculation` | Verify waste is calculated when E13 on E14 position | E13, 100% | E14 positions have wasteAmount > 0 |

### 8. Validation Tests (7 tests)

| # | Test Name | Description | Invalid Input | Expected Result |
|---|-----------|-------------|---------------|-----------------|
| 17 | `rejectMissingStartDate` | Reject request without start date | startDate = null | IllegalArgumentException with "date" |
| 18 | `rejectMissingEndDate` | Reject request without end date | endDate = null | IllegalArgumentException with "date" |
| 19 | `rejectInvalidDateRange` | Reject request with start after end | start > end | IllegalArgumentException with "before" |
| 20 | `rejectUnknownGrade` | Reject request with unknown grade | grade = "UNKNOWN_GRADE" | IllegalArgumentException with "grade" |
| 21 | `rejectInvalidFillPercentageTooHigh` | Reject percentage > 100 | fillPercentage = 150 | IllegalArgumentException with "percentage" |
| 22 | `rejectInvalidFillPercentageZero` | Reject percentage = 0 | fillPercentage = 0 | IllegalArgumentException with "percentage" |
| 23 | `rejectMissingGrade` | Reject request without grade | employeeGrade = null | IllegalArgumentException with "grade" |

---

## Grade Values Used in Tests

| Grade | Monthly Value | Type |
|-------|--------------|------|
| E6 | 3,450.00 | Entgelt |
| E8 | 3,600.00 | Entgelt |
| E9 | 4,050.00 | Entgelt |
| E9A | 4,050.00 | Entgelt |
| E9B | 4,050.00 | Entgelt |
| E10 | 4,350.00 | Entgelt |
| E11 | 4,550.00 | Entgelt |
| E12 | 4,750.00 | Entgelt |
| E13 | 5,600.00 | Entgelt |
| E14 | 6,000.00 | Entgelt |
| E15 | 6,550.00 | Entgelt |
| A13 | 5,600.00 | Beamten |
| A14 | 6,100.00 | Beamten |
| A15 | 6,900.00 | Beamten |
| W2 | 7,100.00 | Professur |
| W3 | 8,300.00 | Professur |

---

## Running the Tests

```bash
# Run all position finder tests
./gradlew test --tests "de.tum.cit.aet.positions.PositionFinderIntegrationTest"

# Run specific test category
./gradlew test --tests "de.tum.cit.aet.positions.PositionFinderIntegrationTest\$ValidationTests"

# Run with verbose output
./gradlew test --tests "de.tum.cit.aet.positions.PositionFinderIntegrationTest" --info
```

## Test Infrastructure

- **Database:** H2 in-memory with PostgreSQL mode
- **Security:** Mock `CurrentUserProvider` via `TestSecurityConfiguration`
- **Grade Values:** Seeded in `@BeforeAll` via `AbstractIntegrationTest`
- **Cleanup:** Positions and research groups deleted in `@BeforeEach`
