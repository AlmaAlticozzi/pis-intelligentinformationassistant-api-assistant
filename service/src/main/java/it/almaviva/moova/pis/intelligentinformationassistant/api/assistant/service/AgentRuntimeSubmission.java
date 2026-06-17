package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentRuntimeSubmission(
        String submissionId,
        String desiredStatus,
        long packageVersion,
        Instant submittedAt,
        String submittedBy,
        boolean startImmediatelyIfAllowed,
        String note,
        AgentRuntimeDefinitionPackage agentDefinition) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AgentRuntimeDefinitionPackage(
            String id,
            String name,
            String description,
            AgentRuntimeSourceReference source,
            AgentRuntimeProfileSnapshot profile,
            AgentRuntimeActivationPolicy activationPolicy,
            String interpreterType,
            String triggerType,
            String inputModel,
            String outputModel,
            AgentRuntimeContractPackage runtimeContract,
            AgentRuntimeArtifact artifact,
            Map<String, Object> metadata,
            OffsetDateTime sourceUpdatedAt,
            String dataDomain,
            List<AgentRuntimeDataSourceBinding> dataSourceBindings) {

        public AgentRuntimeDefinitionPackage {
            metadata = immutableMap(metadata);
            dataSourceBindings = immutableList(dataSourceBindings);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AgentRuntimeSourceReference(
            String controlPlaneComponent,
            String alertId,
            String alertName,
            Integer alertVersion,
            String agentCompilationId,
            Long agentCompilationVersion) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AgentRuntimeProfileSnapshot(
            String id,
            String name,
            Boolean enabled,
            Integer cpuRequestMillicores,
            Integer cpuLimitMillicores,
            Integer memoryRequestMiB,
            Integer memoryLimitMiB,
            String networkPolicy,
            String runtimeClass,
            Integer maxRuntimeConcurrency) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AgentRuntimeActivationPolicy(
            String type,
            String timezone,
            OffsetDateTime validFrom,
            OffsetDateTime validTo,
            LocalDate validFromDate,
            LocalDate validToDate,
            LocalTime dailyStartTime,
            LocalTime dailyEndTime,
            List<String> daysOfWeek) {

        public AgentRuntimeActivationPolicy {
            daysOfWeek = immutableList(daysOfWeek);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AgentRuntimeContractPackage(
            String runtimeImage,
            String sdkVersion,
            String minimumRuntimeVersion,
            String runtimeExecutionModel,
            String interpreterType,
            String triggerType,
            String inputModel,
            String outputModel,
            String evaluationMode,
            List<String> requiredOperators,
            List<RuntimeToolReference> allowedTools,
            String networkPolicy,
            List<String> forbiddenCapabilities,
            Map<String, Object> compatibility,
            List<String> requiredDataSourceAccessModes,
            List<String> requiredConnectorTypes,
            List<String> allowedConnectorRefs) {

        public AgentRuntimeContractPackage {
            requiredOperators = immutableList(requiredOperators);
            allowedTools = immutableList(allowedTools);
            forbiddenCapabilities = immutableList(forbiddenCapabilities);
            compatibility = immutableMap(compatibility);
            requiredDataSourceAccessModes = immutableList(requiredDataSourceAccessModes);
            requiredConnectorTypes = immutableList(requiredConnectorTypes);
            allowedConnectorRefs = immutableList(allowedConnectorRefs);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RuntimeToolReference(
            String name,
            Map<String, Object> operations) {

        public RuntimeToolReference {
            operations = immutableMap(operations);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AgentRuntimeArtifact(
            String artifactType,
            String deliveryMode,
            String mediaType,
            String schemaVersion,
            Map<String, Object> content,
            String hashAlgorithm,
            String hash,
            String canonicalization,
            AgentRuntimeArtifactSignature signature,
            OffsetDateTime createdAt,
            Integer sizeBytes) {

        public AgentRuntimeArtifact {
            content = immutableMap(content);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AgentRuntimeArtifactSignature(
            String type,
            String algorithm,
            OffsetDateTime signedAt,
            String signedBy,
            String keyId,
            String value) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AgentRuntimeDataSourceBinding(
            String bindingId,
            String dataDomain,
            String accessMode,
            String connectorType,
            String connectorRef,
            String inputModel,
            String inputSchemaVersion,
            String bindingSchemaVersion,
            String operationRef,
            Boolean required,
            Map<String, Object> metadata) {

        public AgentRuntimeDataSourceBinding {
            metadata = immutableMap(metadata);
        }
    }

    private static <T> List<T> immutableList(List<T> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    private static Map<String, Object> immutableMap(Map<String, Object> value) {
        return value == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }
}
