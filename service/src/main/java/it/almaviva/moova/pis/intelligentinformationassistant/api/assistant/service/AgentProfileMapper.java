package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AgentProfileMapper {

    private static final List<AlertInterpreterType> DEFAULT_SUPPORTED_INTERPRETER_TYPES = List.of(
            AlertInterpreterType.EVENT_INTERPRETER,
            AlertInterpreterType.SCHEDULED_INTERPRETER);
    private static final List<String> DEFAULT_SUPPORTED_TRIGGER_TYPES = List.of("EVENT", "SCHEDULE");
    private static final String DEFAULT_RUNTIME_CLASS = "STANDARD_DSL_RUNTIME";

    public it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile toDto(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile profile) {
        if (profile == null) {
            return null;
        }

        it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile dto =
                new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile()
                        .id(profile.getCodAgentprofile())
                        .name(profile.getDscName())
                        .description(profile.getDscDescription())
                        .enabled(profile.getFlgEnabled())
                        .recommendedFor(toStringList(profile.getJsnRecommendedfor()))
                        .cpuRequestMillicores(profile.getNumCpurequestmillicores())
                        .cpuLimitMillicores(profile.getNumCpulimitmillicores())
                        .memoryRequestMiB(profile.getNumMemoryrequestmib())
                        .memoryLimitMiB(profile.getNumMemorylimitmib())
                        .networkPolicy(profile.getDscNetworkpolicy())
                        .maxRuntimeConcurrency(profile.getNumMaxruntimeconcurrency());

        applyDefaultRuntimeGovernance(dto);
        return dto;
    }

    // Temporary defaults until profile runtime constraints are persisted, e.g. in agent_profile.jsn_runtimeconstraints.
    private void applyDefaultRuntimeGovernance(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile dto) {
        dto.supportedInterpreterTypes(DEFAULT_SUPPORTED_INTERPRETER_TYPES)
                .supportedTriggerTypes(DEFAULT_SUPPORTED_TRIGGER_TYPES)
                .supportsStatefulExecution(false)
                .runtimeClass(DEFAULT_RUNTIME_CLASS);
        System.out.println("[IIA][AGENT_PROFILE][MAPPER] applied default runtime governance profileId=" + dto.getId());
    }

    private List<String> toStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of(String.valueOf(value));
    }
}
