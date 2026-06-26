package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import io.quarkus.runtime.StartupEvent;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class RuntimePackageBackfillService {

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Inject
    RuntimePackageBackfillDefinitionProcessor processor;

    void onStart(@Observes StartupEvent event) {
        backfill();
    }

    public void backfill() {
        int candidates = 0;
        int materialized = 0;
        int reused = 0;
        int failed = 0;
        List<String> ids;
        try {
            ids = agentDefinitionRepository.findActiveDefinitionIdsWithoutCurrentRuntimePackage();
        } catch (RuntimeException ex) {
            System.out.println("[IIA][RUNTIME_PACKAGE_BACKFILL] skipped reason=persistence_unavailable");
            return;
        }
        candidates = ids.size();
        for (String agentDefinitionId : ids) {
            try {
                RuntimePackageBackfillDefinitionProcessor.BackfillOutcome outcome = processor.process(agentDefinitionId);
                if (RuntimePackageBackfillDefinitionProcessor.BackfillOutcome.REUSED.equals(outcome)) {
                    reused++;
                } else {
                    materialized++;
                }
            } catch (AgentActivationPreconditionFailedException | AgentRuntimePackageBuildException ex) {
                failed++;
                System.out.println("[IIA][RUNTIME_PACKAGE_BACKFILL] failed agentDefinitionId=" + agentDefinitionId
                        + " reason=" + sanitize(ex.getMessage()));
            } catch (RuntimeException ex) {
                failed++;
                System.out.println("[IIA][RUNTIME_PACKAGE_BACKFILL] failed agentDefinitionId=" + agentDefinitionId
                        + " reason=runtime_package_materialization_failed");
            }
        }
        System.out.println("[IIA][RUNTIME_PACKAGE_BACKFILL] completed candidates=" + candidates
                + " materialized=" + materialized
                + " reused=" + reused
                + " failed=" + failed);
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return "runtime_package_incomplete";
        }
        String sanitized = message.replaceAll("[\\r\\n\\t]+", " ");
        return sanitized.length() <= 180 ? sanitized : sanitized.substring(0, 180);
    }
}
