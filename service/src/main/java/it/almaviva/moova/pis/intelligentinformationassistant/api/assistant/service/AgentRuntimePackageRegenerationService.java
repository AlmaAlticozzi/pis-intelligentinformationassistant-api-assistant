package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimePackageRegenerationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimePackageRegenerationResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimePackageRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class AgentRuntimePackageRegenerationService {

    @Inject AgentDefinitionRepository agentDefinitionRepository;
    @Inject AgentRuntimePackageRepository runtimePackageRepository;
    @Inject AgentActivationSnapshotLoader snapshotLoader;
    @Inject AgentCompilationPreconditionValidator compilationPreconditionValidator;
    @Inject AgentDslArtifactBuilder dslArtifactBuilder;
    @Inject AgentArtifactHashService artifactHashService;
    @Inject AgentDslRuntimeCompatibilityValidator runtimeCompatibilityValidator;
    @Inject RuntimePackageIdentityService runtimePackageIdentityService;
    @Inject RuntimeCatalogLifecyclePublisher runtimeCatalogLifecyclePublisher;

    Clock clock = Clock.systemUTC();

    @Transactional
    public AgentRuntimePackageRegenerationResponse regenerate(
            String agentDefinitionId,
            AgentRuntimePackageRegenerationRequest request) {
        String id = normalizedId(agentDefinitionId);
        AgentDefinition definition = agentDefinitionRepository.findByDefinitionIdForUpdate(id)
                .orElseThrow(() -> new AgentDefinitionNotFoundException(
                        "agentDefinitionId", "Agent Definition not found."));
        String status = definition.getSglStatus() == null ? null : definition.getSglStatus().getSglStatus();
        if (!"ACTIVE".equals(status)) {
            throw new AgentRuntimePackageRegenerationRejectedException(
                    "Runtime package regeneration requires an ACTIVE Agent Definition; current status is " + status + ".");
        }

        AgentRuntimePackage oldPackage = runtimePackageRepository.findCurrentByAgentDefinition(id)
                .orElseThrow(() -> new AgentRuntimePackageRegenerationRejectedException(
                        "ACTIVE Agent Definition has no current Runtime Agent Package."));
        AgentActivationSnapshot source = snapshotLoader.load(id)
                .orElseThrow(() -> new AgentDefinitionNotFoundException(
                        "agentDefinitionId", "Agent Definition not found."));
        requireReadyCompilation(source);

        AgentCompilationPreconditionValidationResult compilationValidation =
                compilationPreconditionValidator.validate(definition, "DSL");
        if (!compilationValidation.valid()) {
            throw new AgentActivationPreconditionFailedException(compilationValidation.errors().stream()
                    .map(message -> new AgentActivationPreconditionViolation(
                            AgentActivationPreconditionCode.RUNTIME_CONTRACT_INVALID, "compilation", message))
                    .toList());
        }

        OffsetDateTime deterministicCreatedAt = deterministicArtifactTime(source);
        AgentDslArtifactBuildResult artifactBuild = "SCHEDULED_INTERPRETER".equals(compilationValidation.interpreterType())
                ? dslArtifactBuilder.buildScheduledArtifact(definition, compilationValidation, deterministicCreatedAt)
                : dslArtifactBuilder.buildEventArtifact(definition, compilationValidation, deterministicCreatedAt);
        if (!artifactBuild.success()) {
            throw new AgentActivationPreconditionFailedException(List.of(new AgentActivationPreconditionViolation(
                    AgentActivationPreconditionCode.DSL_ARTIFACT_INVALID,
                    "latestCompilation.resultJson.dslArtifact",
                    artifactBuild.errorMessage())));
        }
        AgentDslRuntimeCompatibilityValidationResult compatibility =
                runtimeCompatibilityValidator.validate(artifactBuild.artifact().artifact());
        if (!compatibility.compatible()) {
            throw new AgentActivationPreconditionFailedException(compatibility.errors().stream()
                    .map(message -> new AgentActivationPreconditionViolation(
                            AgentActivationPreconditionCode.DSL_RUNTIME_COMPATIBILITY_FAILED,
                            "latestCompilation.resultJson.dslArtifact", message))
                    .toList());
        }

        String artifactHash = artifactHashService.hashDslArtifact(
                id, source.latestCompilation().compilationId(), artifactBuild.artifact().artifact()).artifactHash();
        AgentActivationSnapshot regenerated = source.withRegeneratedDslArtifact(
                artifactBuild.artifact().artifact(), artifactHash);
        AgentActivationCommand command = new AgentActivationCommand(
                id, request == null ? null : request.getNote(), true);
        RuntimePackageIdentityService.RuntimePackageMaterialization materialization =
                runtimePackageIdentityService.materializeForCurrentReplacement(
                        id, command, regenerated, oldPackage.getCodRuntimepackage());
        AgentRuntimePackage currentPackage = materialization.runtimePackage();

        boolean promoted = !currentPackage.getCodRuntimepackage().equals(oldPackage.getCodRuntimepackage());
        if (promoted) {
            OffsetDateTime promotedAt = OffsetDateTime.now(clock.withZone(ZoneOffset.UTC));
            definition.setCodCurrentruntimepackage(currentPackage.getCodRuntimepackage());
            definition.setDtUpdatedat(promotedAt);
            runtimeCatalogLifecyclePublisher.appendUpsert(id, currentPackage, promotedAt);
        }
        return new AgentRuntimePackageRegenerationResponse()
                .agentDefinitionId(id)
                .oldPackageVersion(oldPackage.getNumPackageversion())
                .currentPackageVersion(currentPackage.getNumPackageversion())
                .artifactHash(currentPackage.getDscArtifacthash())
                .packageFingerprint(currentPackage.getDscPackagefingerprint())
                .submissionId(currentPackage.getCodSubmissionid())
                .status(AgentRuntimePackageRegenerationResponse.StatusEnum.ACTIVE)
                .packageCreated(materialization.created())
                .currentPackageReused(!promoted);
    }

    private void requireReadyCompilation(AgentActivationSnapshot snapshot) {
        if (snapshot.latestCompilation() == null
                || !"READY".equals(snapshot.latestCompilation().status())
                || !"READY".equals(snapshot.latestCompilation().currentStep())
                || snapshot.latestCompilation().resultJson() == null
                || snapshot.artifact() == null) {
            throw new AgentRuntimePackageRegenerationRejectedException(
                    "The latest governed compilation must be completed in READY state.");
        }
    }

    private OffsetDateTime deterministicArtifactTime(AgentActivationSnapshot source) {
        if (source.latestCompilation().startedAt() != null) {
            return source.latestCompilation().startedAt();
        }
        if (source.latestCompilation().completedAt() != null) {
            return source.latestCompilation().completedAt();
        }
        if (source.updatedAt() != null) {
            return source.updatedAt();
        }
        return OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    }

    private String normalizedId(String agentDefinitionId) {
        if (agentDefinitionId == null || agentDefinitionId.isBlank()) {
            throw new AgentDefinitionInvalidRequestException(
                    "agentDefinitionId", "The agentDefinitionId path parameter is required.");
        }
        String id = agentDefinitionId.trim();
        if (id.length() > 50) {
            throw new AgentDefinitionInvalidRequestException(
                    "agentDefinitionId", "The agentDefinitionId path parameter exceeds 50 characters.");
        }
        return id;
    }
}
