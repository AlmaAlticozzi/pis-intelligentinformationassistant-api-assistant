package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimeCatalogChangeRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimePackageRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimeCatalogChange;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.RuntimeCatalogAction;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuntimeCatalogLifecyclePublisherTest {

    private static final String AGENT_ID = "AGDF1";
    private static final String PACKAGE_ID = "RTPK1";
    private static final String CATALOG_CHANGE_ID = "RTCH0123456789ABCDEF0123456789ABCDEF";
    private static final String FINGERPRINT = "a".repeat(64);
    private static final Instant LIFECYCLE_INSTANT = Instant.parse("2026-06-30T10:00:00Z");
    private static final OffsetDateTime ACTIVATED_AT = OffsetDateTime.parse(LIFECYCLE_INSTANT.toString());

    private AgentRuntimeCatalogChangeRepository catalogRepository;
    private EntityManager entityManager;
    private RuntimeCatalogLifecyclePublisher publisher;
    private AgentDefinition definitionReference;
    private AgentRuntimePackage runtimePackage;

    @BeforeEach
    void setUp() {
        catalogRepository = mock(AgentRuntimeCatalogChangeRepository.class);
        entityManager = mock(EntityManager.class);
        publisher = new RuntimeCatalogLifecyclePublisher(catalogRepository, entityManager);
        definitionReference = mock(AgentDefinition.class);
        runtimePackage = runtimePackage();

        when(catalogRepository.findByDeduplicationKey(deduplicationKey())).thenReturn(Optional.empty());
        when(catalogRepository.nextCatalogChangeId()).thenReturn(CATALOG_CHANGE_ID);
        when(entityManager.getReference(AgentDefinition.class, AGENT_ID)).thenReturn(definitionReference);
        when(entityManager.getReference(AgentRuntimePackage.class, PACKAGE_ID)).thenReturn(runtimePackage);
    }

    @Test
    void newUpsertHasGeneratedCatalogChangeIdBeforePersistence() {
        ArgumentCaptor<AgentRuntimeCatalogChange> changeCaptor = ArgumentCaptor.forClass(AgentRuntimeCatalogChange.class);
        when(catalogRepository.append(any())).thenAnswer(invocation -> {
            AgentRuntimeCatalogChange change = invocation.getArgument(0);
            assertThat(change.getCodCatalogchange()).isNotBlank();
            return change;
        });

        publisher.appendUpsert(AGENT_ID, runtimePackage, ACTIVATED_AT);

        verify(catalogRepository).append(changeCaptor.capture());
        AgentRuntimeCatalogChange change = changeCaptor.getValue();
        assertThat(change.getCodCatalogchange()).isEqualTo(CATALOG_CHANGE_ID).hasSizeLessThanOrEqualTo(50);
        assertThat(change.getSglAction()).isEqualTo(RuntimeCatalogAction.UPSERT);
        assertThat(change.getCodAgentdefinition()).isSameAs(definitionReference);
        assertThat(change.getCodRuntimepackage()).isSameAs(runtimePackage);
        assertThat(change.getNumPackageversion()).isEqualTo(5L);
        assertThat(change.getDscPackagefingerprint()).isEqualTo(FINGERPRINT);
        assertThat(change.getDscDeduplicationkey())
                .startsWith("DRC:UPSERT:")
                .hasSize("DRC:UPSERT:".length() + 64)
                .isEqualTo(deduplicationKey());
        assertThat(change.getSglSourceagentstatus()).isEqualTo("ACTIVE");
        assertThat(change.getSglRemovalreason()).isNull();
        verify(catalogRepository, times(1)).nextCatalogChangeId();
        verify(catalogRepository, times(1)).append(any());
    }

    @Test
    void equivalentReplayReusesExistingChangeWithoutGeneratingOrPersistingAnother() {
        AgentRuntimeCatalogChange existing = equivalentExistingChange();
        when(catalogRepository.findByDeduplicationKey(deduplicationKey())).thenReturn(Optional.of(existing));

        assertThat(publisher.appendUpsert(AGENT_ID, runtimePackage, ACTIVATED_AT)).isSameAs(existing);

        verify(catalogRepository, never()).nextCatalogChangeId();
        verify(catalogRepository, never()).append(any());
    }

    @Test
    void conflictingDeduplicationRowFailsWithoutGeneratingOrPersisting() {
        AgentRuntimeCatalogChange existing = equivalentExistingChange();
        when(existing.getDscPackagefingerprint()).thenReturn("b".repeat(64));
        when(catalogRepository.findByDeduplicationKey(deduplicationKey())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> publisher.appendUpsert(AGENT_ID, runtimePackage, ACTIVATED_AT))
                .isInstanceOf(AgentActivationTechnicalException.class)
                .hasMessageContaining("conflicts");

        verify(catalogRepository, never()).nextCatalogChangeId();
        verify(catalogRepository, never()).append(any());
    }

    @Test
    void acceptedActivationTransitionsAndPublishesWithinFinalizer() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentRuntimePackageRepository packageRepository = mock(AgentRuntimePackageRepository.class);
        RuntimeCatalogLifecyclePublisher catalogPublisher = mock(RuntimeCatalogLifecyclePublisher.class);
        AgentDefinition definition = definition("READY");
        AgentDefinitionStatus active = status("ACTIVE");
        when(definitionRepository.findByDefinitionIdForUpdate(AGENT_ID)).thenReturn(Optional.of(definition));
        when(definitionRepository.statusReference("ACTIVE")).thenReturn(active);
        when(packageRepository.findByIdOptional(PACKAGE_ID)).thenReturn(Optional.of(runtimePackage));

        AgentActivationFinalizationService finalizer = new AgentActivationFinalizationService();
        finalizer.agentDefinitionRepository = definitionRepository;
        finalizer.runtimePackageRepository = packageRepository;
        finalizer.runtimeCatalogLifecyclePublisher = catalogPublisher;

        assertThat(finalizer.finalizeAcceptedActivation(
                AGENT_ID, PACKAGE_ID, 5L, "SUBMISSION-5", FINGERPRINT, ACTIVATED_AT))
                .isEqualTo(AgentActivationFinalizationService.FinalizationResult.COMMITTED);
        assertThat(definition.getSglStatus()).isSameAs(active);
        assertThat(definition.getCodCurrentruntimepackage()).isEqualTo(PACKAGE_ID);
        verify(catalogPublisher).appendUpsert(AGENT_ID, runtimePackage, ACTIVATED_AT);
    }

    @Test
    void catalogFailurePreventsSuccessfulFinalizationResult() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentRuntimePackageRepository packageRepository = mock(AgentRuntimePackageRepository.class);
        RuntimeCatalogLifecyclePublisher catalogPublisher = mock(RuntimeCatalogLifecyclePublisher.class);
        AgentDefinition definition = definition("READY");
        when(definitionRepository.findByDefinitionIdForUpdate(AGENT_ID)).thenReturn(Optional.of(definition));
        when(definitionRepository.statusReference("ACTIVE")).thenReturn(status("ACTIVE"));
        when(packageRepository.findByIdOptional(PACKAGE_ID)).thenReturn(Optional.of(runtimePackage));
        when(catalogPublisher.appendUpsert(AGENT_ID, runtimePackage, ACTIVATED_AT))
                .thenThrow(new AgentActivationTechnicalException("catalog persistence failed"));

        AgentActivationFinalizationService finalizer = new AgentActivationFinalizationService();
        finalizer.agentDefinitionRepository = definitionRepository;
        finalizer.runtimePackageRepository = packageRepository;
        finalizer.runtimeCatalogLifecyclePublisher = catalogPublisher;

        assertThatThrownBy(() -> finalizer.finalizeAcceptedActivation(
                AGENT_ID, PACKAGE_ID, 5L, "SUBMISSION-5", FINGERPRINT, ACTIVATED_AT))
                .isInstanceOf(AgentActivationTechnicalException.class)
                .hasMessageContaining("catalog persistence failed");
    }

    private AgentRuntimePackage runtimePackage() {
        AgentRuntimePackage value = mock(AgentRuntimePackage.class);
        AgentDefinition owner = mock(AgentDefinition.class);
        when(owner.getCodAgentdefinition()).thenReturn(AGENT_ID);
        when(value.getCodRuntimepackage()).thenReturn(PACKAGE_ID);
        when(value.getCodAgentdefinition()).thenReturn(owner);
        when(value.getNumPackageversion()).thenReturn(5L);
        when(value.getCodSubmissionid()).thenReturn("SUBMISSION-5");
        when(value.getDscPackagefingerprint()).thenReturn(FINGERPRINT);
        return value;
    }

    private AgentRuntimeCatalogChange equivalentExistingChange() {
        AgentRuntimeCatalogChange existing = mock(AgentRuntimeCatalogChange.class);
        when(existing.getCodCatalogchange()).thenReturn(CATALOG_CHANGE_ID);
        when(existing.getSglAction()).thenReturn(RuntimeCatalogAction.UPSERT);
        when(existing.getCodAgentdefinition()).thenReturn(definitionReference);
        when(definitionReference.getCodAgentdefinition()).thenReturn(AGENT_ID);
        when(existing.getCodRuntimepackage()).thenReturn(runtimePackage);
        when(existing.getNumPackageversion()).thenReturn(5L);
        when(existing.getDscPackagefingerprint()).thenReturn(FINGERPRINT);
        when(existing.getSglSourceagentstatus()).thenReturn("ACTIVE");
        when(existing.getDtSourceupdatedat()).thenReturn(ACTIVATED_AT);
        when(existing.getSglRemovalreason()).thenReturn(null);
        return existing;
    }

    private AgentDefinition definition(String currentStatus) {
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition(AGENT_ID);
        definition.setSglStatus(status(currentStatus));
        return definition;
    }

    private AgentDefinitionStatus status(String value) {
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus(value);
        return status;
    }

    private String deduplicationKey() {
        String material = String.join("\u0000", "UPSERT", AGENT_ID, PACKAGE_ID, "5",
                FINGERPRINT.toLowerCase(Locale.ROOT), LIFECYCLE_INSTANT.toString());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(material.getBytes(StandardCharsets.UTF_8));
            return "DRC:UPSERT:" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new AssertionError(ex);
        }
    }
}
