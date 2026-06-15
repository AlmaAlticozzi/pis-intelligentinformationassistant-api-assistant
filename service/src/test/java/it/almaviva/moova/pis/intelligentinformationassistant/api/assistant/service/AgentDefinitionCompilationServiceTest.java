package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStep;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentCompilationRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
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
    void compileGeneratesEventDslArtifactAndStopsAtSigning() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentDefinition definition = definition("DRAFT", "AUTO");
        AgentCompilation created = compilation("PENDING");
        AgentCompilation failed = compilation("FAILED");
        failed.setDscCurrentstep("SIGNING");
        failed.setDscErrormessage("Artifact hash, signature and Agent Definition update are not implemented yet.");
        failed.setJsnResult(Map.of(
                "preconditionsValid", true,
                "artifactGenerated", true,
                "runtimeCompatibilityValidated", true,
                "artifactType", "DSL",
                "schemaVersion", "iia.agent.dsl/v1",
                "dslArtifact", Map.of("schemaVersion", "iia.agent.dsl/v1"),
                "agentDefinitionStatusUpdated", false,
                "reason", "Artifact hash, signature and Agent Definition update are not implemented yet."));
        var steps = List.of(
                step(1, "REQUEST_ACCEPTED", "READY"),
                step(2, "VALIDATING_BLUEPRINT", "READY"),
                step(3, "GENERATING_ARTIFACT", "READY"),
                step(4, "STATIC_ANALYSIS", "READY"),
                step(5, "SIGNING", "FAILED"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), eq(false), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(failed));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var response = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));

        assertThat(response.getStatus().toString()).isEqualTo("FAILED");
        assertThat(response.getCurrentStep()).isEqualTo("SIGNING");
        assertThat(response.getErrors()).containsExactly("Artifact hash, signature and Agent Definition update are not implemented yet.");
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
                        AgentCompilationStep.StatusEnum.FAILED);

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
        verify(compilationRepository).markFailed(
                eq("AGCP1"),
                eq("FAILED"),
                eq("SIGNING"),
                eq("Artifact hash, signature and Agent Definition update are not implemented yet."),
                resultCaptor.capture(),
                any(OffsetDateTime.class));
        assertThat(resultCaptor.getValue())
                .containsEntry("preconditionsValid", true)
                .containsEntry("artifactGenerated", true)
                .containsEntry("runtimeCompatibilityValidated", true)
                .containsEntry("schemaVersion", "iia.agent.dsl/v1")
                .containsEntry("agentDefinitionStatusUpdated", false);
        assertThat(resultCaptor.getValue().get("dslArtifact")).isInstanceOf(Map.class);
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("DRAFT");
    }

    @Test
    void compileGeneratesScheduledDslArtifactAndStopsAtSigning() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentDefinition definition = AgentCompilationTestFixtures.scheduledDefinition();
        AgentCompilation created = compilation("PENDING");
        AgentCompilation failed = compilation("FAILED");
        failed.setDscCurrentstep("SIGNING");
        failed.setDscErrormessage("Artifact hash, signature and Agent Definition update are not implemented yet.");
        var steps = List.of(
                step(1, "REQUEST_ACCEPTED", "READY"),
                step(2, "VALIDATING_BLUEPRINT", "READY"),
                step(3, "GENERATING_ARTIFACT", "READY"),
                step(4, "STATIC_ANALYSIS", "READY"),
                step(5, "SIGNING", "FAILED"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), eq(false), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(failed));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var response = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));

        assertThat(response.getStatus().toString()).isEqualTo("FAILED");
        assertThat(response.getCurrentStep()).isEqualTo("SIGNING");
        assertThat(response.getErrors()).containsExactly("Artifact hash, signature and Agent Definition update are not implemented yet.");
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
                        AgentCompilationStep.StatusEnum.FAILED);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> resultCaptor = ArgumentCaptor.forClass(Map.class);
        verify(compilationRepository).markFailed(
                eq("AGCP1"),
                eq("FAILED"),
                eq("SIGNING"),
                eq("Artifact hash, signature and Agent Definition update are not implemented yet."),
                resultCaptor.capture(),
                any(OffsetDateTime.class));
        assertThat(resultCaptor.getValue())
                .containsEntry("preconditionsValid", true)
                .containsEntry("artifactGenerated", true)
                .containsEntry("runtimeCompatibilityValidated", true)
                .containsEntry("interpreterType", "SCHEDULED_INTERPRETER")
                .containsEntry("accessMode", "SERVICE_DATA_API_SNAPSHOT")
                .containsEntry("schemaVersion", "iia.agent.dsl/v1");
        @SuppressWarnings("unchecked")
        Map<String, Object> dslArtifact = (Map<String, Object>) resultCaptor.getValue().get("dslArtifact");
        @SuppressWarnings("unchecked")
        Map<String, Object> runtime = (Map<String, Object>) dslArtifact.get("runtime");
        assertThat(runtime).containsEntry("executionModel", "SCHEDULED_POLLING");
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("DRAFT");
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
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("DRAFT");
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
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("DRAFT");
    }

    @Test
    void getCompilationAfterCompileReadsSameLatestCompilation() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentDefinition definition = definition("DRAFT", "AUTO");
        AgentCompilation created = compilation("PENDING");
        AgentCompilation failed = compilation("FAILED");
        failed.setDscCurrentstep("SIGNING");
        failed.setDscErrormessage("Artifact hash, signature and Agent Definition update are not implemented yet.");
        var steps = List.of(
                step(1, "REQUEST_ACCEPTED", "READY"),
                step(2, "VALIDATING_BLUEPRINT", "READY"),
                step(3, "GENERATING_ARTIFACT", "READY"),
                step(4, "STATIC_ANALYSIS", "READY"),
                step(5, "SIGNING", "FAILED"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), anyBoolean(), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(failed), Optional.of(failed));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var postResponse = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));
        var getResponse = service.getAgentDefinitionCompilation("AGDF1");

        assertThat(getResponse).isEqualTo(postResponse);
        assertThat(getResponse.getStatus().toString()).isEqualTo("FAILED");
        assertThat(getResponse.getCurrentStep()).isEqualTo("SIGNING");
        assertThat(getResponse.getSteps()).hasSize(5);
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
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("DRAFT");
    }

    private AgentDefinitionService service(
            AgentDefinitionRepository definitionRepository,
            AgentCompilationRepository compilationRepository) {
        AgentDefinitionService service = new AgentDefinitionService();
        service.agentDefinitionRepository = definitionRepository;
        service.agentCompilationRepository = compilationRepository;
        service.agentCompilationMapper = new AgentCompilationMapper();
        service.agentCompilationMapper.stepStatusMapper = new AgentCompilationStepStatusMapper();
        service.agentCompilationPreconditionValidator = new AgentCompilationPreconditionValidator();
        service.agentDslArtifactBuilder = new AgentDslArtifactBuilder();
        service.agentDslRuntimeCompatibilityValidator = new AgentDslRuntimeCompatibilityValidator();
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
