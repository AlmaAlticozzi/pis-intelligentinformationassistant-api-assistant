package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimePackageRegenerationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimePackageRegenerationResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimePackageRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentRuntimePackageRegenerationServiceTest {

    private static final String AGENT_ID = "AGDFB30E5AEFE0274025BA0FD07E29A980C0";
    private static final String OLD_HASH = "a".repeat(64);
    private static final String NEW_HASH = "b".repeat(64);
    private static final String OLD_SIGNED_HASH = "sha256:" + OLD_HASH;
    private static final String NEW_SIGNED_HASH = "sha256:" + NEW_HASH;

    private AgentDefinition definition;
    private AgentDefinitionRepository definitionRepository;
    private AgentRuntimePackageRepository packageRepository;
    private AgentActivationSnapshotLoader snapshotLoader;
    private AgentCompilationPreconditionValidator compilationValidator;
    private AgentDslArtifactBuilder dslBuilder;
    private AgentArtifactHashService hashService;
    private AgentDslRuntimeCompatibilityValidator compatibilityValidator;
    private RuntimePackageIdentityService identityService;
    private RuntimeCatalogLifecyclePublisher catalogPublisher;
    private AgentRuntimePackage oldPackage;
    private AgentRuntimePackage correctedPackage;
    private AgentRuntimePackageRegenerationService service;

    @BeforeEach
    void setUp() {
        definition = definition("ACTIVE");
        definition.setCodCurrentruntimepackage("RTPK2");
        definitionRepository = mock(AgentDefinitionRepository.class);
        packageRepository = mock(AgentRuntimePackageRepository.class);
        snapshotLoader = mock(AgentActivationSnapshotLoader.class);
        compilationValidator = mock(AgentCompilationPreconditionValidator.class);
        dslBuilder = mock(AgentDslArtifactBuilder.class);
        hashService = mock(AgentArtifactHashService.class);
        compatibilityValidator = mock(AgentDslRuntimeCompatibilityValidator.class);
        identityService = mock(RuntimePackageIdentityService.class);
        catalogPublisher = mock(RuntimeCatalogLifecyclePublisher.class);
        oldPackage = runtimePackage("RTPK2", 2L, OLD_HASH, "a".repeat(64),
                "ACTIVATE:" + AGENT_ID + ":e702b55a9c9b646e");
        correctedPackage = runtimePackage("RTPK3", 3L, NEW_HASH, "b".repeat(64),
                "ACTIVATE:" + AGENT_ID + ":bbbbbbbbbbbbbbbb");

        when(definitionRepository.findByDefinitionIdForUpdate(AGENT_ID)).thenReturn(Optional.of(definition));
        when(packageRepository.findCurrentByAgentDefinition(AGENT_ID)).thenReturn(Optional.of(oldPackage));
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(sourceSnapshot()));
        when(compilationValidator.validate(definition, "DSL")).thenReturn(validCompilation());
        when(dslBuilder.buildEventArtifact(eq(definition), any(), any())).thenReturn(
                AgentDslArtifactBuildResult.success(new AgentDslArtifact(
                        "iia.agent.dsl/v1", "DSL", AGENT_ID, "EVENT_INTERPRETER", "EVENT",
                        "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION", "STATELESS_EVENT_MATCH",
                        correctedDsl(), Map.of())));
        when(compatibilityValidator.validate(anyMap())).thenReturn(
                new AgentDslRuntimeCompatibilityValidationResult(
                        true, List.of(), List.of(), "iia.agent.dsl/v1", "DSL", "EVENT_INTERPRETER",
                        "EVENT", "KAFKA_EVENT", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION",
                        "STATELESS_EVENT_MATCH", Map.of()));
        when(hashService.hashDslArtifact(eq(AGENT_ID), eq("AGCP1"), anyMap())).thenReturn(
                new AgentArtifactHashResult("{}", NEW_SIGNED_HASH, "SHA-256", 2));
        when(identityService.materializeForCurrentReplacement(eq(AGENT_ID), any(), any(), eq("RTPK2"))).thenReturn(
                new RuntimePackageIdentityService.RuntimePackageMaterialization(correctedPackage, true));

        service = new AgentRuntimePackageRegenerationService();
        service.agentDefinitionRepository = definitionRepository;
        service.runtimePackageRepository = packageRepository;
        service.snapshotLoader = snapshotLoader;
        service.compilationPreconditionValidator = compilationValidator;
        service.dslArtifactBuilder = dslBuilder;
        service.artifactHashService = hashService;
        service.runtimeCompatibilityValidator = compatibilityValidator;
        service.runtimePackageIdentityService = identityService;
        service.runtimeCatalogLifecyclePublisher = catalogPublisher;
        service.clock = Clock.fixed(Instant.parse("2026-07-27T12:00:00Z"), ZoneOffset.UTC);
    }

    @Test
    void activeStalePackageIsRebuiltPromotedAndPublishedWithoutDisableOrOrchestrator() {
        AgentRuntimePackageRegenerationResponse response = service.regenerate(
                AGENT_ID, new AgentRuntimePackageRegenerationRequest().note("repair canonical DSL"));

        ArgumentCaptor<AgentActivationSnapshot> snapshot = ArgumentCaptor.forClass(AgentActivationSnapshot.class);
        verify(identityService).materializeForCurrentReplacement(
                eq(AGENT_ID), any(), snapshot.capture(), eq("RTPK2"));
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = (Map<String, Object>) ((Map<String, Object>)
                snapshot.getValue().dslArtifact().get("evaluation")).get("condition");
        assertThat(condition).containsExactly(
                Map.entry("field", "payload.ongroundServiceEvent.eventsType"),
                Map.entry("operator", "CONTAINS"),
                Map.entry("value", "DEPARTING"));
        assertThat(condition).doesNotContainKey("type");
        assertThat(response.getOldPackageVersion()).isEqualTo(2L);
        assertThat(response.getCurrentPackageVersion()).isEqualTo(3L);
        assertThat(response.getArtifactHash()).isEqualTo(NEW_HASH).isNotEqualTo(OLD_HASH);
        assertThat(response.getPackageFingerprint()).isEqualTo("b".repeat(64));
        assertThat(response.getSubmissionId()).isNotEqualTo(oldPackage.getCodSubmissionid());
        assertThat(response.getPackageCreated()).isTrue();
        assertThat(response.getCurrentPackageReused()).isFalse();
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("ACTIVE");
        assertThat(definition.getCodCurrentruntimepackage()).isEqualTo("RTPK3");
        verify(catalogPublisher).appendUpsert(eq(AGENT_ID), eq(correctedPackage), any());
        var order = inOrder(definitionRepository, identityService, catalogPublisher);
        order.verify(definitionRepository).findByDefinitionIdForUpdate(AGENT_ID);
        order.verify(identityService).materializeForCurrentReplacement(
                eq(AGENT_ID), any(), any(), eq("RTPK2"));
        order.verify(catalogPublisher).appendUpsert(eq(AGENT_ID), eq(correctedPackage), any());
    }

    @Test
    void repeatedEquivalentRegenerationReusesCurrentPackageWithoutAnotherPromotion() {
        definition.setCodCurrentruntimepackage("RTPK3");
        when(packageRepository.findCurrentByAgentDefinition(AGENT_ID)).thenReturn(Optional.of(correctedPackage));
        when(identityService.materializeForCurrentReplacement(eq(AGENT_ID), any(), any(), eq("RTPK3"))).thenReturn(
                new RuntimePackageIdentityService.RuntimePackageMaterialization(correctedPackage, false));

        AgentRuntimePackageRegenerationResponse response = service.regenerate(AGENT_ID, null);

        assertThat(response.getCurrentPackageVersion()).isEqualTo(3L);
        assertThat(response.getPackageCreated()).isFalse();
        assertThat(response.getCurrentPackageReused()).isTrue();
        verify(catalogPublisher, never()).appendUpsert(any(), any(), any());
    }

    @Test
    void packageFailureLeavesVersionTwoCurrentAndActive() {
        when(identityService.materializeForCurrentReplacement(eq(AGENT_ID), any(), any(), eq("RTPK2")))
                .thenThrow(new AgentActivationTechnicalException("package persistence failed"));

        assertThatThrownBy(() -> service.regenerate(AGENT_ID, null))
                .isInstanceOf(AgentActivationTechnicalException.class)
                .hasMessageContaining("persistence failed");
        assertThat(definition.getCodCurrentruntimepackage()).isEqualTo("RTPK2");
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("ACTIVE");
        verify(catalogPublisher, never()).appendUpsert(any(), any(), any());
    }

    @Test
    void nonActiveDefinitionIsRejectedBeforeMaterialization() {
        definition.setSglStatus(status("READY"));

        assertThatThrownBy(() -> service.regenerate(AGENT_ID, null))
                .isInstanceOf(AgentRuntimePackageRegenerationRejectedException.class)
                .hasMessageContaining("requires an ACTIVE");
        verify(identityService, never()).materializeForCurrentReplacement(any(), any(), any(), any());
    }

    private AgentActivationSnapshot sourceSnapshot() {
        Map<String, Object> staleDsl = correctedDsl();
        Map<String, Object> staleCondition = new LinkedHashMap<>((Map<String, Object>)
                ((Map<String, Object>) staleDsl.get("evaluation")).get("condition"));
        staleCondition.put("type", "SERVICE_DATA_FIELD_MATCH");
        ((Map<String, Object>) staleDsl.get("evaluation")).put("condition", staleCondition);
        AgentActivationSnapshot.AgentActivationCompilationSnapshot compilation =
                new AgentActivationSnapshot.AgentActivationCompilationSnapshot(
                        "AGCP1", AGENT_ID, "READY", "READY", "DSL", true, Map.of(),
                        Map.of("artifactHash", OLD_SIGNED_HASH, "dslArtifact", staleDsl), null, "operator",
                        OffsetDateTime.parse("2026-07-01T09:59:00Z"),
                        OffsetDateTime.parse("2026-07-01T10:00:00Z"),
                        OffsetDateTime.parse("2026-07-01T10:01:00Z"),
                        OffsetDateTime.parse("2026-07-01T10:01:00Z"), staleDsl);
        return new AgentActivationSnapshot(
                AGENT_ID, "Agent", null, "ACTIVE", "DSL", "LOW", "EVENT_INTERPRETER", "EVENT",
                "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION", "operator",
                OffsetDateTime.parse("2026-06-01T10:00:00Z"), OffsetDateTime.parse("2026-07-01T10:01:00Z"),
                null, null, null, null,
                new AgentActivationSnapshot.AgentActivationArtifactSnapshot(
                        "DSL", "iia-agent-artifact://agent-definitions/" + AGENT_ID + "/compilations/AGCP1/dsl",
                        OLD_SIGNED_HASH, "SIGNED", "runtime", "v1", "ready"),
                new AgentActivationSnapshot.AgentActivationCompilationSummarySnapshot(
                        "AGCP1", "READY", "READY", OffsetDateTime.parse("2026-07-01T10:01:00Z")),
                compilation);
    }

    private AgentCompilationPreconditionValidationResult validCompilation() {
        return new AgentCompilationPreconditionValidationResult(
                true, List.of(), List.of(), "EVENT_INTERPRETER", "EVENT", "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION", "STATELESS_EVENT_MATCH", "DSL", "KAFKA_EVENT", Map.of());
    }

    private Map<String, Object> correctedDsl() {
        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("field", "payload.ongroundServiceEvent.eventsType");
        condition.put("operator", "CONTAINS");
        condition.put("value", "DEPARTING");
        Map<String, Object> evaluation = new LinkedHashMap<>();
        evaluation.put("condition", condition);
        Map<String, Object> artifact = new LinkedHashMap<>();
        artifact.put("schemaVersion", "iia.agent.dsl/v1");
        artifact.put("evaluation", evaluation);
        return artifact;
    }

    private AgentDefinition definition(String value) {
        AgentDefinition result = new AgentDefinition();
        result.setCodAgentdefinition(AGENT_ID);
        result.setSglStatus(status(value));
        return result;
    }

    private AgentDefinitionStatus status(String value) {
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus(value);
        return status;
    }

    private AgentRuntimePackage runtimePackage(
            String id, long version, String hash, String fingerprint, String submissionId) {
        AgentRuntimePackage value = mock(AgentRuntimePackage.class);
        when(value.getCodRuntimepackage()).thenReturn(id);
        when(value.getNumPackageversion()).thenReturn(version);
        when(value.getDscArtifacthash()).thenReturn(hash);
        when(value.getDscPackagefingerprint()).thenReturn(fingerprint);
        when(value.getCodSubmissionid()).thenReturn(submissionId);
        return value;
    }
}
