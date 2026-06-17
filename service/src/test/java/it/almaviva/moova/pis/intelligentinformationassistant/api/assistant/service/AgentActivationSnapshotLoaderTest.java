package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentCompilationRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentActivationType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactSignatureStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionAllowedTool;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionAllowedToolId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionDayOfWeek;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionDayOfWeekId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionRequiredSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionRequiredSourceId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.DataSourceCategory;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.DayOfWeek;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentActivationSnapshotLoaderTest {

    @Test
    void draftWithoutCompilationBuildsSnapshotWithoutDslArtifact() {
        Fixture fixture = fixture(definition("DRAFT", "EVENT_INTERPRETER", "EVENT", "ServiceDataV2"));
        fixture.noCompilation();

        AgentActivationSnapshot snapshot = fixture.load();

        assertThat(snapshot.status()).isEqualTo("DRAFT");
        assertThat(snapshot.compilationSummary().latestCompilationReferenceId()).isNull();
        assertThat(snapshot.latestCompilation()).isNull();
        assertThat(snapshot.dslArtifact()).isNull();
        assertThat(snapshot.profile().id()).isEqualTo("MEDIUM");
        assertThat(snapshot.activationPolicy().activationType()).isEqualTo("CONTINUOUS");
        assertThat(snapshot.activationPolicy().activationPolicyJson()).containsEntry("type", "CONTINUOUS");
    }

    @Test
    void readyEventCompiledSnapshotContainsExactCompilationAndDslArtifact() {
        AgentDefinition definition = definition("READY", "EVENT_INTERPRETER", "EVENT", "ServiceDataV2");
        definition.setDscArtifacturi("iia-agent-artifact://agent-definitions/AGDF1/compilations/AGCP1/dsl");
        definition.setDscArtifacthash("sha256:event");
        definition.setSglSignaturestatus(signatureStatus("SIGNED"));
        definition.setDscRuntimeimage("STANDARD_AGENT_DSL_EVALUATOR");
        definition.setDscSdkversion("iia.agent.dsl/v1");
        definition.setDscImplementationsummary("Compiled event DSL.");
        Fixture fixture = fixture(definition);
        fixture.sources(source("SERVICE_DATA"));
        fixture.noTools();
        fixture.compilation(compilation("AGCP1", "READY", Map.of(
                "artifactType", "DSL",
                "dslArtifact", Map.of(
                        "schemaVersion", "iia.agent.dsl/v1",
                        "triggerType", "EVENT",
                        "runtime", Map.of("inputModel", "ServiceDataV2")))));

        AgentActivationSnapshot snapshot = fixture.load();

        assertThat(snapshot.alert().alertId()).isEqualTo("ALRT1");
        assertThat(snapshot.profile().name()).isEqualTo("Medium profile");
        assertThat(snapshot.requirements().requiredSources()).extracting(AgentActivationSnapshot.AgentActivationSourceSnapshot::source)
                .containsExactly("SERVICE_DATA");
        assertThat(snapshot.requirements().allowedTools()).isEmpty();
        assertThat(snapshot.compilationSummary().latestCompilationReferenceId()).isEqualTo("AGCP1");
        assertThat(snapshot.latestCompilation().compilationId()).isEqualTo("AGCP1");
        assertThat(snapshot.latestCompilation().resultJson()).containsKey("dslArtifact");
        assertThat(snapshot.dslArtifact()).containsEntry("schemaVersion", "iia.agent.dsl/v1");
        assertThat(snapshot.artifact().artifactUri()).contains("AGCP1");
        verify(fixture.compilationRepository).findByCompilationId("AGCP1");
        verify(fixture.compilationRepository, never()).findLatestByAgentDefinitionId("AGDF1");
    }

    @Test
    void readyScheduledCompiledSnapshotContainsAllowedToolAndScheduledDsl() {
        AgentDefinition definition = definition("READY", "SCHEDULED_INTERPRETER", "SCHEDULE", "ServiceDataStopPointJourneysV2");
        definition.setJsnAllowedtools(List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"));
        Fixture fixture = fixture(definition);
        fixture.sources(source("SERVICE_DATA"));
        fixture.tools(tool("SERVICE_DATA_API.POST_/v2/stoppointjourneys"));
        fixture.compilation(compilation("AGCP1", "READY", Map.of(
                "dslArtifact", Map.of(
                        "schemaVersion", "iia.agent.dsl/v1",
                        "triggerType", "SCHEDULE",
                        "accessMode", "SERVICE_DATA_API_SNAPSHOT"))));

        AgentActivationSnapshot snapshot = fixture.load();

        assertThat(snapshot.interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(snapshot.inputModel()).isEqualTo("ServiceDataStopPointJourneysV2");
        assertThat(snapshot.requirements().allowedTools()).extracting(AgentActivationSnapshot.AgentActivationAllowedToolSnapshot::toolName)
                .containsExactly("SERVICE_DATA_API.POST_/v2/stoppointjourneys");
        assertThat(snapshot.requirements().requiredSources()).extracting(AgentActivationSnapshot.AgentActivationSourceSnapshot::source)
                .containsExactly("SERVICE_DATA");
        assertThat(snapshot.latestCompilation().compilationId()).isEqualTo("AGCP1");
        assertThat(snapshot.dslArtifact()).containsEntry("triggerType", "SCHEDULE");
    }

    @Test
    void dailyWindowPolicyLoadsDatesTimesTimezoneAndDeduplicatedDays() {
        AgentDefinition definition = definition("DRAFT", "SCHEDULED_INTERPRETER", "SCHEDULE", "ServiceDataStopPointJourneysV2");
        definition.setSglActivationtype(activationType("DAILY_WINDOW"));
        definition.setDValidfromdate(LocalDate.parse("2026-06-01"));
        definition.setDValidtodate(LocalDate.parse("2026-06-30"));
        definition.setTDailystarttime(LocalTime.parse("08:30"));
        definition.setTDailyendtime(LocalTime.parse("18:45"));
        definition.setDscTimezone("Europe/Rome");
        Fixture fixture = fixture(definition);
        fixture.days(day("MONDAY"), day("MONDAY"), day("WEDNESDAY"));
        fixture.noCompilation();

        AgentActivationSnapshot.AgentActivationPolicySnapshot policy = fixture.load().activationPolicy();

        assertThat(policy.validFromDate()).isEqualTo(LocalDate.parse("2026-06-01"));
        assertThat(policy.validToDate()).isEqualTo(LocalDate.parse("2026-06-30"));
        assertThat(policy.dailyStartTime()).isEqualTo(LocalTime.parse("08:30"));
        assertThat(policy.dailyEndTime()).isEqualTo(LocalTime.parse("18:45"));
        assertThat(policy.timezone()).isEqualTo("Europe/Rome");
        assertThat(policy.daysOfWeek()).containsExactly("MONDAY", "WEDNESDAY");
    }

    @Test
    void inconsistentCompilationReferenceIsPreservedWithoutFallback() {
        Fixture fixture = fixture(definition("READY", "EVENT_INTERPRETER", "EVENT", "ServiceDataV2"));
        fixture.compilationReference("AGCP_MISSING");
        when(fixture.compilationRepository.findByCompilationId("AGCP_MISSING")).thenReturn(Optional.empty());

        AgentActivationSnapshot snapshot = fixture.load();

        assertThat(snapshot.compilationSummary().latestCompilationReferenceId()).isEqualTo("AGCP_MISSING");
        assertThat(snapshot.latestCompilation()).isNull();
        verify(fixture.compilationRepository).findByCompilationId("AGCP_MISSING");
        verify(fixture.compilationRepository, never()).findLatestByAgentDefinitionId("AGDF1");
    }

    @Test
    void compilationResultWithoutDslArtifactKeepsCompilationAndResult() {
        Fixture fixture = fixture(definition("READY", "EVENT_INTERPRETER", "EVENT", "ServiceDataV2"));
        fixture.compilation(compilation("AGCP1", "READY", Map.of("artifactGenerated", true)));

        AgentActivationSnapshot snapshot = fixture.load();

        assertThat(snapshot.latestCompilation()).isNotNull();
        assertThat(snapshot.latestCompilation().resultJson()).containsEntry("artifactGenerated", true);
        assertThat(snapshot.dslArtifact()).isNull();
    }

    @Test
    void snapshotIsDetachedAndDoesNotExposeHibernateEntities() {
        Fixture fixture = fixture(definition("READY", "EVENT_INTERPRETER", "EVENT", "ServiceDataV2"));
        fixture.sources(source("SERVICE_DATA"));
        fixture.tools(tool("TOOL_A"));
        fixture.compilation(compilation("AGCP1", "READY", Map.of(
                "dslArtifact", Map.of("nested", Map.of("value", "ok")))));

        AgentActivationSnapshot snapshot = fixture.load();

        assertThatCode(() -> {
            snapshot.profile().recommendedFor().size();
            snapshot.requirements().requiredSources().size();
            snapshot.requirements().allowedTools().getFirst().operationsJson().get("operations");
            snapshot.dslArtifact().get("nested");
        }).doesNotThrowAnyException();
        assertThatCode(() -> {
            if (false) {
                throw new LazyInitializationException("not expected");
            }
        }).doesNotThrowAnyException();
        assertThat(snapshot.profile()).isNotInstanceOf(AgentProfile.class);
        assertThat(snapshot.alert()).isNotInstanceOf(Alert.class);
        assertThatThrownBy(() -> snapshot.requirements().requiredSources().add(
                new AgentActivationSnapshot.AgentActivationSourceSnapshot("X", true, null)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> snapshot.dslArtifact().put("mutate", true))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void loadIsReadOnlyAndDoesNotChangeDefinitionOrCreateRuntimeRows() {
        AgentDefinition definition = definition("READY", "EVENT_INTERPRETER", "EVENT", "ServiceDataV2");
        OffsetDateTime originalUpdatedAt = definition.getDtUpdatedat();
        Fixture fixture = fixture(definition);
        fixture.compilation(compilation("AGCP1", "READY", Map.of("dslArtifact", Map.of("schemaVersion", "iia.agent.dsl/v1"))));

        fixture.load();

        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("READY");
        assertThat(definition.getDtUpdatedat()).isEqualTo(originalUpdatedAt);
        verify(fixture.compilationRepository).findByCompilationId("AGCP1");
        verify(fixture.compilationRepository, never()).createCompilation(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private Fixture fixture(AgentDefinition definition) {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        TransactionSynchronizationRegistry transactionRegistry = mock(TransactionSynchronizationRegistry.class);
        when(transactionRegistry.getTransactionKey()).thenReturn(new Object());
        when(definitionRepository.findActivationSnapshotDefinition("AGDF1")).thenReturn(Optional.of(definition));
        when(definitionRepository.findActivationDaysOfWeek("AGDF1")).thenReturn(List.of());
        when(definitionRepository.findActivationRequiredSources("AGDF1")).thenReturn(List.of());
        when(definitionRepository.findActivationAllowedTools("AGDF1")).thenReturn(List.of());

        AgentActivationSnapshotLoader loader = new AgentActivationSnapshotLoader();
        loader.agentDefinitionRepository = definitionRepository;
        loader.agentCompilationRepository = compilationRepository;
        loader.transactionSynchronizationRegistry = transactionRegistry;
        return new Fixture(loader, definitionRepository, compilationRepository);
    }

    private AgentDefinition definition(String status, String interpreterType, String triggerType, String inputModel) {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-17T10:00:00Z");
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition("AGDF1");
        definition.setDscName("Activation Agent");
        definition.setDscDescription("Activation snapshot fixture");
        definition.setSglStatus(definitionStatus(status));
        definition.setSglGenerationmode(generationMode("DSL"));
        definition.setSglComplexity(complexity("MEDIUM"));
        definition.setCodAlert(alert(interpreterType));
        definition.setNumAlertversion(7);
        definition.setCodAgentprofile(profile(now));
        definition.setSglActivationtype(activationType("CONTINUOUS"));
        definition.setDscTimezone("Europe/Rome");
        definition.setJsnActivationpolicy(Map.of("type", "CONTINUOUS", "timezone", "Europe/Rome"));
        definition.setSglArtifacttype(artifactType("DSL"));
        definition.setSglSignaturestatus(signatureStatus("SIGNED"));
        definition.setDscInputmodel(inputModel);
        definition.setDscOutputmodel("AgentOutput.CANDIDATE_SUGGESTION");
        definition.setJsnRequiredpermissions(List.of(Map.of("permission", "READ_SERVICE_DATA")));
        definition.setJsnGenerationwarnings(List.of("warning"));
        definition.setJsnRuntimecontract(Map.of(
                "interpreterType", interpreterType,
                "triggerType", triggerType,
                "inputModel", inputModel,
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION"));
        definition.setJsnBlueprint(Map.of("triggerType", triggerType));
        definition.setCodCreatedby("operator1");
        definition.setDtCreatedat(now.minusDays(1));
        definition.setDtUpdatedat(now);
        definition.setSglLatestcompilationstatus(compilationStatus("READY"));
        definition.setDscLatestcompilationstep("READY");
        definition.setDtLatestcompilationcompletedat(now.plusMinutes(5));
        return definition;
    }

    private AgentCompilation compilation(String id, String status, Map<String, Object> result) {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-17T11:00:00Z");
        AgentCompilation compilation = new AgentCompilation();
        compilation.setCodAgentcompilation(id);
        compilation.setCodAgentdefinition(new AgentDefinition());
        compilation.getCodAgentdefinition().setCodAgentdefinition("AGDF1");
        compilation.setSglStatus(compilationStatus(status));
        compilation.setDscCurrentstep(status);
        compilation.setDscRequestedmode("DSL");
        compilation.setFlgForce(false);
        compilation.setJsnRequest(Map.of("requestedMode", "DSL"));
        compilation.setJsnResult(result);
        compilation.setCodRequestedby("operator1");
        compilation.setDtRequestedat(now.minusMinutes(5));
        compilation.setDtStartedat(now.minusMinutes(4));
        compilation.setDtCompletedat(now);
        compilation.setDtUpdatedat(now);
        return compilation;
    }

    private Alert alert(String interpreterType) {
        Alert alert = new Alert();
        alert.setCodAlert("ALRT1");
        alert.setDscName("Source alert");
        alert.setSglInterpretertype(interpreterType(interpreterType));
        return alert;
    }

    private AgentProfile profile(OffsetDateTime now) {
        AgentProfile profile = new AgentProfile();
        profile.setCodAgentprofile("MEDIUM");
        profile.setDscName("Medium profile");
        profile.setDscDescription("Balanced runtime profile");
        profile.setFlgEnabled(true);
        profile.setJsnRecommendedfor(new ArrayList<>(List.of("SCHEDULED_INTERPRETER", "EVENT_INTERPRETER")));
        profile.setNumCpurequestmillicores(250);
        profile.setNumCpulimitmillicores(500);
        profile.setNumMemoryrequestmib(512);
        profile.setNumMemorylimitmib(1024);
        profile.setDscNetworkpolicy("TOOL_GATEWAY_ONLY");
        profile.setNumMaxruntimeconcurrency(2);
        profile.setDtCreatedat(now.minusDays(10));
        profile.setDtUpdatedat(now.minusDays(2));
        return profile;
    }

    private AgentDefinitionRequiredSource source(String value) {
        AgentDefinitionRequiredSourceId id = new AgentDefinitionRequiredSourceId();
        id.setCodAgentdefinition("AGDF1");
        id.setSglCategory(value);
        AgentDefinitionRequiredSource source = new AgentDefinitionRequiredSource();
        source.setId(id);
        source.setSglCategory(category(value));
        source.setFlgRequired(true);
        source.setDscDescription("Required source");
        return source;
    }

    private AgentDefinitionAllowedTool tool(String name) {
        AgentDefinitionAllowedToolId id = new AgentDefinitionAllowedToolId();
        id.setCodAgentdefinition("AGDF1");
        id.setDscToolname(name);
        AgentDefinitionAllowedTool tool = new AgentDefinitionAllowedTool();
        tool.setId(id);
        tool.setJsnOperations(Map.of("operations", List.of(name)));
        return tool;
    }

    private AgentDefinitionDayOfWeek day(String value) {
        AgentDefinitionDayOfWeekId id = new AgentDefinitionDayOfWeekId();
        id.setCodAgentdefinition("AGDF1");
        id.setSglDayofweek(value);
        AgentDefinitionDayOfWeek day = new AgentDefinitionDayOfWeek();
        day.setId(id);
        DayOfWeek entity = new DayOfWeek();
        entity.setSglDayofweek(value);
        day.setSglDayofweek(entity);
        return day;
    }

    private AgentDefinitionStatus definitionStatus(String value) {
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus(value);
        return status;
    }

    private AgentGenerationMode generationMode(String value) {
        AgentGenerationMode mode = new AgentGenerationMode();
        mode.setSglGenerationmode(value);
        return mode;
    }

    private AgentComplexity complexity(String value) {
        AgentComplexity complexity = new AgentComplexity();
        complexity.setSglComplexity(value);
        return complexity;
    }

    private AlertInterpreterType interpreterType(String value) {
        AlertInterpreterType type = new AlertInterpreterType();
        type.setSglInterpretertype(value);
        return type;
    }

    private AgentActivationType activationType(String value) {
        AgentActivationType type = new AgentActivationType();
        type.setSglActivationtype(value);
        return type;
    }

    private AgentArtifactType artifactType(String value) {
        AgentArtifactType type = new AgentArtifactType();
        type.setSglArtifacttype(value);
        return type;
    }

    private AgentArtifactSignatureStatus signatureStatus(String value) {
        AgentArtifactSignatureStatus status = new AgentArtifactSignatureStatus();
        status.setSglSignaturestatus(value);
        return status;
    }

    private AgentCompilationStatus compilationStatus(String value) {
        AgentCompilationStatus status = new AgentCompilationStatus();
        status.setSglStatus(value);
        return status;
    }

    private DataSourceCategory category(String value) {
        DataSourceCategory category = new DataSourceCategory();
        category.setSglCategory(value);
        return category;
    }

    private record Fixture(
            AgentActivationSnapshotLoader loader,
            AgentDefinitionRepository definitionRepository,
            AgentCompilationRepository compilationRepository) {

        AgentActivationSnapshot load() {
            return loader.load("AGDF1").orElseThrow();
        }

        void noCompilation() {
            when(definitionRepository.findLatestCompilationReferenceId("AGDF1")).thenReturn(Optional.empty());
        }

        void compilation(AgentCompilation compilation) {
            compilationReference(compilation.getCodAgentcompilation());
            when(compilationRepository.findByCompilationId(compilation.getCodAgentcompilation()))
                    .thenReturn(Optional.of(compilation));
        }

        void compilationReference(String compilationId) {
            when(definitionRepository.findLatestCompilationReferenceId("AGDF1")).thenReturn(Optional.of(compilationId));
        }

        void sources(AgentDefinitionRequiredSource... sources) {
            when(definitionRepository.findActivationRequiredSources("AGDF1")).thenReturn(List.of(sources));
        }

        void noTools() {
            when(definitionRepository.findActivationAllowedTools("AGDF1")).thenReturn(List.of());
        }

        void tools(AgentDefinitionAllowedTool... tools) {
            when(definitionRepository.findActivationAllowedTools("AGDF1")).thenReturn(List.of(tools));
        }

        void days(AgentDefinitionDayOfWeek... days) {
            when(definitionRepository.findActivationDaysOfWeek("AGDF1")).thenReturn(List.of(days));
        }
    }
}
