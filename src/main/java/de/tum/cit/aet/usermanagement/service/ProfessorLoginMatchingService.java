package de.tum.cit.aet.usermanagement.service;

import de.tum.cit.aet.usermanagement.domain.ResearchGroup;
import de.tum.cit.aet.usermanagement.domain.User;
import de.tum.cit.aet.usermanagement.domain.UserGroup;
import de.tum.cit.aet.usermanagement.domain.key.UserGroupId;
import de.tum.cit.aet.usermanagement.repository.ResearchGroupRepository;
import de.tum.cit.aet.usermanagement.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for automatically assigning professors to their research groups on login.
 * Matches are based on exact (case-insensitive) first name and last name comparison
 * between the user and the professor name fields in research groups.
 *
 * When a user logs in and their name matches a research group's professor name,
 * they are automatically assigned the professor role and set as head of that group.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessorLoginMatchingService {

    private static final String PROFESSOR_ROLE = "professor";

    private final ResearchGroupRepository researchGroupRepository;
    private final UserGroupRepository userGroupRepository;

    /**
     * Attempts to match a user to their research group based on name and assign them as head.
     * If a match is found, the user is automatically granted the professor role.
     *
     * @param user the user to match
     */
    @Transactional
    public void matchProfessorToResearchGroup(User user) {
        if (user == null) {
            return;
        }

        // Check if user is already a head of any research group
        if (isAlreadyHead(user)) {
            log.debug("User {} is already head of a research group, skipping matching", user.getUniversityId());
            return;
        }

        // Need both first and last name for matching
        if (user.getFirstName() == null || user.getLastName() == null) {
            log.debug("User {} has incomplete name, skipping matching", user.getUniversityId());
            return;
        }

        // Try to find matching research group by professor name
        Optional<ResearchGroup> matchingGroup = researchGroupRepository
                .findByProfessorNameIgnoreCase(user.getFirstName(), user.getLastName());

        if (matchingGroup.isEmpty()) {
            log.debug("No matching research group found for user: {} {}",
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

        // Assign professor role if user doesn't have it
        if (!user.hasAnyGroup(PROFESSOR_ROLE)) {
            assignProfessorRole(user);
        }

        // Assign user as head
        group.setHead(user);
        user.setResearchGroup(group);
        researchGroupRepository.save(group);

        log.info("Auto-assigned {} {} as head of research group '{}' with professor role",
                user.getFirstName(), user.getLastName(), group.getName());
    }

    private void assignProfessorRole(User user) {
        UserGroupId groupId = new UserGroupId();
        groupId.setUserId(user.getId());
        groupId.setRole(PROFESSOR_ROLE);

        UserGroup userGroup = new UserGroup();
        userGroup.setId(groupId);
        userGroup.setUser(user);

        userGroupRepository.save(userGroup);
        user.getGroups().add(userGroup);

        log.info("Auto-assigned professor role to user {} {} based on research group match",
                user.getFirstName(), user.getLastName());
    }

    private boolean isAlreadyHead(User user) {
        return researchGroupRepository.existsByHeadId(user.getId());
    }
}
