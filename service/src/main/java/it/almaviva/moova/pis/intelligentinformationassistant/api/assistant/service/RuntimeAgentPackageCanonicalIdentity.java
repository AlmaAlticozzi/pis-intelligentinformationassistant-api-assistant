package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RuntimeAgentPackageCanonicalIdentity {
    public static final String CANONICALIZATION = "RFC8785_JSON";
    public static final String HASH_ALGORITHM = "SHA-256";

    private static final ObjectMapper MAPPER = JsonMapper.builder().findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
    private final AgentCanonicalJsonService canonicalJson = new AgentCanonicalJsonService();

    public Identity identify(AgentRuntimeSubmission submission) {
        AgentCanonicalJsonHashResult result = canonicalJson.hash(payload(submission));
        return new Identity(SemanticSha256Hash.digest(result.hash()), result.canonicalJson(), result.sizeBytes());
    }

    public Map<String, Object> payload(AgentRuntimeSubmission submission) {
        if (submission == null || submission.agentDefinition() == null) {
            throw new AgentRuntimePackageBuildException("Complete runtime package is required for identity.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> definition = MAPPER.convertValue(submission.agentDefinition(), LinkedHashMap.class);

        // Human-facing/audit-only fields are deliberately outside runtime identity.
        definition.remove("name");
        definition.remove("description");
        definition.remove("sourceUpdatedAt");
        remove(definition, "source", "alertName");
        remove(definition, "profile", "name");
        remove(definition, "artifact", "createdAt");
        remove(definition, "artifact", "signature", "signedAt");
        Object metadataValue = definition.get("metadata");
        if (metadataValue instanceof Map<?, ?> metadata) {
            Map<String, Object> governed = new LinkedHashMap<>();
            if (metadata.containsKey("requiredPermissions")) governed.put("requiredPermissions", metadata.get("requiredPermissions"));
            if (governed.isEmpty()) definition.remove("metadata"); else definition.put("metadata", governed);
        }

        sortStrings(definition, "activationPolicy", "daysOfWeek");
        sortStrings(definition, "runtimeContract", "requiredOperators");
        sortStrings(definition, "runtimeContract", "forbiddenCapabilities");
        sortStrings(definition, "runtimeContract", "requiredDataSourceAccessModes");
        sortStrings(definition, "runtimeContract", "requiredConnectorTypes");
        sortStrings(definition, "runtimeContract", "allowedConnectorRefs");
        sortObjects(definition, "runtimeContract", "allowedTools", "name");
        sortStrings(definition, "metadata", "requiredPermissions");
        sortObjects(definition, "dataSourceBindings", "bindingId");
        Object bindings = definition.get("dataSourceBindings");
        if (bindings instanceof List<?> list) {
            for (Object binding : list) if (binding instanceof Map<?, ?> map) sortStrings(cast(map), "metadata", "failoverConnectorRefs");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("desiredStatus", submission.desiredStatus());
        payload.put("startImmediatelyIfAllowed", submission.startImmediatelyIfAllowed());
        payload.put("agentDefinition", definition);
        return payload;
    }

    public String stableSubmissionId(String agentDefinitionId, String fingerprint) {
        String value = "ACTIVATE:" + agentDefinitionId + ":" + SemanticSha256Hash.digest(fingerprint).substring(0, 16);
        if (value.length() > 100) throw new AgentRuntimePackageBuildException("Deterministic submissionId exceeds 100 characters.");
        return value;
    }

    public void validate(AgentRuntimePackage row) {
        String id = row.getCodAgentdefinition().getCodAgentdefinition();
        String code = null;
        try {
            if (!CANONICALIZATION.equals(row.getDscCanonicalization())) code = "CANONICALIZATION_UNSUPPORTED";
            else if (!HASH_ALGORITHM.equals(row.getDscHashalgorithm())) code = "HASH_ALGORITHM_UNSUPPORTED";
            else {
                AgentRuntimeSubmission json = MAPPER.convertValue(row.getJsnRuntimepackage(), AgentRuntimeSubmission.class);
                if (!SemanticSha256Hash.digest(row.getDscPackagefingerprint()).equals(identify(json).fingerprint())) code = "PACKAGE_FINGERPRINT_MISMATCH";
                else if (json.packageVersion() != row.getNumPackageversion()) code = "PACKAGE_VERSION_MISMATCH";
                else if (!json.submissionId().equals(row.getCodSubmissionid())) code = "SUBMISSION_ID_MISMATCH";
                else if (!id.equals(json.agentDefinition().id())) code = "AGENT_DEFINITION_MISMATCH";
                else if (!SemanticSha256Hash.equal(row.getDscArtifacthash(), json.agentDefinition().artifact().hash())) code = "ARTIFACT_HASH_MISMATCH";
            }
        } catch (RuntimeException ex) {
            if (code == null) code = "PACKAGE_STRUCTURE_INVALID";
        }
        if (code != null) throw new AgentActivationTechnicalException(
                "Runtime package integrity failure: operation=PERSIST_OR_REUSE agentDefinitionId=" + id
                        + " packageVersion=" + row.getNumPackageversion() + " invariant=" + code + ".");
    }

    private void remove(Map<String, Object> root, String... path) {
        Map<String, Object> parent = root;
        for (int i = 0; i < path.length - 1; i++) {
            Object next = parent.get(path[i]);
            if (!(next instanceof Map<?, ?> map)) return;
            parent = cast(map);
        }
        parent.remove(path[path.length - 1]);
    }

    private void sortStrings(Map<String, Object> root, String... path) { sort(root, path, Comparator.comparing(String::valueOf)); }
    private void sortObjects(Map<String, Object> root, String first, String second, String key) {
        sort(root, new String[]{first, second}, Comparator.comparing(v -> String.valueOf(((Map<?, ?>) v).get(key))));
    }
    private void sortObjects(Map<String, Object> root, String field, String key) {
        sort(root, new String[]{field}, Comparator.comparing(v -> String.valueOf(((Map<?, ?>) v).get(key))));
    }
    private void sort(Map<String, Object> root, String[] path, Comparator<Object> comparator) {
        Map<String, Object> parent = root;
        for (int i = 0; i < path.length - 1; i++) {
            Object next = parent.get(path[i]); if (!(next instanceof Map<?, ?> map)) return; parent = cast(map);
        }
        Object value = parent.get(path[path.length - 1]);
        if (value instanceof List<?> list) { List<Object> copy = new ArrayList<>(list); copy.sort(comparator); parent.put(path[path.length - 1], copy); }
    }
    @SuppressWarnings("unchecked") private Map<String, Object> cast(Map<?, ?> value) { return (Map<String, Object>) value; }

    public record Identity(String fingerprint, String canonicalJson, int sizeBytes) { }
}
