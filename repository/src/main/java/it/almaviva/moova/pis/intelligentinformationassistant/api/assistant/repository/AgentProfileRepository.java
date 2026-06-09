package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AgentProfileRepository implements PanacheRepositoryBase<AgentProfile, String> {

    public List<AgentProfile> findAllOrdered() {
        return list("order by codAgentprofile asc, dscName asc");
    }

    public Optional<AgentProfile> findByProfileId(String agentProfileId) {
        return find("codAgentprofile", agentProfileId).firstResultOptional();
    }
}
