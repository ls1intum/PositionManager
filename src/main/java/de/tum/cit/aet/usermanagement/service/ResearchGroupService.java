package de.tum.cit.aet.usermanagement.service;

import de.tum.cit.aet.usermanagement.domain.ResearchGroup;
import de.tum.cit.aet.usermanagement.domain.ResearchGroupAlias;
import de.tum.cit.aet.usermanagement.dto.ResearchGroupDTO;
import de.tum.cit.aet.usermanagement.dto.ResearchGroupImportResultDTO;
import de.tum.cit.aet.usermanagement.repository.ResearchGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchGroupService {

    private final ResearchGroupRepository researchGroupRepository;

    /**
     * Returns all research groups (not archived).
     *
     * @return list of research group DTOs
     */
    @Transactional(readOnly = true)
    public List<ResearchGroupDTO> getAllResearchGroups() {
        return researchGroupRepository.findAllWithAliasesNotArchived()
                .stream()
                .map(rg -> ResearchGroupDTO.fromEntity(rg, researchGroupRepository.countPositionsByResearchGroupId(rg.getId())))
                .toList();
    }

    /**
     * Returns a research group by ID.
     *
     * @param id the research group ID
     * @return the research group DTO
     */
    @Transactional(readOnly = true)
    public ResearchGroupDTO getResearchGroup(UUID id) {
        ResearchGroup researchGroup = researchGroupRepository.findByIdWithAliases(id)
                .orElseThrow(() -> new IllegalArgumentException("Research group not found: " + id));
        int positionCount = researchGroupRepository.countPositionsByResearchGroupId(id);
        return ResearchGroupDTO.fromEntity(researchGroup, positionCount);
    }

    /**
     * Returns a research group entity by ID.
     *
     * @param id the research group ID
     * @return optional containing the research group entity
     */
    @Transactional(readOnly = true)
    public Optional<ResearchGroup> findById(UUID id) {
        return researchGroupRepository.findById(id);
    }

    /**
     * Returns research groups without a head assigned.
     *
     * @return list of research group DTOs without head
     */
    @Transactional(readOnly = true)
    public List<ResearchGroupDTO> getResearchGroupsWithoutHead() {
        return researchGroupRepository.findByHeadIsNullAndArchivedFalse()
                .stream()
                .map(ResearchGroupDTO::fromEntity)
                .toList();
    }

    /**
     * Creates a new research group.
     *
     * @param dto the research group data
     * @return the created research group DTO
     */
    @Transactional
    public ResearchGroupDTO createResearchGroup(ResearchGroupDTO dto) {
        if (researchGroupRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Research group with name already exists: " + dto.name());
        }
        if (researchGroupRepository.existsByAbbreviation(dto.abbreviation())) {
            throw new IllegalArgumentException("Research group with abbreviation already exists: " + dto.abbreviation());
        }

        ResearchGroup researchGroup = new ResearchGroup();
        updateEntityFromDto(researchGroup, dto);
        researchGroup = researchGroupRepository.save(researchGroup);
        log.info("Created research group: {}", researchGroup.getName());
        return ResearchGroupDTO.fromEntity(researchGroup);
    }

    /**
     * Updates an existing research group.
     *
     * @param id the research group ID
     * @param dto the updated research group data
     * @return the updated research group DTO
     */
    @Transactional
    public ResearchGroupDTO updateResearchGroup(UUID id, ResearchGroupDTO dto) {
        ResearchGroup researchGroup = researchGroupRepository.findByIdWithAliases(id)
                .orElseThrow(() -> new IllegalArgumentException("Research group not found: " + id));

        // Check if name is being changed to an existing one
        if (!researchGroup.getName().equals(dto.name()) && researchGroupRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Research group with name already exists: " + dto.name());
        }

        // Check if abbreviation is being changed to an existing one
        if (!researchGroup.getAbbreviation().equals(dto.abbreviation()) && researchGroupRepository.existsByAbbreviation(dto.abbreviation())) {
            throw new IllegalArgumentException("Research group with abbreviation already exists: " + dto.abbreviation());
        }

        updateEntityFromDto(researchGroup, dto);
        researchGroup = researchGroupRepository.save(researchGroup);
        log.info("Updated research group: {}", researchGroup.getName());
        return ResearchGroupDTO.fromEntity(researchGroup, researchGroupRepository.countPositionsByResearchGroupId(id));
    }

    /**
     * Archives a research group (soft delete).
     *
     * @param id the research group ID
     */
    @Transactional
    public void archiveResearchGroup(UUID id) {
        ResearchGroup researchGroup = researchGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Research group not found: " + id));
        researchGroup.setArchived(true);
        researchGroupRepository.save(researchGroup);
        log.info("Archived research group: {}", researchGroup.getName());
    }

    /**
     * Imports research groups from a CSV file.
     * CSV format: firstName,lastName,groupName,abbreviation,department
     *
     * @param file the CSV file to import
     * @return the import result with counts and errors
     */
    @Transactional
    public ResearchGroupImportResultDTO importFromCsv(MultipartFile file) {
        ResearchGroupImportResultDTO.Builder result = ResearchGroupImportResultDTO.builder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.addError("CSV file is empty");
                return result.build();
            }

            // Remove BOM if present
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            char delimiter = detectDelimiter(headerLine);
            String[] headers = parseCsvLine(headerLine, delimiter);
            Map<String, Integer> headerIndices = mapHeaderIndices(headers);

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] values = parseCsvLine(line, delimiter);
                    processImportRecord(values, headerIndices, result, lineNumber);
                } catch (Exception e) {
                    result.addError("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error parsing CSV file", e);
            result.addError("Failed to parse CSV file: " + e.getMessage());
        }

        ResearchGroupImportResultDTO importResult = result.build();
        log.info("CSV import completed: created={}, updated={}, skipped={}, errors={}",
                importResult.created(), importResult.updated(), importResult.skipped(), importResult.errors().size());
        return importResult;
    }

    private char detectDelimiter(String headerLine) {
        int semicolons = headerLine.length() - headerLine.replace(";", "").length();
        int commas = headerLine.length() - headerLine.replace(",", "").length();
        return semicolons > commas ? ';' : ',';
    }

    private String[] parseCsvLine(String line, char delimiter) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());

        return fields.toArray(new String[0]);
    }

    private Map<String, Integer> mapHeaderIndices(String[] headers) {
        Map<String, Integer> indices = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            indices.put(headers[i].trim().toLowerCase(), i);
        }
        return indices;
    }

    private void processImportRecord(String[] values, Map<String, Integer> headerIndices,
                                     ResearchGroupImportResultDTO.Builder result, int lineNumber) {
        String firstName = getValueByHeader(values, headerIndices, "firstname");
        String lastName = getValueByHeader(values, headerIndices, "lastname");
        String groupName = getValueByHeader(values, headerIndices, "groupname");
        String abbreviation = getValueByHeader(values, headerIndices, "abbreviation");
        String department = getValueByHeader(values, headerIndices, "department");

        if (groupName == null || groupName.isBlank()) {
            result.addError("Line " + lineNumber + ": groupName is required");
            result.incrementSkipped();
            return;
        }

        if (abbreviation == null || abbreviation.isBlank()) {
            result.addError("Line " + lineNumber + ": abbreviation is required");
            result.incrementSkipped();
            return;
        }

        Optional<ResearchGroup> existingByName = researchGroupRepository.findByName(groupName);
        Optional<ResearchGroup> existingByAbbr = researchGroupRepository.findByAbbreviation(abbreviation);

        if (existingByName.isPresent()) {
            // Update existing
            ResearchGroup existing = existingByName.get();
            if (!existing.getAbbreviation().equals(abbreviation) && existingByAbbr.isPresent()) {
                result.addWarning("Line " + lineNumber + ": Abbreviation conflict, skipping update for " + groupName);
                result.incrementSkipped();
                return;
            }
            existing.setAbbreviation(abbreviation);
            existing.setProfessorFirstName(firstName);
            existing.setProfessorLastName(lastName);
            existing.setDepartment(department);
            researchGroupRepository.save(existing);
            result.incrementUpdated();
        } else if (existingByAbbr.isPresent()) {
            result.addWarning("Line " + lineNumber + ": Abbreviation " + abbreviation + " already exists for different group");
            result.incrementSkipped();
        } else {
            // Create new
            ResearchGroup newGroup = new ResearchGroup();
            newGroup.setName(groupName);
            newGroup.setAbbreviation(abbreviation);
            newGroup.setProfessorFirstName(firstName);
            newGroup.setProfessorLastName(lastName);
            newGroup.setDepartment(department);
            researchGroupRepository.save(newGroup);
            result.incrementCreated();
        }
    }

    private String getValueByHeader(String[] values, Map<String, Integer> headerIndices, String header) {
        Integer index = headerIndices.get(header);
        if (index == null || index >= values.length) {
            return null;
        }
        String value = values[index].trim();
        return value.isEmpty() ? null : value;
    }

    private void updateEntityFromDto(ResearchGroup entity, ResearchGroupDTO dto) {
        entity.setName(dto.name());
        entity.setAbbreviation(dto.abbreviation());
        entity.setDescription(dto.description());
        entity.setWebsiteUrl(dto.websiteUrl());
        entity.setCampus(dto.campus());
        entity.setDepartment(dto.department());
        entity.setProfessorFirstName(dto.professorFirstName());
        entity.setProfessorLastName(dto.professorLastName());

        // Update aliases
        if (dto.aliases() != null) {
            entity.getAliases().clear();
            for (String alias : dto.aliases()) {
                ResearchGroupAlias aliasEntity = new ResearchGroupAlias();
                aliasEntity.setResearchGroup(entity);
                aliasEntity.setAliasPattern(alias);
                aliasEntity.setMatchType(ResearchGroupAlias.MatchType.CONTAINS);
                entity.getAliases().add(aliasEntity);
            }
        }
    }
}
