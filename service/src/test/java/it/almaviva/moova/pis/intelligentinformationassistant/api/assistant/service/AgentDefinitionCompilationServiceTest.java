package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStep;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentCompilationRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactSignatureStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentActivationType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStepId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentDefinitionCompilationServiceTest {

    @Test
    void rejectsBlankAgentDefinitionId() {
        AgentDefinitionService service = service(mock(AgentDefinitionRepository.class), mock(AgentCompilationRepository.class));

        assertThatThrownBy(() -> service.getAgentDefinitionCompilation(" "))
                .isInstanceOf(AgentDefinitionInvalidRequestException.class)
                .extracting(ex -> ((AgentDefinitionInvalidRequestException) ex).source())
                .isEqualTo("agentDefinitionId");
    }

    @Test
    void rejectsTooLongAgentDefinitionId() {
        AgentDefinitionService service = service(mock(AgentDefinitionRepository.class), mock(AgentCompilationRepository.class));

        assertThatThrownBy(() -> service.getAgentDefinitionCompilation("A".repeat(51)))
                .isInstanceOf(AgentDefinitionInvalidRequestException.class)
                .hasMessageContaining("exceeds 50 characters");
    }

    @Test
    void throwsNotFoundWhenAgentDefinitionDoesNotExist() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentCompilationRepository.class));
        when(definitionRepository.findByDefinitionId("AGDF404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAgentDefinitionCompilation("AGDF404"))
                .isInstanceOf(AgentDefinitionNotFoundException.class)
                .hasMessageContaining("Agent Definition not found");
    }

    @Test
    void throwsNotFoundWhenAgentDefinitionHasNoCompilation() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition()));
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAgentDefinitionCompilation("AGDF1"))
                .isInstanceOf(AgentDefinitionNotFoundException.class)
                .hasMessageContaining("No compilation found for Agent Definition AGDF1.");
    }

    @Test
    void returnsLatestCompilationResponse() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentCompilation compilation = compilation("READY");
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition()));
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(compilation));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(List.of());

        var response = service.getAgentDefinitionCompilation(" AGDF1 ");

        assertThat(response.getAgentDefinitionId()).isEqualTo("AGDF1");
        assertThat(response.getStatus().toString()).isEqualTo("READY");
        assertThat(response.getSteps()).isEmpty();
        verify(compilationRepository).findLatestByAgentDefinitionId("AGDF1");
        verify(compilationRepository).findStepsByCompilationId("AGCP1");
    }

    @Test
    void compileThrowsNotFoundWhenAgentDefinitionDoesNotExist() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentCompilationRepository.class));
        when(definitionRepository.findByDefinitionId("AGDF404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.compileAgentDefinition("AGDF404", compileRequest(AgentGenerationMode.DSL)))
                .isInstanceOf(AgentDefinitionNotFoundException.class);
    }

    @Test
    void compileRejectsJavaTemplateGenerationMode() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition("DRAFT", "AUTO")));
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);

        assertThatThrownBy(() -> service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.JAVA_TEMPLATE)))
                .isInstanceOf(AgentCompilationRejectedException.class)
                .hasMessageContaining("JAVA_TEMPLATE");
    }

    @Test
    void compileRejectsRunningCompilationEvenWhenForceIsTrue() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentCompilationRequest request = compileRequest(AgentGenerationMode.DSL).force(true);
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition("DRAFT", "AUTO")));
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(true);

        assertThatThrownBy(() -> service.compileAgentDefinition("AGDF1", request))
                .isInstanceOf(AgentCompilationRejectedException.class)
                .hasMessageContaining("Force recompilation of a running compilation is not implemented yet.");
    }

    @Test
    void compileRejectsActiveAgentDefinition() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentCompilationRepository.class));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition("ACTIVE", "AUTO")));

        assertThatThrownBy(() -> service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL)))
                .isInstanceOf(AgentCompilationRejectedException.class)
                .hasMessageContaining("cannot be compiled while status is ACTIVE");
    }

    @Test
    void compileGeneratesEventDslArtifactAndMarksDefinitionReady() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentDefinition definition = definition("DRAFT", "AUTO");
        AgentCompilation created = compilation("PENDING");
        AgentCompilation ready = compilation("READY");
        ready.setDscCurrentstep("READY");
        Map<String, Object> readyResult = new java.util.LinkedHashMap<>();
        readyResult.put("preconditionsValid", true);
        readyResult.put("artifactGenerated", true);
        readyResult.put("runtimeCompatibilityValidated", true);
        readyResult.put("artifactHashed", true);
        readyResult.put("artifactHash", "sha256:test");
        readyResult.put("artifactUri", "iia-agent-artifact://agent-definitions/AGDF1/compilations/AGCP1/dsl");
        readyResult.put("artifactType", "DSL");
        readyResult.put("schemaVersion", "iia.agent.dsl/v1");
        readyResult.put("dslArtifact", Map.of("schemaVersion", "iia.agent.dsl/v1"));
        readyResult.put("agentDefinitionStatusUpdated", true);
        readyResult.put("agentDefinitionStatus", "READY");
        ready.setJsnResult(readyResult);
        var steps = List.of(
                step(1, "REQUEST_ACCEPTED", "READY"),
                step(2, "VALIDATING_BLUEPRINT", "READY"),
                step(3, "GENERATING_ARTIFACT", "READY"),
                step(4, "STATIC_ANALYSIS", "READY"),
                step(5, "SIGNING", "READY"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        stubMarkReady(definitionRepository, definition);
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), eq(false), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(ready));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var response = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));

        assertThat(response.getStatus().toString()).isEqualTo("READY");
        assertThat(response.getCurrentStep()).isEqualTo("READY");
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getName)
                .containsExactly("REQUEST_ACCEPTED", "VALIDATING_BLUEPRINT", "GENERATING_ARTIFACT", "STATIC_ANALYSIS", "SIGNING");
        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getStatus)
                .containsExactly(
                        AgentCompilationStep.StatusEnum.SUCCESS,
                        AgentCompilationStep.StatusEnum.SUCCESS,
                        AgentCompilationStep.StatusEnum.SUCCESS,
                        AgentCompilationStep.StatusEnum.SUCCESS,
                        AgentCompilationStep.StatusEnum.SUCCESS);
        assertThat(response.getArtifact()).isNotNull();
        assertThat(response.getArtifact().getArtifactHash()).startsWith("sha256:");
        assertThat(response.getArtifact().getArtifactUri())
                .isEqualTo("iia-agent-artifact://agent-definitions/AGDF1/compilations/AGCP1/dsl");
        assertThat(response.getArtifact().getSignatureStatus().toString()).isEqualTo("SIGNED");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> requestJsonCaptor = ArgumentCaptor.forClass(Map.class);
        verify(compilationRepository).createCompilation(eq("AGDF1"), eq("DSL"), eq(false), requestJsonCaptor.capture(), eq(null));
        assertThat(requestJsonCaptor.getValue())
                .containsEntry("force", false)
                .containsEntry("generationMode", "DSL")
                .containsEntry("requestedGenerationMode", "DSL")
                .containsEntry("effectiveGenerationMode", "DSL")
                .containsEntry("runSimulation", false)
                .containsEntry("note", "First manual DSL compilation skeleton")
                .containsEntry("skeleton", true);

        ArgumentCaptor<Integer> stepOrderCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> stepNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(compilationRepository, org.mockito.Mockito.times(5)).addStep(
                eq("AGCP1"),
                stepOrderCaptor.capture(),
                stepNameCaptor.capture(),
                anyString(),
                anyString(),
                any(),
                any(),
                any());
        assertThat(stepOrderCaptor.getAllValues()).containsExactly(1, 2, 3, 4, 5);
        assertThat(stepNameCaptor.getAllValues()).containsExactly("REQUEST_ACCEPTED", "VALIDATING_BLUEPRINT", "GENERATING_ARTIFACT", "STATIC_ANALYSIS", "SIGNING");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> resultCaptor = ArgumentCaptor.forClass(Map.class);
        verify(compilationRepository).markCompleted(
                eq("AGCP1"),
                eq("READY"),
                eq("READY"),
                resultCaptor.capture(),
                any(OffsetDateTime.class));
        assertThat(resultCaptor.getValue())
                .containsEntry("preconditionsValid", true)
                .containsEntry("artifactGenerated", true)
                .containsEntry("runtimeCompatibilityValidated", true)
                .containsEntry("artifactHashed", true)
                .containsEntry("schemaVersion", "iia.agent.dsl/v1")
                .containsEntry("agentDefinitionStatusUpdated", true)
                .containsEntry("agentDefinitionStatus", "READY");
        assertThat(resultCaptor.getValue().get("dslArtifact")).isInstanceOf(Map.class);
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("READY");
        assertThat(definition.getDscArtifacthash()).startsWith("sha256:");
        assertThat(definition.getSglArtifacttype().getSglArtifacttype()).isEqualTo("DSL");
        assertThat(definition.getSglLatestcompilationstatus().getSglStatus()).isEqualTo("READY");
    }

    @Test
    void compileGeneratesScheduledDslArtifactAndMarksDefinitionReady() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentDefinition definition = AgentCompilationTestFixtures.scheduledDefinition();
        AgentCompilation created = compilation("PENDING");
        AgentCompilation ready = compilation("READY");
        ready.setDscCurrentstep("READY");
        var steps = List.of(
                step(1, "REQUEST_ACCEPTED", "READY"),
                step(2, "VALIDATING_BLUEPRINT", "READY"),
                step(3, "GENERATING_ARTIFACT", "READY"),
                step(4, "STATIC_ANALYSIS", "READY"),
                step(5, "SIGNING", "READY"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        stubMarkReady(definitionRepository, definition);
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), eq(false), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(ready));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var response = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));

        assertThat(response.getStatus().toString()).isEqualTo("READY");
        assertThat(response.getCurrentStep()).isEqualTo("READY");
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getName)
                .containsExactly("REQUEST_ACCEPTED", "VALIDATING_BLUEPRINT", "GENERATING_ARTIFACT", "STATIC_ANALYSIS", "SIGNING");
        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getStatus)
                .containsExactly(
                        AgentCompilationStep.StatusEnum.SUCCESS,
                        AgentCompilationStep.StatusEnum.SUCCESS,
                        AgentCompilationStep.StatusEnum.SUCCESS,
                        AgentCompilationStep.StatusEnum.SUCCESS,
                        AgentCompilationStep.StatusEnum.SUCCESS);
        assertThat(response.getArtifact()).isNotNull();
        assertThat(response.getArtifact().getArtifactHash()).startsWith("sha256:");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> resultCaptor = ArgumentCaptor.forClass(Map.class);
        verify(compilationRepository).markCompleted(
                eq("AGCP1"),
                eq("READY"),
                eq("READY"),
                resultCaptor.capture(),
                any(OffsetDateTime.class));
        assertThat(resultCaptor.getValue())
                .containsEntry("preconditionsValid", true)
                .containsEntry("artifactGenerated", true)
                .containsEntry("runtimeCompatibilityValidated", true)
                .containsEntry("artifactHashed", true)
                .containsEntry("interpreterType", "SCHEDULED_INTERPRETER")
                .containsEntry("accessMode", "SERVICE_DATA_API_SNAPSHOT")
                .containsEntry("schemaVersion", "iia.agent.dsl/v1");
        @SuppressWarnings("unchecked")
        Map<String, Object> dslArtifact = (Map<String, Object>) resultCaptor.getValue().get("dslArtifact");
        @SuppressWarnings("unchecked")
        Map<String, Object> runtime = (Map<String, Object>) dslArtifact.get("runtime");
        assertThat(runtime).containsEntry("executionModel", "SCHEDULED_POLLING");
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("READY");
        assertThat(definition.getSglLatestcompilationstatus().getSglStatus()).isEqualTo("READY");
    }

    @Test
    void compileRejectsFunctionalPreconditionsAndDoesNotGenerateArtifactStep() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentDefinition definition = definition("DRAFT", "AUTO");
        definition.setJsnBlueprint(null);
        AgentCompilation created = compilation("PENDING");
        AgentCompilation rejected = compilation("REJECTED");
        rejected.setDscCurrentstep("VALIDATING_BLUEPRINT");
        rejected.setDscErrormessage("Agent Definition blueprint is missing; compilation cannot continue.");
        var steps = List.of(
                step(1, "REQUEST_ACCEPTED", "READY"),
                step(2, "VALIDATING_BLUEPRINT", "REJECTED"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        stubMarkRejected(definitionRepository, definition);
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), eq(false), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(rejected));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var response = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));

        assertThat(response.getStatus().toString()).isEqualTo("REJECTED");
        assertThat(response.getCurrentStep()).isEqualTo("VALIDATING_BLUEPRINT");
        assertThat(response.getErrors()).containsExactly("Agent Definition blueprint is missing; compilation cannot continue.");
        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getName)
                .containsExactly("REQUEST_ACCEPTED", "VALIDATING_BLUEPRINT");
        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getStatus)
                .containsExactly(
                        AgentCompilationStep.StatusEnum.SUCCESS,
                        AgentCompilationStep.StatusEnum.FAILED);

        ArgumentCaptor<String> stepNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(compilationRepository, org.mockito.Mockito.times(2)).addStep(
                eq("AGCP1"),
                any(Integer.class),
                stepNameCaptor.capture(),
                anyString(),
                anyString(),
                any(),
                any(),
                any());
        assertThat(stepNameCaptor.getAllValues()).doesNotContain("GENERATING_ARTIFACT");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> resultCaptor = ArgumentCaptor.forClass(Map.class);
        verify(compilationRepository).markFailed(
                eq("AGCP1"),
                eq("REJECTED"),
                eq("VALIDATING_BLUEPRINT"),
                eq("Agent Definition blueprint is missing; compilation cannot continue."),
                resultCaptor.capture(),
                any(OffsetDateTime.class));
        assertThat(resultCaptor.getValue())
                .containsEntry("preconditionsValid", false)
                .containsEntry("artifactGenerated", false)
                .containsEntry("agentDefinitionStatusUpdated", false);
        verify(definitionRepository).markCompilationRejected(
                eq("AGDF1"),
                eq("AGCP1"),
                eq("VALIDATING_BLUEPRINT"),
                eq("Agent Definition blueprint is missing; compilation cannot continue."),
                any(OffsetDateTime.class));
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("REJECTED");
    }

    @Test
    void compileRejectsRuntimeIncompatibleDslAndDoesNotAddSigningStep() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDslRuntimeCompatibilityValidator runtimeValidator = mock(AgentDslRuntimeCompatibilityValidator.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        service.agentDslRuntimeCompatibilityValidator = runtimeValidator;
        AgentDefinition definition = definition("DRAFT", "AUTO");
        AgentCompilation created = compilation("PENDING");
        AgentCompilation rejected = compilation("REJECTED");
        rejected.setDscCurrentstep("STATIC_ANALYSIS");
        rejected.setDscErrormessage("Unsupported DSL operator MAGIC_OPERATOR.");
        var steps = List.of(
                step(1, "REQUEST_ACCEPTED", "READY"),
                step(2, "VALIDATING_BLUEPRINT", "READY"),
                step(3, "GENERATING_ARTIFACT", "READY"),
                step(4, "STATIC_ANALYSIS", "REJECTED"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        stubMarkRejected(definitionRepository, definition);
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), eq(false), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(rejected));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);
        when(runtimeValidator.validate(any())).thenReturn(new AgentDslRuntimeCompatibilityValidationResult(
                false,
                List.of("Unsupported DSL operator MAGIC_OPERATOR."),
                List.of(),
                "iia.agent.dsl/v1",
                "DSL",
                "EVENT_INTERPRETER",
                "EVENT",
                "KAFKA_EVENT",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "STATELESS_EVENT_MATCH",
                Map.of()));

        var response = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));

        assertThat(response.getStatus().toString()).isEqualTo("REJECTED");
        assertThat(response.getCurrentStep()).isEqualTo("STATIC_ANALYSIS");
        assertThat(response.getErrors()).containsExactly("Unsupported DSL operator MAGIC_OPERATOR.");
        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getName)
                .containsExactly("REQUEST_ACCEPTED", "VALIDATING_BLUEPRINT", "GENERATING_ARTIFACT", "STATIC_ANALYSIS");
        ArgumentCaptor<String> stepNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(compilationRepository, org.mockito.Mockito.times(4)).addStep(
                eq("AGCP1"),
                any(Integer.class),
                stepNameCaptor.capture(),
                anyString(),
                anyString(),
                any(),
                any(),
                any());
        assertThat(stepNameCaptor.getAllValues()).doesNotContain("SIGNING");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> resultCaptor = ArgumentCaptor.forClass(Map.class);
        verify(compilationRepository).markFailed(
                eq("AGCP1"),
                eq("REJECTED"),
                eq("STATIC_ANALYSIS"),
                eq("Unsupported DSL operator MAGIC_OPERATOR."),
                resultCaptor.capture(),
                any(OffsetDateTime.class));
        assertThat(resultCaptor.getValue())
                .containsEntry("runtimeCompatibilityValidated", false)
                .containsEntry("agentDefinitionStatusUpdated", false);
        assertThat(resultCaptor.getValue().get("runtimeCompatibilityErrors"))
                .isEqualTo(List.of("Unsupported DSL operator MAGIC_OPERATOR."));
        verify(definitionRepository).markCompilationRejected(
                eq("AGDF1"),
                eq("AGCP1"),
                eq("STATIC_ANALYSIS"),
                eq("Runtime compatibility rejected the generated DSL artifact."),
                any(OffsetDateTime.class));
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("REJECTED");
    }

    @Test
    void getCompilationAfterCompileReadsSameLatestCompilation() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentDefinition definition = definition("DRAFT", "AUTO");
        AgentCompilation created = compilation("PENDING");
        AgentCompilation ready = compilation("READY");
        ready.setDscCurrentstep("READY");
        var steps = List.of(
                step(1, "REQUEST_ACCEPTED", "READY"),
                step(2, "VALIDATING_BLUEPRINT", "READY"),
                step(3, "GENERATING_ARTIFACT", "READY"),
                step(4, "STATIC_ANALYSIS", "READY"),
                step(5, "SIGNING", "READY"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        stubMarkReady(definitionRepository, definition);
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), anyBoolean(), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(ready), Optional.of(ready));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var postResponse = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));
        var getResponse = service.getAgentDefinitionCompilation("AGDF1");
        var detail = service.getAgentDefinition("AGDF1");

        assertThat(getResponse).isEqualTo(postResponse);
        assertThat(getResponse.getStatus().toString()).isEqualTo("READY");
        assertThat(getResponse.getCurrentStep()).isEqualTo("READY");
        assertThat(getResponse.getSteps()).hasSize(5);
        assertThat(getResponse.getArtifact()).isNotNull();
        assertThat(detail.getStatus().toString()).isEqualTo("READY");
        assertThat(detail.getArtifact()).isNotNull();
        assertThat(detail.getArtifact().getArtifactType().toString()).isEqualTo("DSL");
        assertThat(detail.getArtifact().getArtifactHash()).startsWith("sha256:");
        assertThat(detail.getCompilation().getStatus().toString()).isEqualTo("READY");
        assertThat(detail.getCompilation().getCurrentStep()).isEqualTo("READY");
        assertThat(detail.getCompilation().getSteps()).hasSize(5);
        assertThat(detail.getCompilation().getSteps())
                .extracting(AgentCompilationStep::getStatus)
                .containsOnly(AgentCompilationStep.StatusEnum.SUCCESS);
        assertThat(detail.getCompilation().getArtifact()).isNotNull();
        assertThat(detail.getRuntimeContract().getRuntimeImage()).isEqualTo("STANDARD_AGENT_DSL_EVALUATOR");
        assertThat(detail.getRuntimeContract().getSdkVersion()).isEqualTo("iia.agent.dsl/v1");
    }

    @Test
    void getCompilationAfterRejectedCompileReadsRejectedLatestCompilation() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentDefinition definition = definition("DRAFT", "AUTO");
        definition.setJsnRuntimecontract(null);
        AgentCompilation created = compilation("PENDING");
        AgentCompilation rejected = compilation("REJECTED");
        rejected.setDscCurrentstep("VALIDATING_BLUEPRINT");
        rejected.setDscErrormessage("Agent Definition runtimeContract is missing; compilation cannot continue.");
        var steps = List.of(step(1, "REQUEST_ACCEPTED", "READY"), step(2, "VALIDATING_BLUEPRINT", "REJECTED"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        stubMarkRejected(definitionRepository, definition);
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), anyBoolean(), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(rejected), Optional.of(rejected));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var postResponse = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));
        var getResponse = service.getAgentDefinitionCompilation("AGDF1");

        assertThat(getResponse).isEqualTo(postResponse);
        assertThat(getResponse.getStatus().toString()).isEqualTo("REJECTED");
        assertThat(getResponse.getCurrentStep()).isEqualTo("VALIDATING_BLUEPRINT");
        assertThat(getResponse.getSteps())
                .extracting(AgentCompilationStep::getName)
                .containsExactly("REQUEST_ACCEPTED", "VALIDATING_BLUEPRINT");
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("REJECTED");
    }

    private AgentDefinitionService service(
            AgentDefinitionRepository definitionRepository,
            AgentCompilationRepository compilationRepository) {
        AgentDefinitionService service = new AgentDefinitionService();
        service.agentDefinitionRepository = definitionRepository;
        service.agentCompilationRepository = compilationRepository;
        service.agentDefinitionMapper = new AgentDefinitionMapper();
        service.agentDefinitionMapper.agentProfileMapper = new AgentProfileMapper();
        service.agentCompilationMapper = new AgentCompilationMapper();
        service.agentCompilationMapper.stepStatusMapper = new AgentCompilationStepStatusMapper();
        service.agentCompilationPreconditionValidator = new AgentCompilationPreconditionValidator();
        service.agentDslArtifactBuilder = new AgentDslArtifactBuilder();
        service.agentDslRuntimeCompatibilityValidator = new AgentDslRuntimeCompatibilityValidator();
        service.agentArtifactHashService = new AgentArtifactHashService();
        return service;
    }

    private AgentDefinition definition() {
        return definition("DRAFT", "NONE");
    }

    private AgentDefinition definition(String statusValue, String generationModeValue) {
        AgentDefinition definition = AgentCompilationTestFixtures.eventDefinition();
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus(statusValue);
        definition.setSglStatus(status);
        it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode generationMode =
                new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode();
        generationMode.setSglGenerationmode(generationModeValue);
        definition.setSglGenerationmode(generationMode);
        AgentArtifactType artifactType = new AgentArtifactType();
        artifactType.setSglArtifacttype("NONE");
        definition.setSglArtifacttype(artifactType);
        return definition;
    }

    private void stubMarkReady(AgentDefinitionRepository repository, AgentDefinition definition) {
        doAnswer(invocation -> {
            String compilationId = invocation.getArgument(1);
            String artifactUri = invocation.getArgument(2);
            String artifactHash = invocation.getArgument(3);
            String runtimeImage = invocation.getArgument(4);
            String sdkVersion = invocation.getArgument(5);
            String implementationSummary = invocation.getArgument(6);
            @SuppressWarnings("unchecked")
            Map<String, Object> dslArtifact = invocation.getArgument(7, Map.class);
            OffsetDateTime completedAt = invocation.getArgument(8);
            AgentCompilation latestCompilation = compilation("READY");
            latestCompilation.setCodAgentcompilation(compilationId);
            latestCompilation.setDscCurrentstep("READY");
            latestCompilation.setDtCompletedat(completedAt);

            definition.setSglStatus(definitionStatus("READY"));
            definition.setSglArtifacttype(artifactType("DSL"));
            definition.setDscArtifacturi(artifactUri);
            definition.setDscArtifacthash(artifactHash);
            definition.setSglSignaturestatus(signatureStatus("SIGNED"));
            definition.setDscRuntimeimage(runtimeImage);
            definition.setDscSdkversion(sdkVersion);
            definition.setDscImplementationsummary(implementationSummary);
            definition.setCodLatestcompilation(latestCompilation);
            definition.setSglLatestcompilationstatus(compilationStatus("READY"));
            definition.setDscLatestcompilationstep("READY");
            definition.setDtLatestcompilationcompletedat(completedAt);
            definition.setJsnDslpreview(dslArtifact);
            return null;
        }).when(repository).markCompilationReady(
                eq("AGDF1"),
                eq("AGCP1"),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(),
                any(OffsetDateTime.class));
    }

    private void stubMarkRejected(AgentDefinitionRepository repository, AgentDefinition definition) {
        doAnswer(invocation -> {
            String compilationId = invocation.getArgument(1);
            String latestStep = invocation.getArgument(2);
            String implementationSummary = invocation.getArgument(3);
            OffsetDateTime completedAt = invocation.getArgument(4);
            AgentCompilation latestCompilation = compilation("REJECTED");
            latestCompilation.setCodAgentcompilation(compilationId);

            definition.setSglStatus(definitionStatus("REJECTED"));
            definition.setSglArtifacttype(artifactType("NONE"));
            definition.setDscArtifacturi(null);
            definition.setDscArtifacthash(null);
            definition.setSglSignaturestatus(signatureStatus("NOT_SIGNED"));
            definition.setDscRuntimeimage(null);
            definition.setDscSdkversion(null);
            definition.setDscImplementationsummary(implementationSummary);
            definition.setCodLatestcompilation(latestCompilation);
            definition.setSglLatestcompilationstatus(compilationStatus("REJECTED"));
            definition.setDscLatestcompilationstep(latestStep);
            definition.setDtLatestcompilationcompletedat(completedAt);
            return null;
        }).when(repository).markCompilationRejected(
                eq("AGDF1"),
                eq("AGCP1"),
                anyString(),
                anyString(),
                any(OffsetDateTime.class));
    }

    private AgentDefinitionStatus definitionStatus(String value) {
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus(value);
        return status;
    }

    private AgentArtifactType artifactType(String value) {
        AgentArtifactType artifactType = new AgentArtifactType();
        artifactType.setSglArtifacttype(value);
        return artifactType;
    }

    private AgentArtifactSignatureStatus signatureStatus(String value) {
        AgentArtifactSignatureStatus signatureStatus = new AgentArtifactSignatureStatus();
        signatureStatus.setSglSignaturestatus(value);
        return signatureStatus;
    }

    private AgentCompilationRequest compileRequest(AgentGenerationMode generationMode) {
        return new AgentCompilationRequest()
                .force(false)
                .generationMode(generationMode)
                .runSimulation(false)
                .note("First manual DSL compilation skeleton");
    }

    private AgentCompilation compilation(String statusValue) {
        AgentCompilation compilation = new AgentCompilation();
        compilation.setCodAgentcompilation("AGCP1");
        AgentCompilationStatus status = new AgentCompilationStatus();
        status.setSglStatus(statusValue);
        compilation.setSglStatus(status);
        return compilation;
    }

    private it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStep step(
            int order,
            String name,
            String statusValue) {
        AgentCompilationStepId id = new AgentCompilationStepId();
        id.setCodAgentcompilation("AGCP1");
        id.setNumSteporder(order);
        it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStep step =
                new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStep();
        step.setId(id);
        step.setDscStepname(name);
        step.setSglStatus(compilationStatus(statusValue));
        return step;
    }

    private AgentCompilationStatus compilationStatus(String statusValue) {
        AgentCompilationStatus status = new AgentCompilationStatus();
        status.setSglStatus(statusValue);
        return status;
    }
}
