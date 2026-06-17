package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ProvisionalAgentRuntimePackageVersionProvider implements AgentRuntimePackageVersionProvider {

    @ConfigProperty(name = "iia.agent-activation.provisional-package-version", defaultValue = "1")
    long provisionalPackageVersion;

    @Override
    public long resolvePackageVersion(AgentActivationSnapshot snapshot) {
        if (provisionalPackageVersion < 1) {
            throw new AgentActivationTechnicalException(
                    "Configured provisional runtime package version must be greater than or equal to 1.");
        }
        // TODO Replace with persistent monotonic runtime package version before enabling the real Orchestrator integration.
        return provisionalPackageVersion;
    }
}
