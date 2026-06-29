package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimePackageRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Map;

@ApplicationScoped
public class PersistedRuntimePackageReader {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    @Inject
    AgentRuntimePackageRepository repository;

    @Inject
    ObjectMapper objectMapper;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public PersistedRuntimePackageSnapshot read(String runtimePackageId, String expectedAgentDefinitionId) {
        AgentRuntimePackage entity = repository.findByIdOptional(runtimePackageId)
                .orElseThrow(() -> new AgentActivationTechnicalException("Persisted runtime package was not found."));
        String actualAgentDefinitionId = entity.getCodAgentdefinition().getCodAgentdefinition();
        if (!expectedAgentDefinitionId.equals(actualAgentDefinitionId)) {
            throw new AgentActivationTechnicalException("Persisted runtime package belongs to another Agent Definition.");
        }
        Map<String, Object> payload = entity.getJsnRuntimepackage() == null
                ? null
                : objectMapper.convertValue(entity.getJsnRuntimepackage(), MAP_TYPE);
        return new PersistedRuntimePackageSnapshot(
                entity.getCodRuntimepackage(), actualAgentDefinitionId, entity.getCodSubmissionid(),
                entity.getNumPackageversion(), entity.getDscPackagefingerprint(), payload);
    }
}
