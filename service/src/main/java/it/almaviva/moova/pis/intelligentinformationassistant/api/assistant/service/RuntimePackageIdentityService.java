package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimePackageRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class RuntimePackageIdentityService {

    private static final String PACKAGE_SCHEMA_VERSION = "iia.runtime-agent-submission/v1";
    private static final String CANONICALIZATION = "RFC8785_JSON";
    private static final String HASH_ALGORITHM = "SHA-256";

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Inject
    AgentRuntimePackageRepository runtimePackageRepository;

    @Inject
    AgentActivationSnapshotLoader snapshotLoader;

    @Inject
    AgentActivationPreconditionValidator preconditionValidator;

    @Inject
    RuntimeAgentPackageFactory runtimeAgentPackageFactory;

    @Inject
    RuntimeAgentPackageCanonicalIdentity packageIdentity;

    @Inject
    EntityManager entityManager;

    @ConfigProperty(
            name = "iia.agent-activation.fallback-submitted-by",
            defaultValue = "pis-intelligentinformationassistant-api-assistant")
    String fallbackSubmittedBy;

    Clock clock = Clock.systemUTC();

    public RuntimePackageIdentityService() {
    }

    RuntimePackageIdentityService(
            AgentDefinitionRepository agentDefinitionRepository,
            AgentRuntimePackageRepository runtimePackageRepository,
            AgentActivationSnapshotLoader snapshotLoader,
            AgentActivationPreconditionValidator preconditionValidator,
            RuntimeAgentPackageFactory runtimeAgentPackageFactory,
            EntityManager entityManager,
            Clock clock,
            String fallbackSubmittedBy) {
        this.agentDefinitionRepository = agentDefinitionRepository;
        this.runtimePackageRepository = runtimePackageRepository;
        this.snapshotLoader = snapshotLoader;
        this.preconditionValidator = preconditionValidator;
        this.runtimeAgentPackageFactory = runtimeAgentPackageFactory;
        this.packageIdentity = new RuntimeAgentPackageCanonicalIdentity();
        this.entityManager = entityManager;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.fallbackSubmittedBy = fallbackSubmittedBy;
    }

    @Transactional
    public AgentRuntimePackage materializeOrReuse(String agentDefinitionId, AgentActivationCommand command) {
        return materializeOrReuseWithDecision(agentDefinitionId, command).runtimePackage();
    }

    @Transactional
    public RuntimePackageMaterialization materializeOrReuseWithDecision(String agentDefinitionId, AgentActivationCommand command) {
        System.out.println("[IIA][RUNTIME_PACKAGE_IDENTITY] materialize start agentDefinitionId=" + agentDefinitionId);
        AgentDefinition lockedDefinition = agentDefinitionRepository.findByDefinitionIdForUpdate(agentDefinitionId)
                .orElseThrow(() -> new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));
        AgentActivationSnapshot snapshot = snapshotLoader.load(agentDefinitionId)
                .orElseThrow(() -> new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));
        validate(snapshot);

        long provisionalVersion = Math.max(1L, runtimePackageRepository.findMaximumPackageVersion(agentDefinitionId) + 1L);
        RuntimeAgentPackageBuild build = buildPackage(snapshot, command, provisionalVersion);
        AgentRuntimePackage existing = runtimePackageRepository
                .findByAgentDefinitionAndFingerprint(agentDefinitionId, build.packageFingerprint())
                .orElse(null);
        if (existing != null) {
            packageIdentity.validate(existing);
            System.out.println("[IIA][RUNTIME_PACKAGE_IDENTITY] decision=REUSED agentDefinitionId=" + agentDefinitionId
                    + " packageVersion=" + existing.getNumPackageversion());
            return new RuntimePackageMaterialization(existing, false);
        }

        RuntimeAgentPackageBuild finalBuild = build;
        long packageVersion = provisionalVersion;
        Instant packageCreatedAt = finalBuild.submission().submittedAt();
        AgentRuntimePackage runtimePackage = AgentRuntimePackage.create(
                lockedDefinition,
                packageVersion,
                finalBuild.submission().submissionId(),
                finalBuild.packageFingerprint(),
                finalBuild.submission().agentDefinition().artifact().hash(),
                compilationReference(snapshot),
                PACKAGE_SCHEMA_VERSION,
                CANONICALIZATION,
                HASH_ALGORITHM,
                finalBuild.runtimePackageJson(),
                snapshot.updatedAt(),
                packageCreatedAt,
                OffsetDateTime.ofInstant(packageCreatedAt, ZoneOffset.UTC),
                submittedBy());
        packageIdentity.validate(runtimePackage);
        runtimePackageRepository.persistImmutablePackage(runtimePackage);
        System.out.println("[IIA][RUNTIME_PACKAGE_IDENTITY] decision=CREATED agentDefinitionId=" + agentDefinitionId
                + " packageVersion=" + runtimePackage.getNumPackageversion());
        return new RuntimePackageMaterialization(runtimePackage, true);
    }

    private RuntimeAgentPackageBuild buildPackage(
            AgentActivationSnapshot snapshot,
            AgentActivationCommand command,
            long packageVersion) {
        try {
            AgentRuntimePackageBuildContext context = new AgentRuntimePackageBuildContext(
                    packageVersion,
                    Instant.now(clock),
                    submittedBy());
            return runtimeAgentPackageFactory.build(snapshot, command, context);
        } catch (AgentRuntimePackageBuildException ex) {
            throw new AgentActivationPreconditionFailedException(List.of(
                    new AgentActivationPreconditionViolation(
                            AgentActivationPreconditionCode.RUNTIME_CONTRACT_INVALID,
                            "runtimePackage",
                            sanitize(ex.getMessage()))));
        } catch (AgentActivationPreconditionFailedException | AgentActivationTechnicalException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AgentActivationTechnicalException("Runtime package construction failed.", ex);
        }
    }

    private void validate(AgentActivationSnapshot snapshot) {
        AgentActivationPreconditionValidationResult preconditions = preconditionValidator.validate(snapshot);
        if (!preconditions.valid()) {
            throw new AgentActivationPreconditionFailedException(preconditions.errors());
        }
    }

    private AgentCompilation compilationReference(AgentActivationSnapshot snapshot) {
        String compilationId = snapshot.latestCompilation() == null ? null : snapshot.latestCompilation().compilationId();
        return compilationId == null ? null : entityManager.getReference(AgentCompilation.class, compilationId);
    }

    private String submittedBy() {
        if (fallbackSubmittedBy == null || fallbackSubmittedBy.isBlank()) {
            throw new AgentActivationTechnicalException("Activation submittedBy fallback is not configured.");
        }
        return fallbackSubmittedBy.trim();
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return "Runtime package data is invalid.";
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    public record RuntimePackageMaterialization(
            AgentRuntimePackage runtimePackage,
            boolean created) {
    }
}
