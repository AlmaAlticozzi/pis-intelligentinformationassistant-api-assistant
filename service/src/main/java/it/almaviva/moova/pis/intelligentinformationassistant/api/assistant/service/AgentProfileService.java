package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfileListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentProfileRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class AgentProfileService {

    @Inject
    AgentProfileRepository agentProfileRepository;

    @Inject
    AgentProfileMapper agentProfileMapper;

    public AgentProfileListResponse listAgentProfiles() {
        var profiles = agentProfileRepository.findAllOrdered()
                .stream()
                .map(agentProfileMapper::toDto)
                .toList();
        System.out.println("[IIA][AGENT_PROFILE][LIST] found count=" + profiles.size());
        return new AgentProfileListResponse().items(profiles);
    }

    public Optional<AgentProfile> getAgentProfile(String agentProfileId) {
        return agentProfileRepository.findByProfileId(agentProfileId)
                .map(agentProfileMapper::toDto);
    }
}
