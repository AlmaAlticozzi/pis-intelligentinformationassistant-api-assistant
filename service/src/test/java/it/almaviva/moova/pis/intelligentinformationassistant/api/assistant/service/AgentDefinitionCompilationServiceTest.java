package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentCompilationRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition("AGDF1");
        AgentArtifactType artifactType = new AgentArtifactType();
        artifactType.setSglArtifacttype("NONE");
        definition.setSglArtifacttype(artifactType);
        return definition;
    }

    private AgentCompilation compilation(String statusValue) {
        AgentCompilation compilation = new AgentCompilation();
        compilation.setCodAgentcompilation("AGCP1");
        AgentCompilationStatus status = new AgentCompilationStatus();
        status.setSglStatus(statusValue);
        compilation.setSglStatus(status);
        return compilation;
    }
}
