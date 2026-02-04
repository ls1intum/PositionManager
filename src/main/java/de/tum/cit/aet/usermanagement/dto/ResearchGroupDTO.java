package de.tum.cit.aet.usermanagement.dto;

import de.tum.cit.aet.usermanagement.domain.ResearchGroup;
import de.tum.cit.aet.usermanagement.domain.ResearchGroupAlias;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResearchGroupDTO(
        UUID id,
        String name,
        String abbreviation,
        String description,
        String websiteUrl,
        String campus,
        String department,
        String professorFirstName,
        String professorLastName,
        boolean archived,
        UserSummaryDTO head,
        List<String> aliases,
        int positionCount,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Creates a ResearchGroupDTO from a ResearchGroup entity.
     */
    public static ResearchGroupDTO fromEntity(ResearchGroup entity, int positionCount) {
        List<String> aliasPatterns = entity.getAliases() != null
                ? entity.getAliases().stream()
                        .map(ResearchGroupAlias::getAliasPattern)
                        .toList()
                : List.of();

        return new ResearchGroupDTO(
                entity.getId(),
                entity.getName(),
                entity.getAbbreviation(),
                entity.getDescription(),
                entity.getWebsiteUrl(),
                entity.getCampus(),
                entity.getDepartment(),
                entity.getProfessorFirstName(),
                entity.getProfessorLastName(),
                entity.isArchived(),
                UserSummaryDTO.fromEntity(entity.getHead()),
                aliasPatterns,
                positionCount,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Creates a ResearchGroupDTO from a ResearchGroup entity without position count.
     */
    public static ResearchGroupDTO fromEntity(ResearchGroup entity) {
        return fromEntity(entity, 0);
    }
}
