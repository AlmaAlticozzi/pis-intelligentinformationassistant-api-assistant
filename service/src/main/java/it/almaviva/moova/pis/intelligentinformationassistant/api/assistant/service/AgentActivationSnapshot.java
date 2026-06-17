package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record AgentActivationSnapshot(
        String agentDefinitionId,
        String name,
        String description,
        String status,
        String generationMode,
        String complexity,
        String interpreterType,
        String triggerType,
        String inputModel,
        String outputModel,
        String createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        AgentActivationAlertSnapshot alert,
        AgentActivationProfileSnapshot profile,
        AgentActivationPolicySnapshot activationPolicy,
        AgentActivationRequirementsSnapshot requirements,
        AgentActivationArtifactSnapshot artifact,
        AgentActivationCompilationSummarySnapshot compilationSummary,
        AgentActivationCompilationSnapshot latestCompilation) {

    public AgentActivationSnapshot {
        requirements = requirements == null ? AgentActivationRequirementsSnapshot.empty() : requirements;
    }

    public Map<String, Object> dslArtifact() {
        return latestCompilation == null ? null : latestCompilation.dslArtifact();
    }

    public record AgentActivationAlertSnapshot(
            String alertId,
            String alertName,
            Integer alertVersion) {
    }

    public record AgentActivationProfileSnapshot(
            String id,
            String name,
            String description,
            Boolean enabled,
            List<Object> recommendedFor,
            Integer cpuRequestMillicores,
            Integer cpuLimitMillicores,
            Integer memoryRequestMiB,
            Integer memoryLimitMiB,
            String networkPolicy,
            Integer maxRuntimeConcurrency,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

        public AgentActivationProfileSnapshot {
            recommendedFor = immutableList(recommendedFor);
        }
    }

    public record AgentActivationPolicySnapshot(
            String activationType,
            String timezone,
            OffsetDateTime validFrom,
            OffsetDateTime validTo,
            LocalDate validFromDate,
            LocalDate validToDate,
            LocalTime dailyStartTime,
            LocalTime dailyEndTime,
            List<String> daysOfWeek,
            Map<String, Object> activationPolicyJson) {

        public AgentActivationPolicySnapshot {
            daysOfWeek = immutableList(daysOfWeek);
            activationPolicyJson = immutableMap(activationPolicyJson);
        }
    }

    public record AgentActivationRequirementsSnapshot(
            List<AgentActivationSourceSnapshot> requiredSources,
            List<Object> requiredPermissions,
            List<AgentActivationAllowedToolSnapshot> allowedTools,
            List<Object> allowedToolsJson,
            List<Object> generationWarnings,
            Map<String, Object> runtimeContractJson,
            Map<String, Object> blueprintJson) {

        public AgentActivationRequirementsSnapshot {
            requiredSources = immutableList(requiredSources);
            requiredPermissions = immutableList(requiredPermissions);
            allowedTools = immutableList(allowedTools);
            allowedToolsJson = immutableList(allowedToolsJson);
            generationWarnings = immutableList(generationWarnings);
            runtimeContractJson = immutableMap(runtimeContractJson);
            blueprintJson = immutableMap(blueprintJson);
        }

        static AgentActivationRequirementsSnapshot empty() {
            return new AgentActivationRequirementsSnapshot(
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    Map.of(),
                    null);
        }
    }

    public record AgentActivationSourceSnapshot(
            String source,
            Boolean required,
            String description) {
    }

    public record AgentActivationAllowedToolSnapshot(
            String toolName,
            Map<String, Object> operationsJson) {

        public AgentActivationAllowedToolSnapshot {
            operationsJson = immutableMap(operationsJson);
        }
    }

    public record AgentActivationArtifactSnapshot(
            String artifactType,
            String artifactUri,
            String artifactHash,
            String signatureStatus,
            String runtimeImage,
            String sdkVersion,
            String implementationSummary) {
    }

    public record AgentActivationCompilationSummarySnapshot(
            String latestCompilationReferenceId,
            String latestCompilationStatus,
            String latestCompilationStep,
            OffsetDateTime latestCompilationCompletedAt) {
    }

    public record AgentActivationCompilationSnapshot(
            String compilationId,
            String agentDefinitionId,
            String status,
            String currentStep,
            String requestedMode,
            Boolean force,
            Map<String, Object> requestJson,
            Map<String, Object> resultJson,
            String errorMessage,
            String requestedBy,
            OffsetDateTime requestedAt,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt,
            OffsetDateTime updatedAt,
            Map<String, Object> dslArtifact) {

        public AgentActivationCompilationSnapshot {
            requestJson = immutableMap(requestJson);
            resultJson = immutableMap(resultJson);
            dslArtifact = immutableMap(dslArtifact);
        }
    }

    private static <T> List<T> immutableList(List<T> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    private static Map<String, Object> immutableMap(Map<String, Object> value) {
        return value == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }
}
