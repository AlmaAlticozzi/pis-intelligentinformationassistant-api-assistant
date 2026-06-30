package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeAgentSubmission;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogUpsertItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRow;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Locale;

@ApplicationScoped
public class DesiredRuntimeCatalogMapper {
    private static final String FINGERPRINT_PATTERN = "^[a-fA-F0-9]{64}$";

    @Inject ObjectMapper objectMapper;

    public DesiredRuntimeCatalogMapper() { }
    DesiredRuntimeCatalogMapper(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public DesiredRuntimeCatalogUpsertItem map(DesiredRuntimeCatalogRow row) {
        validateRow(row);
        DesiredRuntimeAgentSubmission submission;
        try {
            submission = objectMapper.convertValue(row.persistedRuntimePackage(), DesiredRuntimeAgentSubmission.class);
        } catch (IllegalArgumentException ex) {
            throw inconsistent(row, "RUNTIME_PACKAGE_JSON_INVALID");
        }
        validateSubmission(row, submission);
        return new DesiredRuntimeCatalogUpsertItem()
                .action(DesiredRuntimeCatalogUpsertItem.ActionEnum.UPSERT)
                .agentDefinitionId(row.agentDefinitionId())
                .sourceStatus(DesiredRuntimeCatalogUpsertItem.SourceStatusEnum.ACTIVE)
                .sourceUpdatedAt(row.sourceUpdatedAt())
                .packageFingerprint(row.catalogPackageFingerprint().toLowerCase(Locale.ROOT))
                .runtimePackage(submission);
    }

    private void validateRow(DesiredRuntimeCatalogRow row) {
        if (!"UPSERT".equals(row.action())) fail(row, "ACTION_INVALID");
        if (!"ACTIVE".equals(row.sourceAgentStatus())) fail(row, "SOURCE_STATUS_INVALID");
        if (blank(row.runtimePackageId())) fail(row, "RUNTIME_PACKAGE_MISSING");
        if (row.catalogPackageVersion() < 1) fail(row, "PACKAGE_VERSION_INVALID");
        if (row.catalogPackageFingerprint() == null
                || !row.catalogPackageFingerprint().matches(FINGERPRINT_PATTERN)) fail(row, "PACKAGE_FINGERPRINT_INVALID");
        if (!row.agentDefinitionId().equals(row.packageAgentDefinitionId())) fail(row, "AGENT_DEFINITION_MISMATCH");
        if (row.catalogPackageVersion() != row.persistedPackageVersion()) fail(row, "PACKAGE_VERSION_MISMATCH");
        if (!row.catalogPackageFingerprint().equalsIgnoreCase(row.persistedPackageFingerprint())) {
            fail(row, "PACKAGE_FINGERPRINT_MISMATCH");
        }
        if (row.persistedRuntimePackage() == null || row.persistedRuntimePackage().isEmpty()) {
            fail(row, "RUNTIME_PACKAGE_JSON_MISSING");
        }
    }

    private void validateSubmission(DesiredRuntimeCatalogRow row, DesiredRuntimeAgentSubmission value) {
        if (value == null || value.getDesiredStatus() != DesiredRuntimeAgentSubmission.DesiredStatusEnum.ACTIVE) {
            fail(row, "DESIRED_STATUS_INVALID");
        }
        if (value.getPackageVersion() == null || value.getPackageVersion() != row.persistedPackageVersion()) {
            fail(row, "JSON_PACKAGE_VERSION_MISMATCH");
        }
        if (!row.persistedSubmissionId().equals(value.getSubmissionId())) fail(row, "SUBMISSION_ID_MISMATCH");
        var definition = value.getAgentDefinition();
        if (definition == null || !row.agentDefinitionId().equals(definition.getId())) {
            fail(row, "JSON_AGENT_DEFINITION_MISMATCH");
        }
        if (definition.getSource() == null || definition.getProfile() == null
                || definition.getActivationPolicy() == null || definition.getRuntimeContract() == null
                || definition.getArtifact() == null || definition.getDataSourceBindings() == null
                || definition.getInterpreterType() == null || definition.getTriggerType() == null
                || blank(definition.getInputModel()) || blank(definition.getOutputModel())) {
            fail(row, "GOVERNED_RUNTIME_SECTION_MISSING");
        }
    }

    private void fail(DesiredRuntimeCatalogRow row, String reason) {
        throw inconsistent(row, reason);
    }

    private DesiredRuntimeCatalogConsistencyException inconsistent(DesiredRuntimeCatalogRow row, String reason) {
        System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][CONSISTENCY] agentDefinitionId=" + row.agentDefinitionId()
                + " runtimePackageId=" + row.runtimePackageId()
                + " catalogChangeSequence=" + row.catalogChangeSequence() + " valid=false reason=" + reason);
        return new DesiredRuntimeCatalogConsistencyException("Desired Runtime Catalog UPSERT is inconsistent: " + reason + ".");
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
