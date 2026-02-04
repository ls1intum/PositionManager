package de.tum.cit.aet.usermanagement.dto;

import java.util.List;

/**
 * DTO for the result of a research group CSV import operation.
 */
public record ResearchGroupImportResultDTO(
        int created,
        int updated,
        int skipped,
        List<String> errors,
        List<String> warnings
) {
    public static ResearchGroupImportResultDTO empty() {
        return new ResearchGroupImportResultDTO(0, 0, 0, List.of(), List.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int created = 0;
        private int updated = 0;
        private int skipped = 0;
        private final java.util.ArrayList<String> errors = new java.util.ArrayList<>();
        private final java.util.ArrayList<String> warnings = new java.util.ArrayList<>();

        public Builder incrementCreated() {
            created++;
            return this;
        }

        public Builder incrementUpdated() {
            updated++;
            return this;
        }

        public Builder incrementSkipped() {
            skipped++;
            return this;
        }

        public Builder addError(String error) {
            errors.add(error);
            return this;
        }

        public Builder addWarning(String warning) {
            warnings.add(warning);
            return this;
        }

        public ResearchGroupImportResultDTO build() {
            return new ResearchGroupImportResultDTO(created, updated, skipped, List.copyOf(errors), List.copyOf(warnings));
        }
    }
}
