package de.tum.cit.aet.usermanagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "research_group_aliases")
public class ResearchGroupAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alias_id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_group_id", nullable = false)
    private ResearchGroup researchGroup;

    @NotBlank
    @Column(name = "alias_pattern", nullable = false)
    private String aliasPattern;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 50)
    private MatchType matchType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum MatchType {
        EXACT,
        CONTAINS,
        PREFIX
    }
}
