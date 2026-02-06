package de.tum.cit.aet.positions.web;

import de.tum.cit.aet.core.security.CurrentUserProvider;
import de.tum.cit.aet.positions.dto.PositionFinderRequestDTO;
import de.tum.cit.aet.positions.dto.PositionFinderResponseDTO;
import de.tum.cit.aet.positions.repository.PositionRepository;
import de.tum.cit.aet.positions.service.PositionFinderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v2/position-finder")
@RequiredArgsConstructor
public class PositionFinderResource {

    private final PositionFinderService positionFinderService;
    private final PositionRepository positionRepository;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Searches for positions matching the given criteria.
     * Requires one of the roles: admin, job_manager.
     *
     * @param request the search criteria
     * @return matching positions with scores
     */
    @PostMapping("/search")
    public ResponseEntity<PositionFinderResponseDTO> searchPositions(
            @RequestBody PositionFinderRequestDTO request) {

        if (!currentUserProvider.isJobManager() && !currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        log.info("Position finder search: grade={}, percentage={}%, dates={} to {}, relevanceTypes={}",
                request.employeeGrade(),
                request.fillPercentageOrDefault(),
                request.startDate(),
                request.endDate(),
                request.relevanceTypes());

        PositionFinderResponseDTO response = positionFinderService.findPositions(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Returns all distinct relevance types for positions.
     * Requires one of the roles: admin, job_manager.
     *
     * @return list of distinct relevance types
     */
    @GetMapping("/relevance-types")
    public ResponseEntity<List<String>> getRelevanceTypes() {
        if (!currentUserProvider.isJobManager() && !currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        List<String> relevanceTypes = positionRepository.findDistinctRelevanceTypes();
        return ResponseEntity.ok(relevanceTypes);
    }
}
