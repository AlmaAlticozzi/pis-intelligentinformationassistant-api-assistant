package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStep;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentCompilationRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStepId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
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
    void compileCreatesSkeletonCompilationAndReturnsFailedResponse() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentCompilationRepository compilationRepository = mock(AgentCompilationRepository.class);
        AgentDefinitionService service = service(definitionRepository, compilationRepository);
        AgentDefinition definition = definition("DRAFT", "AUTO");
        AgentCompilation created = compilation("PENDING");
        AgentCompilation failed = compilation("FAILED");
        failed.setDscCurrentstep("GENERATING_ARTIFACT");
        failed.setDscErrormessage("DSL artifact generation is not implemented yet.");
        failed.setJsnResult(Map.of(
                "skeleton", true,
                "artifactGenerated", false,
                "agentDefinitionStatusUpdated", false,
                "reason", "DSL artifact generation is not implemented yet."));
        var steps = List.of(
                step(1, "REQUEST_ACCEPTED", "READY"),
                step(2, "VALIDATING_BLUEPRINT", "READY"),
                step(3, "GENERATING_ARTIFACT", "FAILED"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), eq(false), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(failed));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var response = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));

        assertThat(response.getStatus().toString()).isEqualTo("FAILED");
        assertThat(response.getCurrentStep()).isEqualTo("GENERATING_ARTIFACT");
        assertThat(response.getErrors()).containsExactly("DSL artifact generation is not implemented yet.");
        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getName)
                .containsExactly("REQUEST_ACCEPTED", "VALIDATING_BLUEPRINT", "GENERATING_ARTIFACT");
        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getStatus)
                .containsExactly(
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
        verify(compilationRepository, org.mockito.Mockito.times(3)).addStep(
                eq("AGCP1"),
                stepOrderCaptor.capture(),
                stepNameCaptor.capture(),
                anyString(),
                anyString(),
                any(),
                any(),
                any());
        assertThat(stepOrderCaptor.getAllValues()).containsExactly(1, 2, 3);
        assertThat(stepNameCaptor.getAllValues()).containsExactly("REQUEST_ACCEPTED", "VALIDATING_BLUEPRINT", "GENERATING_ARTIFACT");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> resultCaptor = ArgumentCaptor.forClass(Map.class);
        verify(compilationRepository).markFailed(
                eq("AGCP1"),
                eq("FAILED"),
                eq("GENERATING_ARTIFACT"),
                eq("DSL artifact generation is not implemented yet."),
                resultCaptor.capture(),
                any(OffsetDateTime.class));
        assertThat(resultCaptor.getValue())
                .containsEntry("skeleton", true)
                .containsEntry("artifactGenerated", false)
                .containsEntry("agentDefinitionStatusUpdated", false);
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
        failed.setDscCurrentstep("GENERATING_ARTIFACT");
        failed.setDscErrormessage("DSL artifact generation is not implemented yet.");
        var steps = List.of(step(1, "REQUEST_ACCEPTED", "READY"), step(3, "GENERATING_ARTIFACT", "FAILED"));
        when(definitionRepository.findByDefinitionId("AGDF1")).thenReturn(Optional.of(definition));
        when(compilationRepository.existsRunningCompilation("AGDF1")).thenReturn(false);
        when(compilationRepository.createCompilation(eq("AGDF1"), eq("DSL"), anyBoolean(), any(), eq(null))).thenReturn(created);
        when(compilationRepository.findLatestByAgentDefinitionId("AGDF1")).thenReturn(Optional.of(failed), Optional.of(failed));
        when(compilationRepository.findStepsByCompilationId("AGCP1")).thenReturn(steps);

        var postResponse = service.compileAgentDefinition("AGDF1", compileRequest(AgentGenerationMode.DSL));
        var getResponse = service.getAgentDefinitionCompilation("AGDF1");

        assertThat(getResponse).isEqualTo(postResponse);
        assertThat(getResponse.getStatus().toString()).isEqualTo("FAILED");
        assertThat(getResponse.getSteps()).hasSize(2);
    }

    private AgentDefinitionService service(
            AgentDefinitionRepository definitionRepository,
            AgentCompilationRepository compilationRepository) {
        AgentDefinitionService service = new AgentDefinitionService();
        service.agentDefinitionRepository = definitionRepository;
        service.agentCompilationRepository = compilationRepository;
        service.agentCompilationMapper = new AgentCompilationMapper();
        service.agentCompilationMapper.stepStatusMapper = new AgentCompilationStepStatusMapper();
        return service;
    }

    private AgentDefinition definition() {
        return definition("DRAFT", "NONE");
    }

    private AgentDefinition definition(String statusValue, String generationModeValue) {
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition("AGDF1");
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
