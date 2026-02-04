package de.tum.cit.aet.usermanagement.repository;

import de.tum.cit.aet.usermanagement.domain.ResearchGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResearchGroupRepository extends JpaRepository<ResearchGroup, UUID> {

    Optional<ResearchGroup> findByName(String name);

    Optional<ResearchGroup> findByAbbreviation(String abbreviation);

    List<ResearchGroup> findAllByArchivedFalseOrderByNameAsc();

    List<ResearchGroup> findByHeadIsNullAndArchivedFalse();

    @Query("SELECT rg FROM ResearchGroup rg WHERE rg.archived = false AND rg.professorLastName = :lastName AND rg.professorFirstName = :firstName")
    Optional<ResearchGroup> findByProfessorName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    @Query("SELECT rg FROM ResearchGroup rg WHERE rg.archived = false AND LOWER(rg.professorLastName) = LOWER(:lastName) AND LOWER(rg.professorFirstName) = LOWER(:firstName)")
    Optional<ResearchGroup> findByProfessorNameIgnoreCase(@Param("firstName") String firstName, @Param("lastName") String lastName);

    @Query("SELECT rg FROM ResearchGroup rg LEFT JOIN FETCH rg.aliases WHERE rg.id = :id")
    Optional<ResearchGroup> findByIdWithAliases(@Param("id") UUID id);

    @Query("SELECT DISTINCT rg FROM ResearchGroup rg LEFT JOIN FETCH rg.aliases WHERE rg.archived = false ORDER BY rg.name")
    List<ResearchGroup> findAllWithAliasesNotArchived();

    @Query("""
            SELECT COUNT(p) FROM Position p
            WHERE p.researchGroup.id = :researchGroupId
            """)
    int countPositionsByResearchGroupId(@Param("researchGroupId") UUID researchGroupId);

    boolean existsByName(String name);

    boolean existsByAbbreviation(String abbreviation);
}
