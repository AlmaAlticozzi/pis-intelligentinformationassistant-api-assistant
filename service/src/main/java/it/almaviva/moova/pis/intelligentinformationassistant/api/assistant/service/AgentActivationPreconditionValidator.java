package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@ApplicationScoped
public class AgentActivationPreconditionValidator {

    private static final Pattern HASH_PATTERN = Pattern.compile("sha256:[0-9a-fA-F]{64}");
    private static final Pattern ARTIFACT_URI_PATTERN = Pattern.compile(
            "iia-agent-artifact://agent-definitions/[^/]+/compilations/[^/]+/dsl");
    private static final Set<String> DAYS_OF_WEEK = Set.of(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
    private static final String SERVICE_DATA = "SERVICE_DATA";
    private static final String SCHEDULED_TOOL = "SERVICE_DATA_API.POST_/v2/stoppointjourneys";

    @Inject
    AgentArtifactHashService agentArtifactHashService;

    @Inject
    AgentDslRuntimeCompatibilityValidator agentDslRuntimeCompatibilityValidator;

    private Clock clock = Clock.systemUTC();

    public AgentActivationPreconditionValidator() {
    }

    AgentActivationPreconditionValidator(
            AgentArtifactHashService agentArtifactHashService,
            AgentDslRuntimeCompatibilityValidator agentDslRuntimeCompatibilityValidator,
            Clock clock) {
        this.agentArtifactHashService = agentArtifactHashService;
        this.agentDslRuntimeCompatibilityValidator = agentDslRuntimeCompatibilityValidator;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public AgentActivationPreconditionValidationResult validate(AgentActivationSnapshot snapshot) {
        String agentDefinitionId = snapshot == null ? null : snapshot.agentDefinitionId();
        System.out.println("[IIA][AGENT_ACTIVATION][PRECONDITION] start agentDefinitionId=" + agentDefinitionId);

        List<AgentActivationPreconditionViolation> errors = new ArrayList<>();
        List<AgentActivationPreconditionViolation> warnings = new ArrayList<>();
        ValidationFlags flags = new ValidationFlags();

        if (snapshot == null) {
            error(errors, AgentActivationPreconditionCode.DSL_ARTIFACT_INVALID, "snapshot", "Activation snapshot is missing.");
            return completed(agentDefinitionId, errors, warnings, flags);
        }

        validateCompilation(snapshot, errors, flags);
        validateArtifactMetadata(snapshot, errors);
        validateHashAndSignature(snapshot, errors, flags);
        validateProfile(snapshot, errors, flags);
        validateActivationPolicy(snapshot, errors, warnings);
        validateRuntimeContract(snapshot, errors);
        validateDslRuntimeCompatibility(snapshot, errors, warnings, flags);
        validateSourcesAndTools(snapshot, errors);

        return completed(agentDefinitionId, errors, warnings, flags);
    }

    private void validateCompilation(
            AgentActivationSnapshot snapshot,
            List<AgentActivationPreconditionViolation> errors,
            ValidationFlags flags) {
        String referenceId = snapshot.compilationSummary() == null
                ? null
                : text(snapshot.compilationSummary().latestCompilationReferenceId());
        if (referenceId == null) {
            error(errors, AgentActivationPreconditionCode.LATEST_COMPILATION_REFERENCE_MISSING,
                    "compilationSummary.latestCompilationReferenceId",
                    "Latest compilation reference is missing.");
            return;
        }

        AgentActivationSnapshot.AgentActivationCompilationSnapshot compilation = snapshot.latestCompilation();
        if (compilation == null) {
            error(errors, AgentActivationPreconditionCode.LATEST_COMPILATION_NOT_FOUND,
                    "latestCompilation",
                    "Latest compilation referenced by Agent Definition was not found.");
            return;
        }

        if (!referenceId.equals(text(compilation.compilationId()))) {
            error(errors, AgentActivationPreconditionCode.LATEST_COMPILATION_REFERENCE_MISMATCH,
                    "latestCompilation.compilationId",
                    "Loaded compilation does not match the Agent Definition latest compilation reference.");
        }
        if (!Objects.equals(text(snapshot.agentDefinitionId()), text(compilation.agentDefinitionId()))) {
            error(errors, AgentActivationPreconditionCode.COMPILATION_AGENT_DEFINITION_MISMATCH,
                    "latestCompilation.agentDefinitionId",
                    "Loaded compilation belongs to a different Agent Definition.");
        }
        if (!"READY".equals(text(compilation.status())) || !"READY".equals(text(compilation.currentStep()))) {
            error(errors, AgentActivationPreconditionCode.COMPILATION_NOT_READY,
                    "latestCompilation.status",
                    "Latest compilation must be READY and completed at READY step.");
        } else {
            flags.compilationReady = true;
        }
        if (compilation.resultJson() == null || compilation.resultJson().isEmpty()) {
            error(errors, AgentActivationPreconditionCode.COMPILATION_RESULT_MISSING,
                    "latestCompilation.resultJson",
                    "Latest compilation result JSON is missing.");
        }
        Map<String, Object> dslArtifact = snapshot.dslArtifact();
        if (dslArtifact == null) {
            error(errors, AgentActivationPreconditionCode.DSL_ARTIFACT_MISSING,
                    "latestCompilation.resultJson.dslArtifact",
                    "Latest compilation result does not contain dslArtifact.");
        } else if (dslArtifact.isEmpty()) {
            error(errors, AgentActivationPreconditionCode.DSL_ARTIFACT_INVALID,
                    "latestCompilation.resultJson.dslArtifact",
                    "DSL artifact is empty.");
        } else {
            flags.dslPresent = true;
        }
    }

    private void validateArtifactMetadata(
            AgentActivationSnapshot snapshot,
            List<AgentActivationPreconditionViolation> errors) {
        AgentActivationSnapshot.AgentActivationArtifactSnapshot artifact = snapshot.artifact();
        if (artifact == null) {
            error(errors, AgentActivationPreconditionCode.ARTIFACT_TYPE_MISSING, "artifact", "Artifact metadata is missing.");
            return;
        }

        String artifactType = text(artifact.artifactType());
        if (artifactType == null) {
            error(errors, AgentActivationPreconditionCode.ARTIFACT_TYPE_MISSING,
                    "artifact.artifactType",
                    "Artifact type is missing.");
        } else if (!"DSL".equals(artifactType)) {
            error(errors, AgentActivationPreconditionCode.ARTIFACT_TYPE_UNSUPPORTED,
                    "artifact.artifactType",
                    "Only DSL artifact type is supported for activation.");
        }

        String artifactUri = text(artifact.artifactUri());
        if (artifactUri == null) {
            error(errors, AgentActivationPreconditionCode.ARTIFACT_URI_MISSING,
                    "artifact.artifactUri",
                    "Artifact URI is missing.");
        } else if (!ARTIFACT_URI_PATTERN.matcher(artifactUri).matches()) {
            error(errors, AgentActivationPreconditionCode.ARTIFACT_URI_INVALID,
                    "artifact.artifactUri",
                    "Artifact URI does not match the generated DSL artifact URI format.");
        }

        String artifactHash = text(artifact.artifactHash());
        if (artifactHash == null) {
            error(errors, AgentActivationPreconditionCode.ARTIFACT_HASH_MISSING,
                    "artifact.artifactHash",
                    "Artifact hash is missing.");
        } else if (!HASH_PATTERN.matcher(artifactHash).matches()) {
            error(errors, AgentActivationPreconditionCode.ARTIFACT_HASH_FORMAT_INVALID,
                    "artifact.artifactHash",
                    "Artifact hash must use sha256:<64 hex> format.");
        }

        if (!"SIGNED".equals(text(artifact.signatureStatus()))) {
            error(errors, AgentActivationPreconditionCode.ARTIFACT_NOT_SIGNED,
                    "artifact.signatureStatus",
                    "Artifact logical signature status must be SIGNED.");
        }
        if (text(artifact.runtimeImage()) == null) {
            error(errors, AgentActivationPreconditionCode.RUNTIME_IMAGE_MISSING,
                    "artifact.runtimeImage",
                    "Runtime image is missing.");
        }
        if (text(artifact.sdkVersion()) == null) {
            error(errors, AgentActivationPreconditionCode.SDK_VERSION_MISSING,
                    "artifact.sdkVersion",
                    "SDK version is missing.");
        }
        if (text(artifact.implementationSummary()) == null) {
            error(errors, AgentActivationPreconditionCode.IMPLEMENTATION_SUMMARY_MISSING,
                    "artifact.implementationSummary",
                    "Implementation summary is missing.");
        }
    }

    private void validateHashAndSignature(
            AgentActivationSnapshot snapshot,
            List<AgentActivationPreconditionViolation> errors,
            ValidationFlags flags) {
        Map<String, Object> dslArtifact = snapshot.dslArtifact();
        String persistedHash = snapshot.artifact() == null ? null : text(snapshot.artifact().artifactHash());
        if (dslArtifact == null || persistedHash == null || !HASH_PATTERN.matcher(persistedHash).matches()) {
            return;
        }

        try {
            AgentArtifactHashResult hashResult = hashService().hashDslArtifact(
                    snapshot.agentDefinitionId(),
                    snapshot.latestCompilation() == null ? null : snapshot.latestCompilation().compilationId(),
                    dslArtifact);
            if (!persistedHash.equalsIgnoreCase(hashResult.artifactHash())) {
                error(errors, AgentActivationPreconditionCode.ARTIFACT_HASH_MISMATCH,
                        "artifact.artifactHash",
                        "Persisted artifact hash does not match the canonical DSL artifact hash.");
            } else if ("SIGNED".equals(text(snapshot.artifact().signatureStatus()))) {
                flags.hashVerified = true;
                flags.signatureValid = true;
            }

            String compilationHash = snapshot.latestCompilation() == null || snapshot.latestCompilation().resultJson() == null
                    ? null
                    : text(snapshot.latestCompilation().resultJson().get("artifactHash"));
            if (compilationHash != null && !persistedHash.equalsIgnoreCase(compilationHash)) {
                error(errors, AgentActivationPreconditionCode.COMPILATION_ARTIFACT_HASH_MISMATCH,
                        "latestCompilation.resultJson.artifactHash",
                        "Compilation result artifact hash does not match Agent Definition metadata.");
            }
        } catch (RuntimeException e) {
            flags.canonicalizationFailed = true;
            error(errors, AgentActivationPreconditionCode.ARTIFACT_CANONICALIZATION_FAILED,
                    "latestCompilation.resultJson.dslArtifact",
                    "Unable to canonicalize DSL artifact for hash verification.");
        }
    }

    private void validateDslRuntimeCompatibility(
            AgentActivationSnapshot snapshot,
            List<AgentActivationPreconditionViolation> errors,
            List<AgentActivationPreconditionViolation> warnings,
            ValidationFlags flags) {
        Map<String, Object> dslArtifact = snapshot.dslArtifact();
        if (dslArtifact == null || dslArtifact.isEmpty() || flags.canonicalizationFailed) {
            return;
        }
        try {
            AgentDslRuntimeCompatibilityValidationResult result = compatibilityValidator().validate(dslArtifact);
            if (result.compatible()) {
                flags.runtimeCompatible = true;
            } else {
                for (String message : safeList(result.errors())) {
                    error(errors, AgentActivationPreconditionCode.DSL_RUNTIME_COMPATIBILITY_FAILED,
                            "latestCompilation.resultJson.dslArtifact",
                            message);
                }
            }
            for (String message : safeList(result.warnings())) {
                warning(warnings, AgentActivationPreconditionCode.DSL_RUNTIME_COMPATIBILITY_FAILED,
                        "latestCompilation.resultJson.dslArtifact",
                        message);
            }
        } catch (RuntimeException e) {
            error(errors, AgentActivationPreconditionCode.DSL_RUNTIME_COMPATIBILITY_FAILED,
                    "latestCompilation.resultJson.dslArtifact",
                    "DSL runtime compatibility validation failed unexpectedly.");
        }
    }

    private void validateProfile(
            AgentActivationSnapshot snapshot,
            List<AgentActivationPreconditionViolation> errors,
            ValidationFlags flags) {
        AgentActivationSnapshot.AgentActivationProfileSnapshot profile = snapshot.profile();
        if (profile == null) {
            error(errors, AgentActivationPreconditionCode.AGENT_PROFILE_MISSING,
                    "profile",
                    "Agent Profile is missing.");
            return;
        }
        if (text(profile.id()) == null) {
            error(errors, AgentActivationPreconditionCode.AGENT_PROFILE_MISSING,
                    "profile.id",
                    "Agent Profile id is missing.");
        }
        if (!Boolean.TRUE.equals(profile.enabled())) {
            error(errors, AgentActivationPreconditionCode.AGENT_PROFILE_DISABLED,
                    "profile.enabled",
                    "Agent Profile must be enabled.");
        } else {
            flags.profileEnabled = true;
        }
        validateResourceRange(profile.cpuRequestMillicores(), profile.cpuLimitMillicores(), "CPU", errors);
        validateResourceRange(profile.memoryRequestMiB(), profile.memoryLimitMiB(), "memory", errors);
        if (profile.maxRuntimeConcurrency() != null && profile.maxRuntimeConcurrency() <= 0) {
            error(errors, AgentActivationPreconditionCode.AGENT_PROFILE_RESOURCES_INVALID,
                    "profile.maxRuntimeConcurrency",
                    "Agent Profile maxRuntimeConcurrency must be positive when present.");
        }
        if (text(profile.networkPolicy()) == null) {
            error(errors, AgentActivationPreconditionCode.AGENT_PROFILE_NETWORK_POLICY_MISSING,
                    "profile.networkPolicy",
                    "Agent Profile network policy is missing.");
        }
    }

    private void validateResourceRange(
            Integer request,
            Integer limit,
            String label,
            List<AgentActivationPreconditionViolation> errors) {
        if (request != null && request < 0) {
            error(errors, AgentActivationPreconditionCode.AGENT_PROFILE_RESOURCES_INVALID,
                    "profile." + label.toLowerCase(Locale.ROOT) + "Request",
                    label + " request must be non-negative.");
        }
        if (limit == null || limit <= 0) {
            error(errors, AgentActivationPreconditionCode.AGENT_PROFILE_RESOURCES_INVALID,
                    "profile." + label.toLowerCase(Locale.ROOT) + "Limit",
                    label + " limit must be positive.");
        }
        if (request != null && limit != null && request > limit) {
            error(errors, AgentActivationPreconditionCode.AGENT_PROFILE_RESOURCES_INVALID,
                    "profile." + label.toLowerCase(Locale.ROOT),
                    label + " request cannot be greater than limit.");
        }
    }

    private void validateActivationPolicy(
            AgentActivationSnapshot snapshot,
            List<AgentActivationPreconditionViolation> errors,
            List<AgentActivationPreconditionViolation> warnings) {
        AgentActivationSnapshot.AgentActivationPolicySnapshot policy = snapshot.activationPolicy();
        if (policy == null) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_MISSING,
                    "activationPolicy",
                    "Activation policy is missing.");
            return;
        }
        String type = text(policy.activationType());
        if (type == null) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_MISSING,
                    "activationPolicy.activationType",
                    "Activation policy type is missing.");
            return;
        }
        ZoneId zoneId = validateTimezone(policy.timezone(), errors);
        if ("CONTINUOUS".equals(type)) {
            validateContinuousPolicy(policy, zoneId, errors, warnings);
        } else if ("DAILY_WINDOW".equals(type)) {
            validateDailyWindowPolicy(policy, zoneId, errors);
        } else {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_TYPE_UNSUPPORTED,
                    "activationPolicy.activationType",
                    "Unsupported activation policy type " + type + ".");
        }
        validatePolicyJson(policy, type, errors);
    }

    private ZoneId validateTimezone(String timezone, List<AgentActivationPreconditionViolation> errors) {
        String value = text(timezone);
        if (value == null) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_TIMEZONE_INVALID,
                    "activationPolicy.timezone",
                    "Activation policy timezone is missing.");
            return null;
        }
        try {
            return ZoneId.of(value);
        } catch (ZoneRulesException e) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_TIMEZONE_INVALID,
                    "activationPolicy.timezone",
                    "Activation policy timezone must be a valid IANA timezone.");
            return null;
        }
    }

    private void validateContinuousPolicy(
            AgentActivationSnapshot.AgentActivationPolicySnapshot policy,
            ZoneId zoneId,
            List<AgentActivationPreconditionViolation> errors,
            List<AgentActivationPreconditionViolation> warnings) {
        if (policy.validFrom() == null || policy.validTo() == null) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_RANGE_INVALID,
                    "activationPolicy.validFrom",
                    "CONTINUOUS activation policy requires validFrom and validTo.");
            return;
        }
        if (!policy.validTo().isAfter(policy.validFrom())) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_RANGE_INVALID,
                    "activationPolicy.validTo",
                    "Activation policy validTo must be after validFrom.");
        }
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (zoneId != null) {
            now = now.atZoneSameInstant(zoneId).toOffsetDateTime();
        }
        if (!policy.validTo().isAfter(now)) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_EXPIRED,
                    "activationPolicy.validTo",
                    "Activation policy is expired.");
        } else if (policy.validFrom().isAfter(now)) {
            warning(warnings, AgentActivationPreconditionCode.ACTIVATION_POLICY_RANGE_INVALID,
                    "activationPolicy.validFrom",
                    "Activation policy starts in the future.");
        }
    }

    private void validateDailyWindowPolicy(
            AgentActivationSnapshot.AgentActivationPolicySnapshot policy,
            ZoneId zoneId,
            List<AgentActivationPreconditionViolation> errors) {
        if (policy.validFromDate() == null || policy.validToDate() == null) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_RANGE_INVALID,
                    "activationPolicy.validFromDate",
                    "DAILY_WINDOW activation policy requires validFromDate and validToDate.");
        } else {
            if (policy.validToDate().isBefore(policy.validFromDate())) {
                error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_RANGE_INVALID,
                        "activationPolicy.validToDate",
                        "Activation policy validToDate cannot be before validFromDate.");
            }
            LocalDate today = LocalDate.now(zoneId == null ? clock : clock.withZone(zoneId));
            if (policy.validToDate().isBefore(today)) {
                error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_EXPIRED,
                        "activationPolicy.validToDate",
                        "DAILY_WINDOW activation policy is expired.");
            }
        }
        if (policy.dailyStartTime() == null || policy.dailyEndTime() == null
                || !policy.dailyEndTime().isAfter(policy.dailyStartTime())) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_DAILY_WINDOW_INVALID,
                    "activationPolicy.dailyEndTime",
                    "DAILY_WINDOW dailyEndTime must be after dailyStartTime.");
        }
        Set<String> seen = new HashSet<>();
        for (String day : safeList(policy.daysOfWeek())) {
            String normalized = text(day);
            if (normalized == null || !DAYS_OF_WEEK.contains(normalized) || !seen.add(normalized)) {
                error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_DAILY_WINDOW_INVALID,
                        "activationPolicy.daysOfWeek",
                        "DAILY_WINDOW daysOfWeek contains invalid or duplicate values.");
                break;
            }
        }
    }

    private void validatePolicyJson(
            AgentActivationSnapshot.AgentActivationPolicySnapshot policy,
            String type,
            List<AgentActivationPreconditionViolation> errors) {
        Map<String, Object> json = policy.activationPolicyJson();
        if (json == null) {
            return;
        }
        if (json.isEmpty()) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_JSON_INVALID,
                    "activationPolicy.activationPolicyJson",
                    "Activation policy JSON is empty.");
            return;
        }
        String jsonType = text(json.get("type"));
        if (jsonType != null && !Objects.equals(type, jsonType)) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_INCONSISTENT,
                    "activationPolicy.activationPolicyJson.type",
                    "Activation policy JSON type contradicts canonical activation type.");
        }
        String jsonTimezone = text(json.get("timezone"));
        if (jsonTimezone != null && !Objects.equals(text(policy.timezone()), jsonTimezone)) {
            error(errors, AgentActivationPreconditionCode.ACTIVATION_POLICY_INCONSISTENT,
                    "activationPolicy.activationPolicyJson.timezone",
                    "Activation policy JSON timezone contradicts canonical timezone.");
        }
    }

    private void validateRuntimeContract(
            AgentActivationSnapshot snapshot,
            List<AgentActivationPreconditionViolation> errors) {
        Map<String, Object> contract = snapshot.requirements() == null ? null : snapshot.requirements().runtimeContractJson();
        if (contract == null || contract.isEmpty()) {
            error(errors, AgentActivationPreconditionCode.RUNTIME_CONTRACT_MISSING,
                    "requirements.runtimeContractJson",
                    "Runtime contract is missing.");
            return;
        }
        compare(contract, "interpreterType", snapshot.interpreterType(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_INTERPRETER_MISMATCH, errors);
        compare(contract, "triggerType", snapshot.triggerType(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_TRIGGER_MISMATCH, errors);
        compare(contract, "inputModel", snapshot.inputModel(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_INPUT_MODEL_MISMATCH, errors);
        compare(contract, "outputModel", snapshot.outputModel(),
                AgentActivationPreconditionCode.RUNTIME_CONTRACT_OUTPUT_MODEL_MISMATCH, errors);

        String evaluationMode = text(contract.get("evaluationMode"));
        String dslEvaluationMode = nestedText(snapshot.dslArtifact(), "runtime", "evaluationMode");
        if (evaluationMode != null && dslEvaluationMode != null && !Objects.equals(evaluationMode, dslEvaluationMode)) {
            error(errors, AgentActivationPreconditionCode.RUNTIME_CONTRACT_EVALUATION_MODE_MISMATCH,
                    "requirements.runtimeContractJson.evaluationMode",
                    "Runtime contract evaluationMode does not match DSL artifact.");
        }
        String executionModel = text(contract.get("executionModel"));
        if (executionModel != null
                && !"KAFKA_EVENT".equals(executionModel)
                && !"SCHEDULED_POLLING".equals(executionModel)) {
            error(errors, AgentActivationPreconditionCode.RUNTIME_CONTRACT_EXECUTION_MODEL_UNSUPPORTED,
                    "requirements.runtimeContractJson.executionModel",
                    "Runtime contract executionModel is unsupported.");
        }
        if (stringList(contract.get("forbiddenCapabilities")).stream().anyMatch("LLM_RUNTIME_EXECUTION"::equals)
                && Boolean.TRUE.equals(booleanValue(nestedValue(snapshot.dslArtifact(), "governance", "llmRuntimeExecutionAllowed")))) {
            error(errors, AgentActivationPreconditionCode.RUNTIME_CONTRACT_FORBIDDEN_CAPABILITY,
                    "requirements.runtimeContractJson.forbiddenCapabilities",
                    "Runtime contract forbids a capability enabled by the DSL artifact.");
        }
    }

    private void compare(
            Map<String, Object> contract,
            String key,
            String snapshotValue,
            AgentActivationPreconditionCode code,
            List<AgentActivationPreconditionViolation> errors) {
        String contractValue = text(contract.get(key));
        if (contractValue != null && text(snapshotValue) != null && !Objects.equals(contractValue, text(snapshotValue))) {
            error(errors, code, "requirements.runtimeContractJson." + key,
                    "Runtime contract " + key + " does not match Agent Definition metadata.");
        }
    }

    private void validateSourcesAndTools(
            AgentActivationSnapshot snapshot,
            List<AgentActivationPreconditionViolation> errors) {
        List<AgentActivationSnapshot.AgentActivationSourceSnapshot> requiredSources = snapshot.requirements() == null
                ? List.of()
                : safeList(snapshot.requirements().requiredSources());
        if (requiredSources.isEmpty()) {
            error(errors, AgentActivationPreconditionCode.REQUIRED_SOURCE_MISSING,
                    "requirements.requiredSources",
                    "At least one required source is expected.");
        }
        Set<String> sources = new HashSet<>();
        for (AgentActivationSnapshot.AgentActivationSourceSnapshot source : requiredSources) {
            String value = source == null ? null : text(source.source());
            if (value == null || !sources.add(value)) {
                error(errors, AgentActivationPreconditionCode.REQUIRED_SOURCE_INVALID,
                        "requirements.requiredSources",
                        "Required sources contain null, blank or duplicate values.");
                break;
            }
        }
        Map<String, Object> runtimeContract = snapshot.requirements() == null ? null : snapshot.requirements().runtimeContractJson();
        String contractSource = runtimeContract == null ? null : text(runtimeContract.get("source"));
        String dslSource = nestedText(snapshot.dslArtifact(), "runtime", "source");
        if (!sources.isEmpty()
                && ((contractSource != null && !sources.contains(contractSource))
                || (dslSource != null && !sources.contains(dslSource))
                || !sources.contains(SERVICE_DATA))) {
            error(errors, AgentActivationPreconditionCode.REQUIRED_SOURCE_MISMATCH,
                    "requirements.requiredSources",
                    "Required sources are not coherent with runtime contract and DSL artifact.");
        }

        List<AgentActivationSnapshot.AgentActivationAllowedToolSnapshot> allowedTools = snapshot.requirements() == null
                ? List.of()
                : safeList(snapshot.requirements().allowedTools());
        Set<String> tools = new HashSet<>();
        for (AgentActivationSnapshot.AgentActivationAllowedToolSnapshot tool : allowedTools) {
            String toolName = tool == null ? null : text(tool.toolName());
            if (toolName == null || !tools.add(toolName)) {
                error(errors, AgentActivationPreconditionCode.ALLOWED_TOOL_INVALID,
                        "requirements.allowedTools",
                        "Allowed tools contain null, blank or duplicate values.");
                break;
            }
        }
        String interpreterType = text(snapshot.interpreterType());
        if ("EVENT_INTERPRETER".equals(interpreterType) && !tools.isEmpty()) {
            error(errors, AgentActivationPreconditionCode.ALLOWED_TOOL_MISMATCH,
                    "requirements.allowedTools",
                    "EVENT_INTERPRETER cannot require external allowed tools.");
        }
        if ("SCHEDULED_INTERPRETER".equals(interpreterType) && !tools.contains(SCHEDULED_TOOL)) {
            error(errors, AgentActivationPreconditionCode.ALLOWED_TOOL_MISMATCH,
                    "requirements.allowedTools",
                    "SCHEDULED_INTERPRETER requires the ServiceData stoppoint journeys tool.");
        }
        List<String> contractTools = runtimeContract == null
                ? List.of()
                : stringList(runtimeContract.get("allowedTools"));
        if (!contractTools.isEmpty() && !tools.containsAll(contractTools)) {
            error(errors, AgentActivationPreconditionCode.ALLOWED_TOOL_MISMATCH,
                    "requirements.allowedTools",
                    "Allowed tools do not include all tools required by runtime contract.");
        }
    }

    private AgentActivationPreconditionValidationResult completed(
            String agentDefinitionId,
            List<AgentActivationPreconditionViolation> errors,
            List<AgentActivationPreconditionViolation> warnings,
            ValidationFlags flags) {
        AgentActivationPreconditionValidationResult result = new AgentActivationPreconditionValidationResult(
                errors.isEmpty(),
                errors,
                warnings);
        if (result.valid()) {
            System.out.println("[IIA][AGENT_ACTIVATION][PRECONDITION] completed agentDefinitionId=" + agentDefinitionId
                    + " valid=true errors=0 warnings=" + result.warnings().size()
                    + " compilationReady=" + flags.compilationReady
                    + " dslPresent=" + flags.dslPresent
                    + " hashVerified=" + flags.hashVerified
                    + " signatureValid=" + flags.signatureValid
                    + " profileEnabled=" + flags.profileEnabled
                    + " runtimeCompatible=" + flags.runtimeCompatible);
        } else {
            String codes = result.errors().stream()
                    .map(error -> error.code().name())
                    .distinct()
                    .toList()
                    .toString()
                    .replace(" ", "");
            System.out.println("[IIA][AGENT_ACTIVATION][PRECONDITION] completed agentDefinitionId=" + agentDefinitionId
                    + " valid=false errors=" + result.errors().size()
                    + " warnings=" + result.warnings().size()
                    + " codes=" + codes);
        }
        return result;
    }

    private void error(
            List<AgentActivationPreconditionViolation> errors,
            AgentActivationPreconditionCode code,
            String field,
            String message) {
        errors.add(new AgentActivationPreconditionViolation(code, field, message));
    }

    private void warning(
            List<AgentActivationPreconditionViolation> warnings,
            AgentActivationPreconditionCode code,
            String field,
            String message) {
        warnings.add(new AgentActivationPreconditionViolation(code, field, message));
    }

    private AgentArtifactHashService hashService() {
        return agentArtifactHashService == null ? new AgentArtifactHashService() : agentArtifactHashService;
    }

    private AgentDslRuntimeCompatibilityValidator compatibilityValidator() {
        return agentDslRuntimeCompatibilityValidator == null
                ? new AgentDslRuntimeCompatibilityValidator()
                : agentDslRuntimeCompatibilityValidator;
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Boolean.valueOf(text);
        }
        return null;
    }

    private List<String> stringList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::text)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

    private Object nestedValue(Map<String, Object> source, String... path) {
        Object value = source;
        for (String segment : path) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = map.get(segment);
        }
        return value;
    }

    private String nestedText(Map<String, Object> source, String... path) {
        return text(nestedValue(source, path));
    }

    private <T> List<T> safeList(List<T> value) {
        return value == null ? List.of() : value;
    }

    private static class ValidationFlags {
        private boolean compilationReady;
        private boolean dslPresent;
        private boolean hashVerified;
        private boolean signatureValid;
        private boolean profileEnabled;
        private boolean runtimeCompatible;
        private boolean canonicalizationFailed;
    }
}
