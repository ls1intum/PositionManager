package de.tum.cit.aet.usermanagement.web;

import de.tum.cit.aet.core.security.CurrentUserProvider;
import de.tum.cit.aet.usermanagement.dto.ResearchGroupDTO;
import de.tum.cit.aet.usermanagement.dto.ResearchGroupImportResultDTO;
import de.tum.cit.aet.usermanagement.service.ResearchGroupMatchingService;
import de.tum.cit.aet.usermanagement.service.ResearchGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v2/research-groups")
@RequiredArgsConstructor
public class ResearchGroupResource {

    private final ResearchGroupService researchGroupService;
    private final ResearchGroupMatchingService researchGroupMatchingService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Returns all research groups, optionally filtered by search term.
     * Admin only.
     *
     * @param search optional search term to filter by name, abbreviation, professor name, or department
     * @return list of research groups
     */
    @GetMapping
    public ResponseEntity<List<ResearchGroupDTO>> getResearchGroups(
            @RequestParam(required = false) String search) {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(researchGroupService.searchResearchGroups(search.trim()));
        }
        return ResponseEntity.ok(researchGroupService.getAllResearchGroups());
    }

    /**
     * Returns a single research group by ID.
     * Admin only.
     *
     * @param id the research group ID
     * @return the research group
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResearchGroupDTO> getResearchGroup(@PathVariable UUID id) {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(researchGroupService.getResearchGroup(id));
    }

    /**
     * Returns research groups without a head assigned.
     * Admin only.
     *
     * @return list of research groups without head
     */
    @GetMapping("/without-head")
    public ResponseEntity<List<ResearchGroupDTO>> getResearchGroupsWithoutHead() {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(researchGroupService.getResearchGroupsWithoutHead());
    }

    /**
     * Creates a new research group.
     * Admin only.
     *
     * @param dto the research group data
     * @return the created research group
     */
    @PostMapping
    public ResponseEntity<ResearchGroupDTO> createResearchGroup(@RequestBody ResearchGroupDTO dto) {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        ResearchGroupDTO created = researchGroupService.createResearchGroup(dto);
        return ResponseEntity.created(URI.create("/v2/research-groups/" + created.id())).body(created);
    }

    /**
     * Updates an existing research group.
     * Admin only.
     *
     * @param id the research group ID
     * @param dto the updated research group data
     * @return the updated research group
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResearchGroupDTO> updateResearchGroup(
            @PathVariable UUID id,
            @RequestBody ResearchGroupDTO dto) {

        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(researchGroupService.updateResearchGroup(id, dto));
    }

    /**
     * Archives a research group (soft delete).
     * Admin only.
     *
     * @param id the research group ID
     * @return empty response on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveResearchGroup(@PathVariable UUID id) {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        researchGroupService.archiveResearchGroup(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Imports research groups from a CSV file.
     * CSV format: firstName,lastName,groupName,abbreviation,department
     * Admin only.
     *
     * @param file the CSV file
     * @return import result with counts and errors
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResearchGroupImportResultDTO> importResearchGroups(@RequestParam("file") MultipartFile file) {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ResearchGroupImportResultDTO.empty());
        }

        return ResponseEntity.ok(researchGroupService.importFromCsv(file));
    }

    /**
     * Batch assigns research groups to positions based on organization unit fuzzy matching.
     * Admin only.
     *
     * @return batch assign result with matched positions and unmatched org units
     */
    @PostMapping("/batch-assign-positions")
    public ResponseEntity<ResearchGroupMatchingService.BatchAssignResult> batchAssignPositions() {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(researchGroupMatchingService.batchAssignPositions());
    }

    /**
     * Deletes all research groups.
     * Admin only. This is a destructive operation.
     *
     * @return the number of deleted research groups
     */
    @DeleteMapping
    public ResponseEntity<DeleteAllResult> deleteAllResearchGroups() {
        if (!currentUserProvider.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        int count = researchGroupService.deleteAll();
        return ResponseEntity.ok(new DeleteAllResult(count));
    }

    /**
     * Result of deleting all research groups.
     */
    public record DeleteAllResult(int deleted) {}
}
