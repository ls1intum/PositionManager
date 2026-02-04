package de.tum.cit.aet.usermanagement.service;

import de.tum.cit.aet.usermanagement.domain.ResearchGroup;
import de.tum.cit.aet.usermanagement.domain.User;
import de.tum.cit.aet.usermanagement.repository.ResearchGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for automatically assigning professors to their research groups on login.
 * Matches are based on exact (case-insensitive) first name and last name comparison
 * between the user and the professor name fields in research groups.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessorLoginMatchingService {

    private final ResearchGroupRepository researchGroupRepository;

    /**
     * Attempts to match a professor user to their research group and assign them as head.
     * Only runs for users with the professor role who are not already a head of a research group.
     *
     * @param user the user to match (should have professor role)
     */
    @Transactional
    public void matchProfessorToResearchGroup(User user) {
        if (user == null) {
            return;
        }

        // Only process users with professor role
        if (!user.hasAnyGroup("professor")) {
            return;
        }

        // Check if user is already a head of any research group
        // This is determined by checking if any research group has this user as head
        // The relationship is OneToOne with unique constraint
        if (isAlreadyHead(user)) {
            log.debug("User {} is already head of a research group, skipping matching", user.getUniversityId());
            return;
        }

        // Need both first and last name for matching
        if (user.getFirstName() == null || user.getLastName() == null) {
            log.debug("User {} has incomplete name, skipping matching", user.getUniversityId());
            return;
        }

        // Try to find matching research group
        Optional<ResearchGroup> matchingGroup = researchGroupRepository
                .findByProfessorNameIgnoreCase(user.getFirstName(), user.getLastName());

        if (matchingGroup.isEmpty()) {
            log.debug("No matching research group found for professor: {} {}",
                    user.getFirstName(), user.getLastName());
            return;
        }

        ResearchGroup group = matchingGroup.get();

        // Only assign if the group doesn't already have a head
        if (group.getHead() != null) {
            log.debug("Research group '{}' already has a head: {}",
                    group.getName(), group.getHead().getUniversityId());
            return;
        }

        // Assign user as head
        group.setHead(user);
        user.setResearchGroup(group);
        researchGroupRepository.save(group);

        log.info("Auto-assigned professor {} {} as head of research group '{}'",
                user.getFirstName(), user.getLastName(), group.getName());
    }

    private boolean isAlreadyHead(User user) {
        // Check all research groups to see if this user is already a head
        return researchGroupRepository.findAllByArchivedFalseOrderByNameAsc().stream()
                .anyMatch(rg -> rg.getHead() != null && rg.getHead().getId().equals(user.getId()));
    }
}
