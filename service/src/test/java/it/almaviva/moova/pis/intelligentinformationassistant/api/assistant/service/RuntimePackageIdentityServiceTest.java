package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimeCatalogChangeRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimePackageRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimeCatalogChange;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.RuntimeCatalogAction;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.RuntimeCatalogRemovalReason;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuntimePackageIdentityServiceTest {

    private static final String AGENT_ID = "AGDF1";
    private static final String TOOL = "SERVICE_DATA_API.POST_/v2/stoppointjourneys";
    private static final Instant NOW = Instant.parse("2026-06-17T10:00:00Z");

    private final List<AgentRuntimePackage> packages = new ArrayList<>();
    private final List<AgentRuntimeCatalogChange> changes = new ArrayList<>();

    private AgentDefinition definition;
    private AgentDefinitionRepository definitionRepository;
    private AgentRuntimePackageRepository packageRepository;
    private AgentRuntimeCatalogChangeRepository changeRepository;
    private AgentActivationSnapshotLoader snapshotLoader;
    private AgentActivationPreconditionValidator preconditionValidator;
    private EntityManager entityManager;
    private RuntimePackageIdentityService identityService;
    private RuntimeCatalogLifecyclePublisher publisher;
    private AgentActivationFinalizationService activationFinalizer;
    private AgentDisableFinalizationService disableFinalizer;

    @BeforeEach
    void setUp() {
        definition = new AgentDefinition();
        definition.setCodAgentdefinition(AGENT_ID);
        definition.setSglStatus(status("READY"));

        definitionRepository = mock(AgentDefinitionRepository.class);
        packageRepository = mock(AgentRuntimePackageRepository.class);
        changeRepository = mock(AgentRuntimeCatalogChangeRepository.class);
        snapshotLoader = mock(AgentActivationSnapshotLoader.class);
        preconditionValidator = mock(AgentActivationPreconditionValidator.class);
        entityManager = mock(EntityManager.class);

        when(definitionRepository.findByDefinitionIdForUpdate(AGENT_ID)).thenReturn(Optional.of(definition));
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(snapshot()));
        when(preconditionValidator.validate(any())).thenReturn(new AgentActivationPreconditionValidationResult(true, List.of(), List.of()));
        when(entityManager.getReference(eq(AgentCompilation.class), eq("AGCP1"))).thenReturn(compilation());
        when(entityManager.getReference(eq(AgentDefinition.class), eq(AGENT_ID))).thenReturn(definition);
        when(entityManager.getReference(eq(AgentRuntimePackage.class), any())).thenAnswer(invocation -> packageById(invocation.getArgument(1)));

        when(packageRepository.findMaximumPackageVersion(AGENT_ID)).thenAnswer(invocation ->
                packages.stream().mapToLong(AgentRuntimePackage::getNumPackageversion).max().orElse(0L));
        when(packageRepository.findByAgentDefinitionAndFingerprint(eq(AGENT_ID), any())).thenAnswer(invocation -> {
            String fingerprint = invocation.getArgument(1);
            return packages.stream().filter(p -> p.getDscPackagefingerprint().equals(fingerprint)).findFirst();
        });
        when(packageRepository.persistImmutablePackage(any())).thenAnswer(invocation -> {
            AgentRuntimePackage runtimePackage = invocation.getArgument(0);
            set(runtimePackage, "codRuntimepackage", "RTPK" + (packages.size() + 1));
            packages.add(runtimePackage);
            return runtimePackage;
        });
        when(packageRepository.findByIdOptional(any())).thenAnswer(invocation ->
                packages.stream().filter(p -> p.getCodRuntimepackage().equals(invocation.getArgument(0))).findFirst());

        when(changeRepository.findByDeduplicationKey(any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return changes.stream().filter(c -> c.getDscDeduplicationkey().equals(key)).findFirst();
        });
        when(changeRepository.findLatestByAgentDefinitionId(AGENT_ID)).thenAnswer(invocation ->
                changes.isEmpty() ? Optional.empty() : Optional.of(changes.getLast()));
        when(changeRepository.nextCatalogChangeId()).thenAnswer(invocation -> "RTCH" + (changes.size() + 1));
        when(changeRepository.append(any())).thenAnswer(invocation -> {
            AgentRuntimeCatalogChange change = invocation.getArgument(0);
            set(change, "numChangesequence", (long) changes.size() + 1L);
            changes.add(change);
            return change;
        });

        RuntimeAgentPackageFactory factory = new RuntimeAgentPackageFactory(new AgentRuntimePackageBuilder());
        identityService = new RuntimePackageIdentityService(
                definitionRepository,
                packageRepository,
                snapshotLoader,
                preconditionValidator,
                factory,
                entityManager,
                Clock.fixed(NOW, ZoneOffset.UTC),
                "operator1");
        publisher = new RuntimeCatalogLifecyclePublisher(changeRepository, entityManager);
        activationFinalizer = new AgentActivationFinalizationService(definitionRepository, publisher);
        activationFinalizer.runtimePackageRepository = packageRepository;
        disableFinalizer = new AgentDisableFinalizationService(definitionRepository, publisher);
        disableFinalizer.runtimePackageRepository = packageRepository;
        when(definitionRepository.statusReference(any())).thenAnswer(invocation -> status(invocation.getArgument(0)));
    }

    @Test
    void firstPackagePersistsStableIdentityAndCompleteSnapshot() {
        AgentRuntimePackage runtimePackage = identityService.materializeOrReuse(AGENT_ID, command("note one"));

        assertThat(runtimePackage.getNumPackageversion()).isEqualTo(1);
        assertThat(runtimePackage.getCodSubmissionid()).startsWith("ACTIVATE:AGDF1:");
        assertThat(runtimePackage.getDtCreatedat()).isNotNull();
        assertThat(runtimePackage.getDtSubmittedat()).isEqualTo(NOW);
        assertThat(runtimePackage.getDtCreatedat().toInstant()).isEqualTo(runtimePackage.getDtSubmittedat());
        assertThat(runtimePackage.getDscPackagefingerprint()).matches("[0-9a-f]{64}");
        assertThat(runtimePackage.getJsnRuntimepackage()).containsKeys(
                "submissionId", "desiredStatus", "packageVersion", "submittedAt", "submittedBy",
                "startImmediatelyIfAllowed", "note", "agentDefinition");
        assertThat(((Map<?, ?>) ((List<?>) ((Map<?, ?>) runtimePackage.getJsnRuntimepackage().get("agentDefinition"))
                .get("dataSourceBindings")).getFirst()).get("configuration"))
                .isEqualTo(Map.of("subscriptionProfile", "SERVICEDATA_EVENTS"));
        assertThat(packages).hasSize(1);
    }

    @Test
    void equivalentRetryReusesIdentityAndTransportOnlyChangesDoNotMatter() {
        AgentRuntimePackage first = identityService.materializeOrReuse(AGENT_ID, command("first note"));
        identityService.clock = Clock.fixed(Instant.parse("2026-06-18T10:00:00Z"), ZoneOffset.UTC);
        AgentRuntimePackage second = identityService.materializeOrReuse(AGENT_ID, command("different note"));

        assertThat(second.getCodRuntimepackage()).isEqualTo(first.getCodRuntimepackage());
        assertThat(second.getNumPackageversion()).isEqualTo(first.getNumPackageversion());
        assertThat(second.getCodSubmissionid()).isEqualTo(first.getCodSubmissionid());
        assertThat(second.getDscPackagefingerprint()).isEqualTo(first.getDscPackagefingerprint());
        assertThat(second.getDtCreatedat()).isEqualTo(first.getDtCreatedat());
        assertThat(second.getDtSubmittedat()).isEqualTo(first.getDtSubmittedat());
        assertThat(packages).hasSize(1);
    }

    @Test
    void runtimeSignificantChangeCreatesNextVersion() {
        AgentRuntimePackage first = identityService.materializeOrReuse(AGENT_ID, command(null));
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(snapshotWithCpu(300)));

        AgentRuntimePackage second = identityService.materializeOrReuse(AGENT_ID, command(null));

        assertThat(second.getNumPackageversion()).isEqualTo(first.getNumPackageversion() + 1);
        assertThat(second.getDscPackagefingerprint()).isNotEqualTo(first.getDscPackagefingerprint());
        assertThat(packages).hasSize(2);
    }

    @Test
    void activationFailureKeepsReservationWithoutUpsertOrActiveStatus() {
        identityService.materializeOrReuse(AGENT_ID, command(null));

        assertThat(packages).hasSize(1);
        assertThat(changes).isEmpty();
        assertThat(definition.getCodCurrentruntimepackage()).isNull();
    }

    @Test
    void packagePersistenceFailureCreatesNoLifecycleSideEffects() {
        doThrow(new RuntimeException("runtime package insert failed"))
                .when(packageRepository).persistImmutablePackage(any());

        assertThatThrownBy(() -> identityService.materializeOrReuse(AGENT_ID, command(null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("runtime package insert failed");

        assertThat(packages).isEmpty();
        assertThat(changes).isEmpty();
        assertThat(definition.getCodCurrentruntimepackage()).isNull();
    }

    @Test
    void successfulActivationSetsPointerAndAppendsOneIdempotentUpsert() {
        AgentRuntimePackage runtimePackage = identityService.materializeOrReuse(AGENT_ID, command(null));
        OffsetDateTime activatedAt = OffsetDateTime.parse("2026-06-17T10:01:00Z");
        assertThat(activationFinalizer.finalizeAcceptedActivation(AGENT_ID, "READY", runtimePackage, activatedAt)).isTrue();
        publisher.appendUpsert(AGENT_ID, runtimePackage, activatedAt);
        publisher.appendUpsert(AGENT_ID, runtimePackage, activatedAt);

        assertThat(definition.getCodCurrentruntimepackage()).isEqualTo(runtimePackage.getCodRuntimepackage());
        assertThat(changes).hasSize(1);
        assertThat(changes.getFirst().getSglAction()).isEqualTo(RuntimeCatalogAction.UPSERT);
        assertThat(changes.getFirst().getCodRuntimepackage().getCodRuntimepackage()).isEqualTo(runtimePackage.getCodRuntimepackage());
    }

    @Test
    void disableAppendsRemoveAndKeepsPackagePointerAndRows() {
        AgentRuntimePackage runtimePackage = identityService.materializeOrReuse(AGENT_ID, command(null));
        Map<String, Object> originalSnapshot = new LinkedHashMap<>(runtimePackage.getJsnRuntimepackage());
        definition.setCodCurrentruntimepackage(runtimePackage.getCodRuntimepackage());
        OffsetDateTime disabledAt = OffsetDateTime.parse("2026-06-17T10:02:00Z");
        when(definitionRepository.transitionStatus(AGENT_ID, "ACTIVE", "DISABLED", disabledAt)).thenReturn(1);

        assertThat(disableFinalizer.finalizeAcceptedDisable(AGENT_ID, "ACTIVE", "DISABLED", disabledAt)).isTrue();
        publisher.appendRemove(AGENT_ID, "DISABLED", disabledAt);
        publisher.appendRemove(AGENT_ID, "DISABLED", disabledAt);

        assertThat(definition.getCodCurrentruntimepackage()).isEqualTo(runtimePackage.getCodRuntimepackage());
        assertThat(packages).hasSize(1);
        assertThat(changes).hasSize(1);
        assertThat(runtimePackage.getJsnRuntimepackage()).isEqualTo(originalSnapshot);
        assertThat(changes.getFirst().getSglAction()).isEqualTo(RuntimeCatalogAction.REMOVE);
        assertThat(changes.getFirst().getCodRuntimepackage()).isNull();
        assertThat(changes.getFirst().getSglRemovalreason()).isEqualTo(RuntimeCatalogRemovalReason.NOT_ACTIVE);
    }

    @Test
    void reactivationWithoutPackageChangesReusesPackageAndCreatesNewLifecycleUpsert() {
        AgentRuntimePackage runtimePackage = identityService.materializeOrReuse(AGENT_ID, command(null));
        OffsetDateTime epochA = OffsetDateTime.parse("2026-06-17T10:01:00Z");
        OffsetDateTime epochB = OffsetDateTime.parse("2026-06-17T10:02:00Z");
        OffsetDateTime epochC = OffsetDateTime.parse("2026-06-17T10:03:00Z");
        OffsetDateTime epochD = OffsetDateTime.parse("2026-06-17T10:04:00Z");
        AgentRuntimeCatalogChange firstUpsert = publisher.appendUpsert(AGENT_ID, runtimePackage, epochA);
        assertThat(publisher.appendUpsert(AGENT_ID, runtimePackage, epochA)).isSameAs(firstUpsert);
        publisher.appendRemove(AGENT_ID, "DISABLED", epochB);
        AgentRuntimePackage reused = identityService.materializeOrReuse(AGENT_ID, command(null));
        AgentRuntimeCatalogChange secondUpsert = publisher.appendUpsert(AGENT_ID, reused, epochC);
        assertThat(publisher.appendUpsert(AGENT_ID, reused, epochC)).isSameAs(secondUpsert);
        publisher.appendRemove(AGENT_ID, "DISABLED", epochD);

        assertThat(reused.getCodRuntimepackage()).isEqualTo(runtimePackage.getCodRuntimepackage());
        assertThat(reused.getNumPackageversion()).isEqualTo(runtimePackage.getNumPackageversion());
        assertThat(reused.getDscPackagefingerprint()).isEqualTo(runtimePackage.getDscPackagefingerprint());
        assertThat(packages).hasSize(1);
        assertThat(changes).extracting(AgentRuntimeCatalogChange::getSglAction)
                .containsExactly(RuntimeCatalogAction.UPSERT, RuntimeCatalogAction.REMOVE,
                        RuntimeCatalogAction.UPSERT, RuntimeCatalogAction.REMOVE);
        assertThat(changes).filteredOn(c -> RuntimeCatalogAction.UPSERT.equals(c.getSglAction())).hasSize(2);
        assertThat(secondUpsert.getCodCatalogchange()).isNotEqualTo(firstUpsert.getCodCatalogchange());
        assertThat(secondUpsert.getDscDeduplicationkey()).isNotEqualTo(firstUpsert.getDscDeduplicationkey());
        assertThat(changes).extracting(AgentRuntimeCatalogChange::getNumChangesequence)
                .containsExactly(1L, 2L, 3L, 4L);
        assertThat(changes.get(1).getDscDeduplicationkey()).isNotEqualTo(changes.get(3).getDscDeduplicationkey());
    }

    @Test
    void snapshotContainsLogicalConnectorReferencesButNoSensitivePhysicalFields() {
        AgentRuntimePackage runtimePackage = identityService.materializeOrReuse(AGENT_ID, command("operator note"));
        String json = runtimePackage.getJsnRuntimepackage().toString();

        assertThat(json).contains("servicedata-realtime-v2");
        assertThat(json).doesNotContain("password", "token", "bootstrap", "topic", "http://", "https://",
                "privateKey", "Authorization", "natural-language");
    }

    @Test
    void backfillMaterializesOnceAndSecondRunNoopsWhenPointerExists() {
        RuntimePackageBackfillDefinitionProcessor processor = new RuntimePackageBackfillDefinitionProcessor();
        processor.runtimePackageIdentityService = identityService;
        processor.agentDefinitionRepository = definitionRepository;
        processor.runtimeCatalogLifecyclePublisher = publisher;
        when(definitionRepository.setCurrentRuntimePackage(any(), any(), any())).thenAnswer(invocation -> {
            definition.setCodCurrentruntimepackage(invocation.getArgument(1));
            return true;
        });

        RuntimePackageBackfillDefinitionProcessor.BackfillOutcome outcome = processor.process(AGENT_ID);
        RuntimePackageBackfillDefinitionProcessor.BackfillOutcome replay = processor.process(AGENT_ID);

        assertThat(outcome).isEqualTo(RuntimePackageBackfillDefinitionProcessor.BackfillOutcome.MATERIALIZED);
        assertThat(replay).isEqualTo(RuntimePackageBackfillDefinitionProcessor.BackfillOutcome.REUSED);
        assertThat(packages).hasSize(1);
        assertThat(changes).hasSize(1);
        assertThat(definition.getCodCurrentruntimepackage()).isEqualTo(packages.getFirst().getCodRuntimepackage());
    }

    private AgentActivationCommand command(String note) {
        return new AgentActivationCommand(AGENT_ID, note, true);
    }

    private AgentRuntimePackage packageById(String runtimePackageId) {
        return packages.stream()
                .filter(p -> p.getCodRuntimepackage().equals(runtimePackageId))
                .findFirst()
                .orElseThrow();
    }

    private AgentCompilation compilation() {
        AgentCompilation compilation = new AgentCompilation();
        compilation.setCodAgentcompilation("AGCP1");
        return compilation;
    }

    @Test
    void legacyPackageWithoutTransmittedConfigurationIsPreservedAndNotReused() {
        AgentRuntimePackage legacy = identityService.materializeOrReuse(AGENT_ID, command(null));
        Map<String, Object> historicalJson = new LinkedHashMap<>(legacy.getJsnRuntimepackage());
        Map<String, Object> historicalDefinition = new LinkedHashMap<>((Map<String, Object>) historicalJson.get("agentDefinition"));
        List<Map<String, Object>> historicalBindings = ((List<Map<String, Object>>) historicalDefinition.get("dataSourceBindings"))
                .stream().map(value -> (Map<String, Object>) new LinkedHashMap<>(value)).toList();
        historicalBindings.forEach(binding -> binding.put("configuration", Map.of()));
        historicalDefinition.put("dataSourceBindings", historicalBindings);
        historicalJson.put("agentDefinition", historicalDefinition);
        packages.clear();
        legacy = AgentRuntimePackage.create(definition, 1L, legacy.getCodSubmissionid(), "8".repeat(64),
                legacy.getDscArtifacthash(), legacy.getCodAgentcompilation(), legacy.getDscPackageschemaversion(),
                legacy.getDscCanonicalization(), legacy.getDscHashalgorithm(), historicalJson,
                legacy.getDtSourceupdatedat(), legacy.getDtSubmittedat(), legacy.getDtCreatedat(), legacy.getCodCreatedby());
        set(legacy, "codRuntimepackage", "RTPK1");
        packages.add(legacy);
        Map<String, Object> immutableHistoricalSnapshot = new LinkedHashMap<>(historicalJson);

        AgentRuntimePackage corrected = identityService.materializeOrReuse(AGENT_ID, command(null));

        assertThat(corrected.getNumPackageversion()).isEqualTo(2L);
        assertThat(corrected.getDscPackagefingerprint()).isNotEqualTo(legacy.getDscPackagefingerprint());
        assertThat(legacy.getJsnRuntimepackage()).isEqualTo(immutableHistoricalSnapshot);
        assertThat(packages).hasSize(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void correctedScheduledPackageDoesNotReuseHistoricalInvalidProfileAndThenReusesCorrectedIdentity() {
        AgentActivationSnapshot scheduled = scheduledSnapshot();
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(scheduled));

        RuntimeAgentPackageFactory factory = new RuntimeAgentPackageFactory(new AgentRuntimePackageBuilder());
        RuntimeAgentPackageBuild correctedCandidate = factory.build(
                scheduled,
                command(null),
                new AgentRuntimePackageBuildContext(2L, NOW, "operator1"));
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        Map<String, Object> invalidPayload = mapper.convertValue(
                correctedCandidate.runtimePackageJson(), LinkedHashMap.class);
        Map<String, Object> invalidDefinition = (Map<String, Object>) invalidPayload.get("agentDefinition");
        Map<String, Object> invalidBinding = (Map<String, Object>) ((List<?>) invalidDefinition
                .get("dataSourceBindings")).getFirst();
        invalidBinding.put("configuration", Map.of(
                "subscriptionProfile", "SERVICEDATA_STOPPOINTJOURNEYS"));
        invalidPayload.put("packageVersion", 1L);
        invalidPayload.put("submissionId", null);

        RuntimeAgentPackageCanonicalIdentity canonicalIdentity = new RuntimeAgentPackageCanonicalIdentity();
        AgentRuntimeSubmission invalidUnsigned = mapper.convertValue(invalidPayload, AgentRuntimeSubmission.class);
        String invalidFingerprint = canonicalIdentity.identify(invalidUnsigned).fingerprint();
        invalidPayload.put("submissionId", canonicalIdentity.stableSubmissionId(AGENT_ID, invalidFingerprint));
        AgentRuntimeSubmission invalidSubmission = mapper.convertValue(invalidPayload, AgentRuntimeSubmission.class);
        AgentRuntimePackage invalidV1 = AgentRuntimePackage.create(
                definition,
                1L,
                invalidSubmission.submissionId(),
                invalidFingerprint,
                invalidSubmission.agentDefinition().artifact().hash(),
                compilation(),
                "iia.runtime-agent-submission/v1",
                RuntimeAgentPackageCanonicalIdentity.CANONICALIZATION,
                RuntimeAgentPackageCanonicalIdentity.HASH_ALGORITHM,
                invalidPayload,
                scheduled.updatedAt(),
                invalidSubmission.submittedAt(),
                OffsetDateTime.ofInstant(invalidSubmission.submittedAt(), ZoneOffset.UTC),
                "operator1");
        set(invalidV1, "codRuntimepackage", "RTPK1");
        packages.add(invalidV1);

        AgentRuntimePackage correctedV2 = identityService.materializeOrReuse(AGENT_ID, command(null));
        AgentRuntimePackage repeated = identityService.materializeOrReuse(AGENT_ID, command("transport-only"));

        assertThat(correctedV2.getNumPackageversion()).isEqualTo(2L);
        assertThat(correctedV2.getCodRuntimepackage()).isNotEqualTo(invalidV1.getCodRuntimepackage());
        assertThat(correctedV2.getDscPackagefingerprint()).isNotEqualTo(invalidFingerprint);
        Map<String, Object> correctedDefinition = (Map<String, Object>) correctedV2.getJsnRuntimepackage()
                .get("agentDefinition");
        Map<String, Object> correctedBinding = (Map<String, Object>) ((List<?>) correctedDefinition
                .get("dataSourceBindings")).getFirst();
        assertThat((Map<?, ?>) correctedBinding.get("configuration")).isEmpty();
        assertThat(correctedBinding.toString()).doesNotContain("subscriptionProfile");
        assertThat(repeated.getCodRuntimepackage()).isEqualTo(correctedV2.getCodRuntimepackage());
        assertThat(repeated.getNumPackageversion()).isEqualTo(2L);
        assertThat(repeated.getDscPackagefingerprint()).isEqualTo(correctedV2.getDscPackagefingerprint());
        assertThat(packages).hasSize(2);
    }

    @Test
    void currentReplacementNeverPromotesAnEquivalentHistoricalVersionBackward() {
        AgentRuntimePackage historical = identityService.materializeOrReuse(AGENT_ID, command(null));
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(snapshotWithCpu(300)));
        AgentRuntimePackage staleCurrent = identityService.materializeOrReuse(AGENT_ID, command(null));
        definition.setCodCurrentruntimepackage(staleCurrent.getCodRuntimepackage());

        AgentRuntimePackage replacement = identityService.materializeForCurrentReplacement(
                AGENT_ID, command(null), snapshot(), staleCurrent.getCodRuntimepackage()).runtimePackage();

        assertThat(replacement.getNumPackageversion()).isEqualTo(3L);
        assertThat(replacement.getCodRuntimepackage()).isNotEqualTo(historical.getCodRuntimepackage());
        assertThat(replacement.getDscPackagefingerprint()).isEqualTo(historical.getDscPackagefingerprint());
        assertThat(replacement.getCodSubmissionid()).isEqualTo(historical.getCodSubmissionid());
        assertThat(packages).hasSize(3);
    }

    @Test
    void correctedCanonicalArtifactCreatesNextVersionAndStableNewSubmissionIdentity() {
        Map<String, Object> staleDsl = dsl();
        staleDsl.put("evaluation", Map.of(
                "mode", "STATELESS_EVENT_MATCH",
                "condition", Map.of(
                        "type", "SERVICE_DATA_FIELD_MATCH",
                        "field", "payload.ongroundServiceEvent.eventsType",
                        "operator", "CONTAINS",
                        "value", "DEPARTING")));
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(snapshot(250, staleDsl)));
        AgentRuntimePackage stale = identityService.materializeOrReuse(AGENT_ID, command(null));

        Map<String, Object> correctedDsl = dsl();
        correctedDsl.put("evaluation", Map.of(
                "mode", "STATELESS_EVENT_MATCH",
                "condition", Map.of(
                        "field", "payload.ongroundServiceEvent.eventsType",
                        "operator", "CONTAINS",
                        "value", "DEPARTING")));
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(snapshot(250, correctedDsl)));
        AgentRuntimePackage corrected = identityService.materializeOrReuse(AGENT_ID, command(null));
        AgentRuntimePackage repeated = identityService.materializeOrReuse(AGENT_ID, command("transport-only change"));

        assertThat(corrected.getNumPackageversion()).isEqualTo(stale.getNumPackageversion() + 1);
        assertThat(corrected.getDscArtifacthash()).isNotEqualTo(stale.getDscArtifacthash());
        assertThat(corrected.getDscPackagefingerprint()).isNotEqualTo(stale.getDscPackagefingerprint());
        assertThat(corrected.getCodSubmissionid()).isNotEqualTo(stale.getCodSubmissionid());
        assertThat(repeated.getCodRuntimepackage()).isEqualTo(corrected.getCodRuntimepackage());
        assertThat(repeated.getCodSubmissionid()).isEqualTo(corrected.getCodSubmissionid());
        assertThat(packages).hasSize(2);
    }

    private AgentDefinitionStatus status(String value) {
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus(value);
        return status;
    }

    private AgentActivationSnapshot snapshotWithCpu(int cpu) {
        return snapshot(cpu);
    }

    private AgentActivationSnapshot snapshot() {
        return snapshot(250);
    }

    private AgentActivationSnapshot snapshot(int cpu) {
        return snapshot(cpu, dsl());
    }

    private AgentActivationSnapshot snapshot(int cpu, Map<String, Object> dsl) {
        String artifactHash = new AgentCanonicalJsonService().hash(dsl).hash();
        return new AgentActivationSnapshot(
                AGENT_ID, "Runtime Agent", "Runtime package fixture", "READY", "DSL", "MEDIUM",
                "EVENT_INTERPRETER", "EVENT", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION", "operator1",
                OffsetDateTime.parse("2026-06-16T10:00:00Z"), OffsetDateTime.parse("2026-06-17T09:00:00Z"),
                new AgentActivationSnapshot.AgentActivationAlertSnapshot("ALRT1", "Alert", 7),
                new AgentActivationSnapshot.AgentActivationProfileSnapshot(
                        "MEDIUM", "Medium", "Profile", true, List.of("EVENT_INTERPRETER"),
                        cpu, 500, 512, 1024, "TOOL_GATEWAY_ONLY", 2,
                        OffsetDateTime.parse("2026-06-01T10:00:00Z"), OffsetDateTime.parse("2026-06-10T10:00:00Z")),
                new AgentActivationSnapshot.AgentActivationPolicySnapshot(
                        "CONTINUOUS", "Europe/Rome",
                        OffsetDateTime.parse("2026-06-01T10:00:00Z"), OffsetDateTime.parse("2026-08-01T10:00:00Z"),
                        null, null, null, null, List.of(), Map.of("type", "CONTINUOUS", "timezone", "Europe/Rome")),
                new AgentActivationSnapshot.AgentActivationRequirementsSnapshot(
                        List.of(new AgentActivationSnapshot.AgentActivationSourceSnapshot("SERVICE_DATA", true, null)),
                        List.of(Map.of("permission", "READ_SERVICE_DATA")),
                        List.of(),
                        List.of(),
                        List.of(),
                        runtimeContract(),
                        Map.of("dslPreviewOnly", true)),
                new AgentActivationSnapshot.AgentActivationArtifactSnapshot(
                        "DSL",
                        "iia-agent-artifact://agent-definitions/AGDF1/compilations/AGCP1/dsl",
                        artifactHash,
                        "SIGNED",
                        "STANDARD_AGENT_DSL_EVALUATOR",
                        "iia.agent.dsl/v1",
                        "Compiled DSL."),
                new AgentActivationSnapshot.AgentActivationCompilationSummarySnapshot(
                        "AGCP1", "READY", "READY", OffsetDateTime.parse("2026-06-17T09:02:00Z")),
                new AgentActivationSnapshot.AgentActivationCompilationSnapshot(
                        "AGCP1", AGENT_ID, "READY", "READY", "DSL", false,
                        Map.of("requestedMode", "DSL"),
                        Map.of("artifactHash", artifactHash, "dslArtifact", dsl),
                        null, "operator1",
                        OffsetDateTime.parse("2026-06-17T09:00:00Z"),
                        OffsetDateTime.parse("2026-06-17T09:01:00Z"),
                        OffsetDateTime.parse("2026-06-17T09:02:00Z"),
                        OffsetDateTime.parse("2026-06-17T09:02:00Z"),
                        dsl));
    }

    private AgentActivationSnapshot scheduledSnapshot() {
        AgentActivationSnapshot base = snapshot();
        Map<String, Object> scheduledDsl = scheduledDsl();
        String artifactHash = new AgentCanonicalJsonService().hash(scheduledDsl).hash();
        AgentActivationSnapshot.AgentActivationRequirementsSnapshot requirements =
                new AgentActivationSnapshot.AgentActivationRequirementsSnapshot(
                        base.requirements().requiredSources(),
                        base.requirements().requiredPermissions(),
                        List.of(),
                        List.of(),
                        List.of(),
                        scheduledRuntimeContract(),
                        base.requirements().blueprintJson());
        AgentActivationSnapshot.AgentActivationArtifactSnapshot artifact =
                new AgentActivationSnapshot.AgentActivationArtifactSnapshot(
                        base.artifact().artifactType(),
                        base.artifact().artifactUri(),
                        artifactHash,
                        base.artifact().signatureStatus(),
                        base.artifact().runtimeImage(),
                        base.artifact().sdkVersion(),
                        base.artifact().implementationSummary());
        AgentActivationSnapshot.AgentActivationCompilationSnapshot compilation =
                new AgentActivationSnapshot.AgentActivationCompilationSnapshot(
                        base.latestCompilation().compilationId(),
                        base.latestCompilation().agentDefinitionId(),
                        base.latestCompilation().status(),
                        base.latestCompilation().currentStep(),
                        base.latestCompilation().requestedMode(),
                        base.latestCompilation().force(),
                        base.latestCompilation().requestJson(),
                        Map.of("artifactHash", artifactHash, "dslArtifact", scheduledDsl),
                        base.latestCompilation().errorMessage(),
                        base.latestCompilation().requestedBy(),
                        base.latestCompilation().requestedAt(),
                        base.latestCompilation().startedAt(),
                        base.latestCompilation().completedAt(),
                        base.latestCompilation().updatedAt(),
                        scheduledDsl);
        return new AgentActivationSnapshot(
                base.agentDefinitionId(), base.name(), base.description(), base.status(), base.generationMode(),
                base.complexity(), "SCHEDULED_INTERPRETER", "SCHEDULE", "ServiceDataStopPointJourneysV2",
                base.outputModel(), base.createdBy(), base.createdAt(), base.updatedAt(), base.alert(), base.profile(),
                base.activationPolicy(), requirements, artifact, base.compilationSummary(), compilation);
    }

    private Map<String, Object> scheduledRuntimeContract() {
        Map<String, Object> contract = new LinkedHashMap<>(runtimeContract());
        contract.put("interpreterType", "SCHEDULED_INTERPRETER");
        contract.put("triggerType", "SCHEDULE");
        contract.put("inputModel", "ServiceDataStopPointJourneysV2");
        contract.put("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH");
        contract.put("allowedTools", List.of());
        return contract;
    }

    private Map<String, Object> scheduledDsl() {
        Map<String, Object> artifact = new LinkedHashMap<>(dsl());
        Map<String, Object> runtime = new LinkedHashMap<>((Map<String, Object>) artifact.get("runtime"));
        runtime.put("interpreterType", "SCHEDULED_INTERPRETER");
        runtime.put("triggerType", "SCHEDULE");
        runtime.put("inputModel", "ServiceDataStopPointJourneysV2");
        runtime.put("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH");
        artifact.put("runtime", runtime);
        artifact.put("trigger", Map.of(
                "type", "SCHEDULE",
                "source", "SERVICE_DATA",
                "inputModel", "ServiceDataStopPointJourneysV2"));
        artifact.put("evaluation", Map.of(
                "mode", "SCHEDULED_SNAPSHOT_MATCH",
                "condition", Map.of("field", "journey.status", "operator", "EXISTS")));
        return artifact;
    }

    private Map<String, Object> runtimeContract() {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("interpreterType", "EVENT_INTERPRETER");
        contract.put("triggerType", "EVENT");
        contract.put("inputModel", "ServiceDataV2");
        contract.put("outputModel", "AgentOutput.CANDIDATE_SUGGESTION");
        contract.put("evaluationMode", "STATELESS_EVENT_MATCH");
        contract.put("runtimeExecutionModel", "STANDARD_DSL_EVALUATOR");
        contract.put("source", "SERVICE_DATA");
        contract.put("allowedTools", List.of());
        contract.put("forbiddenCapabilities", List.of("LLM_RUNTIME_EXECUTION", "EXTERNAL_CODE_EXECUTION"));
        contract.put("orchestratorCompatibility", Map.of(
                "minimumRuntimeVersion", "0.0.3",
                "runtimeClass", "STANDARD_DSL_RUNTIME",
                "canonicalization", "JACKSON_SORT_PROPERTIES_AND_MAP_ENTRIES"));
        return contract;
    }

    private Map<String, Object> dsl() {
        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("engine", "STANDARD_AGENT_DSL_EVALUATOR");
        runtime.put("executionModel", "STANDARD_DSL_EVALUATOR");
        runtime.put("source", "SERVICE_DATA");
        runtime.put("interpreterType", "EVENT_INTERPRETER");
        runtime.put("triggerType", "EVENT");
        runtime.put("inputModel", "ServiceDataV2");
        runtime.put("outputModel", "AgentOutput.CANDIDATE_SUGGESTION");
        runtime.put("evaluationMode", "STATELESS_EVENT_MATCH");
        runtime.put("allowedTools", List.of());
        Map<String, Object> artifact = new LinkedHashMap<>();
        artifact.put("schemaVersion", "iia.agent.dsl/v1");
        artifact.put("artifactType", "DSL");
        artifact.put("agentDefinitionId", AGENT_ID);
        artifact.put("runtime", runtime);
        artifact.put("trigger", Map.of("type", "EVENT", "source", "SERVICE_DATA", "inputModel", "ServiceDataV2"));
        artifact.put("evaluation", Map.of("mode", "STATELESS_EVENT_MATCH", "condition", Map.of("field", "payload.status", "operator", "EXISTS")));
        artifact.put("output", Map.of("type", "CANDIDATE_SUGGESTION", "outputModel", "AgentOutput.CANDIDATE_SUGGESTION"));
        artifact.put("governance", Map.of("llmRuntimeExecutionAllowed", false, "externalCodeExecutionAllowed", false));
        return artifact;
    }

    private void set(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
