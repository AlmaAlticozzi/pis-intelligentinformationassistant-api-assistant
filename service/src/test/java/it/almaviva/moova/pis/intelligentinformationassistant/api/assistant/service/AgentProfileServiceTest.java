package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentProfileRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentProfileServiceTest {

    @Test
    void listAgentProfilesReturnsNonNullWrapperAndMappedItems() {
        AgentProfileRepository repository = mock(AgentProfileRepository.class);
        when(repository.findAllOrdered()).thenReturn(List.of(profile("MEDIUM")));

        AgentProfileService service = service(repository);

        var response = service.listAgentProfiles();

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getId()).isEqualTo("MEDIUM");
    }

    @Test
    void getAgentProfileReturnsMappedProfileWhenFound() {
        AgentProfileRepository repository = mock(AgentProfileRepository.class);
        when(repository.findByProfileId("MEDIUM")).thenReturn(Optional.of(profile("MEDIUM")));

        AgentProfileService service = service(repository);

        assertThat(service.getAgentProfile("MEDIUM"))
                .get()
                .extracting(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile::getId)
                .isEqualTo("MEDIUM");
    }

    @Test
    void getAgentProfileReturnsEmptyWhenMissing() {
        AgentProfileRepository repository = mock(AgentProfileRepository.class);
        when(repository.findByProfileId("UNKNOWN")).thenReturn(Optional.empty());

        AgentProfileService service = service(repository);

        assertThat(service.getAgentProfile("UNKNOWN")).isEmpty();
    }

    private AgentProfileService service(AgentProfileRepository repository) {
        AgentProfileService service = new AgentProfileService();
        service.agentProfileRepository = repository;
        service.agentProfileMapper = new AgentProfileMapper();
        return service;
    }

    private AgentProfile profile(String id) {
        AgentProfile profile = new AgentProfile();
        profile.setCodAgentprofile(id);
        profile.setDscName(id + " Agent");
        profile.setFlgEnabled(true);
        profile.setJsnRecommendedfor(List.of("Scheduled checks"));
        profile.setNumCpurequestmillicores(250);
        profile.setNumCpulimitmillicores(700);
        profile.setNumMemoryrequestmib(256);
        profile.setNumMemorylimitmib(768);
        profile.setDscNetworkpolicy("TOOL_GATEWAY_ONLY");
        profile.setNumMaxruntimeconcurrency(1);
        return profile;
    }
}
