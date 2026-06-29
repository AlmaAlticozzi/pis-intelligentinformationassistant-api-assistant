package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class AgentRuntimePackageBuilder {

    private static final String DESIRED_STATUS_ACTIVE = "ACTIVE";
    private static final String ARTIFACT_TYPE_DSL = "DSL";
    private static final String DELIVERY_MODE_INLINE = "INLINE";
    private static final String SIGNATURE_TYPE = "LOGICAL_MVP";
    private static final String SIGNATURE_ALGORITHM = "SHA256_WITH_CONTROL_PLANE_ATTESTATION";

    private final AgentRuntimePackageConfiguration configuration;
    private final RuntimeDataSourceBindingResolverRegistry bindingResolverRegistry;
    private final AgentCanonicalJsonService canonicalJsonService;
    private final AgentRuntimeOperatorExtractor operatorExtractor;

    public AgentRuntimePackageBuilder() {
        this(AgentRuntimePackageConfiguration.defaults(), null, new AgentCanonicalJsonService());
    }

    @Inject
    public AgentRuntimePackageBuilder(AgentRuntimePackageConfigurationProvider configurationProvider) {
        this(configurationProvider == null ? AgentRuntimePackageConfiguration.defaults() : configurationProvider.configuration(),
                null,
                new AgentCanonicalJsonService());
    }

    AgentRuntimePackageBuilder(
            AgentRuntimePackageConfiguration configuration,
            RuntimeDataSourceBindingResolverRegistry bindingResolverRegistry,
            AgentCanonicalJsonService canonicalJsonService) {
        this.configuration = configuration == null ? AgentRuntimePackageConfiguration.defaults() : configuration;
        this.bindingResolverRegistry = bindingResolverRegistry == null
                ? defaultRegistry(this.configuration)
                : bindingResolverRegistry;
        this.canonicalJsonService = canonicalJsonService == null ? new AgentCanonicalJsonService() : canonicalJsonService;
        this.operatorExtractor = new AgentRuntimeOperatorExtractor();
    }

    public AgentRuntimePackageBuildResult build(
            AgentActivationSnapshot snapshot,
            AgentActivationCommand command,
            AgentRuntimePackageBuildContext context) {
        require(snapshot != null, "Activation snapshot is required.");
        require(command != null, "AgentActivationCommand is required.");
        require(context != null, "AgentRuntimePackageBuildContext is required.");
        require(Objects.equals(snapshot.agentDefinitionId(), command.agentDefinitionId()),
                "Activation command agentDefinitionId does not match snapshot.");
        System.out.println("[IIA][AGENT_ACTIVATION][PACKAGE] start agentDefinitionId="
                + snapshot.agentDefinitionId() + " packageVersion=" + context.packageVersion());

        List<AgentRuntimeSubmission.AgentRuntimeDataSourceBinding> bindings = bindingResolverRegistry.resolve(snapshot);
        AgentRuntimeSubmission.AgentRuntimeDefinitionPackage agentDefinition = toAgentDefinition(snapshot, bindings);
        AgentCanonicalJsonHashResult packageHash = canonicalJsonService.hash(runtimeSignificantPayload(
                command.startImmediatelyIfAllowed(),
                agentDefinition));
        String hashHex = stripSha256Prefix(packageHash.hash());
        String submissionId = submissionId(snapshot.agentDefinitionId(), context.packageVersion(), hashHex);
        AgentRuntimeSubmission submission = new AgentRuntimeSubmission(
                submissionId,
                DESIRED_STATUS_ACTIVE,
                context.packageVersion(),
                context.submittedAt(),
                context.submittedBy(),
                command.startImmediatelyIfAllowed(),
                command.note(),
                agentDefinition);

        System.out.println("[IIA][AGENT_ACTIVATION][PACKAGE] completed agentDefinitionId="
                + snapshot.agentDefinitionId()
                + " packageVersion=" + context.packageVersion()
                + " submissionId=" + submissionId
                + " packageHashPrefix=" + hashHex.substring(0, 16)
                + " bindingCount=" + bindings.size()
                + " artifactDeliveryMode=INLINE");
        return new AgentRuntimePackageBuildResult(
                submission,
                packageHash.canonicalJson(),
                packageHash.hash(),
                packageHash.sizeBytes());
    }

    private AgentRuntimeSubmission.AgentRuntimeDefinitionPackage toAgentDefinition(
            AgentActivationSnapshot snapshot,
            List<AgentRuntimeSubmission.AgentRuntimeDataSourceBinding> bindings) {
        Map<String, Object> runtimeContract = requireMap(snapshot.requirements().runtimeContractJson(), "Runtime contract is required.");
        AgentRuntimeSubmission.AgentRuntimeProfileSnapshot profile = toProfile(snapshot.profile(), runtimeContract);
        AgentRuntimeSubmission.AgentRuntimeArtifact artifact = toArtifact(snapshot, runtimeContract);
        AgentRuntimeSubmission.AgentRuntimeContractPackage contract = toRuntimeContract(snapshot, runtimeContract, profile, bindings);
        return new AgentRuntimeSubmission.AgentRuntimeDefinitionPackage(
                requireText(snapshot.agentDefinitionId(), "Agent Definition id is required."),
                requireText(snapshot.name(), "Agent Definition name is required."),
                snapshot.description(),
                toSource(snapshot),
                profile,
                toActivationPolicy(snapshot.activationPolicy()),
                requireText(snapshot.interpreterType(), "interpreterType is required."),
                requireText(snapshot.triggerType(), "triggerType is required."),
                requireText(snapshot.inputModel(), "inputModel is required."),
                requireText(snapshot.outputModel(), "outputModel is required."),
                contract,
                artifact,
                metadata(snapshot),
                snapshot.updatedAt(),
                dataDomain(snapshot, runtimeContract),
                bindings);
    }

    private Map<String, Object> runtimeSignificantPayload(
            boolean startImmediatelyIfAllowed,
            AgentRuntimeSubmission.AgentRuntimeDefinitionPackage agentDefinition) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("desiredStatus", DESIRED_STATUS_ACTIVE);
        payload.put("startImmediatelyIfAllowed", startImmediatelyIfAllowed);
        payload.put("agentDefinition", new AgentRuntimeSubmission.AgentRuntimeDefinitionPackage(
                agentDefinition.id(),
                agentDefinition.name(),
                agentDefinition.description(),
                agentDefinition.source(),
                agentDefinition.profile(),
                agentDefinition.activationPolicy(),
                agentDefinition.interpreterType(),
                agentDefinition.triggerType(),
                agentDefinition.inputModel(),
                agentDefinition.outputModel(),
                agentDefinition.runtimeContract(),
                agentDefinition.artifact(),
                agentDefinition.metadata(),
                null,
                agentDefinition.dataDomain(),
                agentDefinition.dataSourceBindings()));
        return payload;
    }

    private AgentRuntimeSubmission.AgentRuntimeSourceReference toSource(AgentActivationSnapshot snapshot) {
        AgentActivationSnapshot.AgentActivationCompilationSnapshot compilation = snapshot.latestCompilation();
        AgentActivationSnapshot.AgentActivationAlertSnapshot alert = snapshot.alert();
        return new AgentRuntimeSubmission.AgentRuntimeSourceReference(
                configuration.controlPlaneComponent(),
                alert == null ? null : alert.alertId(),
                alert == null ? null : alert.alertName(),
                alert == null ? null : alert.alertVersion(),
                compilation == null ? null : compilation.compilationId(),
                null);
    }

    private AgentRuntimeSubmission.AgentRuntimeProfileSnapshot toProfile(
            AgentActivationSnapshot.AgentActivationProfileSnapshot profile,
            Map<String, Object> runtimeContract) {
        require(profile != null, "Agent Profile snapshot is required.");
        String runtimeClass = firstText(
                nestedText(runtimeContract, "orchestratorCompatibility", "runtimeClass"),
                nestedText(runtimeContract, "compatibility", "runtimeClass"),
                configuration.defaultRuntimeClass());
        requireText(runtimeClass, "runtimeClass is required.");
        return new AgentRuntimeSubmission.AgentRuntimeProfileSnapshot(
                requireText(profile.id(), "Agent Profile id is required."),
                profile.name(),
                profile.enabled(),
                profile.cpuRequestMillicores(),
                profile.cpuLimitMillicores(),
                profile.memoryRequestMiB(),
                profile.memoryLimitMiB(),
                configuration.networkPolicy(),
                runtimeClass,
                profile.maxRuntimeConcurrency());
    }

    private AgentRuntimeSubmission.AgentRuntimeActivationPolicy toActivationPolicy(
            AgentActivationSnapshot.AgentActivationPolicySnapshot policy) {
        require(policy != null, "Activation policy is required.");
        String type = requireText(policy.activationType(), "Activation policy type is required.");
        if ("CONTINUOUS".equals(type)) {
            return new AgentRuntimeSubmission.AgentRuntimeActivationPolicy(
                    type,
                    requireText(policy.timezone(), "Activation policy timezone is required."),
                    policy.validFrom(),
                    policy.validTo(),
                    null,
                    null,
                    null,
                    null,
                    null);
        }
        if ("DAILY_WINDOW".equals(type)) {
            return new AgentRuntimeSubmission.AgentRuntimeActivationPolicy(
                    type,
                    requireText(policy.timezone(), "Activation policy timezone is required."),
                    null,
                    null,
                    policy.validFromDate(),
                    policy.validToDate(),
                    policy.dailyStartTime(),
                    policy.dailyEndTime(),
                    policy.daysOfWeek());
        }
        throw new AgentRuntimePackageBuildException("Unsupported activation policy type " + type + ".");
    }

    private AgentRuntimeSubmission.AgentRuntimeArtifact toArtifact(
            AgentActivationSnapshot snapshot,
            Map<String, Object> runtimeContract) {
        AgentActivationSnapshot.AgentActivationArtifactSnapshot artifact = snapshot.artifact();
        require(artifact != null, "Artifact metadata is required.");
        require("RFC8785_JSON".equals(configuration.artifactCanonicalization()),
                "Artifact canonicalization must be RFC8785_JSON.");
        String runtimeExecutionModel = governedRuntimeExecutionModel(runtimeContract);
        Map<String, Object> dslArtifact = governedArtifactContent(
                requireMap(snapshot.dslArtifact(), "DSL artifact is required."), runtimeExecutionModel);
        AgentCanonicalJsonHashResult dslHash = canonicalJsonService.hash(dslArtifact);
        requireText(artifact.artifactHash(), "Artifact hash is required.");
        String signatureStatus = requireText(artifact.signatureStatus(), "Artifact signatureStatus is required.");
        require("SIGNED".equals(signatureStatus), "Artifact signatureStatus must be SIGNED.");
        String hashHex = stripSha256Prefix(dslHash.hash());
        String schemaVersion = firstText(text(dslArtifact.get("schemaVersion")), artifact.sdkVersion());
        return new AgentRuntimeSubmission.AgentRuntimeArtifact(
                ARTIFACT_TYPE_DSL,
                DELIVERY_MODE_INLINE,
                configuration.artifactMediaType(),
                requireText(schemaVersion, "DSL schemaVersion is required."),
                dslArtifact,
                "SHA-256",
                hashHex,
                "RFC8785_JSON",
                signatureStatus,
                new AgentRuntimeSubmission.AgentRuntimeArtifactSignature(
                        SIGNATURE_TYPE,
                        SIGNATURE_ALGORITHM,
                        snapshot.latestCompilation() == null ? null : snapshot.latestCompilation().completedAt(),
                        configuration.controlPlaneComponent(),
                        null,
                        null),
                snapshot.latestCompilation() == null ? null : snapshot.latestCompilation().completedAt(),
                dslHash.sizeBytes());
    }

    private AgentRuntimeSubmission.AgentRuntimeContractPackage toRuntimeContract(
            AgentActivationSnapshot snapshot,
            Map<String, Object> runtimeContract,
            AgentRuntimeSubmission.AgentRuntimeProfileSnapshot profile,
            List<AgentRuntimeSubmission.AgentRuntimeDataSourceBinding> bindings) {
        Map<String, Object> compatibility = compatibility(runtimeContract, profile.runtimeClass());
        List<AgentRuntimeSubmission.RuntimeToolReference> tools = allowedTools(snapshot, runtimeContract);
        return new AgentRuntimeSubmission.AgentRuntimeContractPackage(
                snapshot.artifact() == null ? null : snapshot.artifact().runtimeImage(),
                configuration.sdkVersion(),
                configuration.minimumRuntimeVersion(),
                governedRuntimeExecutionModel(runtimeContract),
                snapshot.interpreterType(),
                snapshot.triggerType(),
                snapshot.inputModel(),
                snapshot.outputModel(),
                firstText(text(runtimeContract.get("evaluationMode")), nestedText(snapshot.dslArtifact(), "runtime", "evaluationMode")),
                operatorExtractor.extract(snapshot.dslArtifact()),
                tools,
                configuration.networkPolicy(),
                distinctStrings(runtimeContract.get("forbiddenCapabilities")),
                compatibility,
                bindings.stream().map(AgentRuntimeSubmission.AgentRuntimeDataSourceBinding::accessMode).distinct().toList(),
                bindings.stream().map(AgentRuntimeSubmission.AgentRuntimeDataSourceBinding::connectorType).distinct().toList(),
                bindings.stream().map(AgentRuntimeSubmission.AgentRuntimeDataSourceBinding::connectorRef).distinct().toList());
    }

    private String governedRuntimeExecutionModel(Map<String, Object> runtimeContract) {
        String value = requireText(
                text(runtimeContract.get("runtimeExecutionModel")),
                "Runtime contract runtimeExecutionModel is required.");
        require("STANDARD_DSL_EVALUATOR".equals(value) || "APPROVED_TEMPLATE_RUNTIME".equals(value),
                "Runtime contract runtimeExecutionModel is unsupported.");
        return value;
    }

    private Map<String, Object> compatibility(Map<String, Object> runtimeContract, String runtimeClass) {
        Map<String, Object> compatibility = new LinkedHashMap<>();
        Object contractCompatibility = runtimeContract.get("orchestratorCompatibility");
        if (!(contractCompatibility instanceof Map<?, ?>)) {
            contractCompatibility = runtimeContract.get("compatibility");
        }
        if (contractCompatibility instanceof Map<?, ?> map) {
            map.forEach((key, value) -> compatibility.put(String.valueOf(key), value));
        }
        compatibility.putIfAbsent("runtimeClass", runtimeClass);
        compatibility.put("canonicalization", "RFC8785_JSON");
        compatibility.putIfAbsent("dataSourceBindingSchema", configuration.bindingSchemaVersion());
        return compatibility;
    }

    private Map<String, Object> governedArtifactContent(
            Map<String, Object> source,
            String runtimeExecutionModel) {
        Map<String, Object> copy = deepCopyMap(source);
        Object runtimeValue = copy.get("runtime");
        require(runtimeValue instanceof Map<?, ?>, "DSL artifact runtime object is required.");
        @SuppressWarnings("unchecked")
        Map<String, Object> runtime = (Map<String, Object>) runtimeValue;
        runtime.put("executionModel", runtimeExecutionModel);
        require(runtimeExecutionModel.equals(text(runtime.get("executionModel"))),
                "Artifact and runtime contract execution models are inconsistent.");
        return copy;
    }

    private Map<String, Object> deepCopyMap(Map<String, Object> source) {
        Map<String, Object> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, deepCopyValue(value)));
        return copy;
    }

    private Object deepCopyValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((key, nested) -> copy.put(String.valueOf(key), deepCopyValue(nested)));
            return copy;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::deepCopyValue).toList();
        }
        return value;
    }

    private List<AgentRuntimeSubmission.RuntimeToolReference> allowedTools(
            AgentActivationSnapshot snapshot,
            Map<String, Object> runtimeContract) {
        Map<String, AgentRuntimeSubmission.RuntimeToolReference> result = new LinkedHashMap<>();
        for (AgentActivationSnapshot.AgentActivationAllowedToolSnapshot tool : snapshot.requirements().allowedTools()) {
            if (tool.toolName() != null && !tool.toolName().isBlank()) {
                result.put(tool.toolName(), new AgentRuntimeSubmission.RuntimeToolReference(tool.toolName(), tool.operationsJson()));
            }
        }
        for (String toolName : distinctStrings(runtimeContract.get("allowedTools"))) {
            result.putIfAbsent(toolName, new AgentRuntimeSubmission.RuntimeToolReference(toolName, null));
        }
        return new ArrayList<>(result.values());
    }

    private Map<String, Object> metadata(AgentActivationSnapshot snapshot) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        put(metadata, "generationMode", snapshot.generationMode());
        put(metadata, "complexity", snapshot.complexity());
        if (snapshot.requirements() != null && !snapshot.requirements().requiredPermissions().isEmpty()) {
            metadata.put("requiredPermissions", snapshot.requirements().requiredPermissions());
        }
        if (snapshot.artifact() != null) {
            put(metadata, "implementationSummary", snapshot.artifact().implementationSummary());
        }
        return metadata;
    }

    private String dataDomain(AgentActivationSnapshot snapshot, Map<String, Object> runtimeContract) {
        return firstText(
                text(runtimeContract.get("source")),
                snapshot.requirements().requiredSources().stream()
                        .map(AgentActivationSnapshot.AgentActivationSourceSnapshot::source)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null));
    }

    private RuntimeDataSourceBindingResolverRegistry defaultRegistry(AgentRuntimePackageConfiguration configuration) {
        return new RuntimeDataSourceBindingResolverRegistry(List.of(
                new EventServiceDataRuntimeDataSourceBindingResolver(configuration),
                new ScheduledServiceDataRuntimeDataSourceBindingResolver(configuration)));
    }

    private String submissionId(String agentDefinitionId, long packageVersion, String hashHex) {
        String value = "ACTIVATE:" + agentDefinitionId + ":" + packageVersion + ":" + hashHex.substring(0, 16);
        if (value.length() > 100) {
            throw new AgentRuntimePackageBuildException("Deterministic submissionId exceeds 100 characters.");
        }
        return value;
    }

    private String stripSha256Prefix(String hash) {
        String value = requireText(hash, "SHA-256 hash is required.");
        if (!value.startsWith("sha256:") || value.length() != 71) {
            throw new AgentRuntimePackageBuildException("SHA-256 hash must use sha256:<64 hex> format.");
        }
        return value.substring("sha256:".length());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> requireMap(Map<String, Object> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new AgentRuntimePackageBuildException(message);
        }
        return map;
    }

    private String nestedText(Map<String, Object> source, String... path) {
        Object value = source;
        for (String segment : path) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = map.get(segment);
        }
        return text(value);
    }

    private List<String> distinctStrings(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        return collection.stream()
                .map(this::text)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String firstText(String... values) {
        for (String value : values) {
            String text = text(value);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private String requireText(String value, String message) {
        String text = text(value);
        if (text == null) {
            throw new AgentRuntimePackageBuildException(message);
        }
        return text;
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private void put(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new AgentRuntimePackageBuildException(message);
        }
    }
}
