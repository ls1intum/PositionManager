package de.tum.cit.aet.usermanagement.repository;

import de.tum.cit.aet.usermanagement.domain.ResearchGroupAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResearchGroupAliasRepository extends JpaRepository<ResearchGroupAlias, UUID> {

    List<ResearchGroupAlias> findByResearchGroupId(UUID researchGroupId);

    void deleteByResearchGroupId(UUID researchGroupId);

    @Query("SELECT a FROM ResearchGroupAlias a WHERE LOWER(a.aliasPattern) = LOWER(:pattern) AND a.matchType = 'EXACT'")
    List<ResearchGroupAlias> findByExactPattern(@Param("pattern") String pattern);

    @Query("SELECT a FROM ResearchGroupAlias a WHERE LOWER(:input) LIKE CONCAT('%', LOWER(a.aliasPattern), '%') AND a.matchType = 'CONTAINS'")
    List<ResearchGroupAlias> findByContainsPattern(@Param("input") String input);

    @Query("SELECT a FROM ResearchGroupAlias a WHERE LOWER(:input) LIKE CONCAT(LOWER(a.aliasPattern), '%') AND a.matchType = 'PREFIX'")
    List<ResearchGroupAlias> findByPrefixPattern(@Param("input") String input);
}
