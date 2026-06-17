package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentCompilationRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionAllowedTool;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionDayOfWeek;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionRequiredSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionSynchronizationRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AgentActivationSnapshotLoader {

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Inject
    AgentCompilationRepository agentCompilationRepository;

    @Inject
    TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Transactional
    public Optional<AgentActivationSnapshot> load(String agentDefinitionId) {
        System.out.println("[IIA][AGENT_ACTIVATION][SNAPSHOT] start agentDefinitionId=" + agentDefinitionId);
        Optional<AgentDefinition> definitionResult =
                agentDefinitionRepository.findActivationSnapshotDefinition(agentDefinitionId);
        if (definitionResult.isEmpty()) {
            System.out.println("[IIA][AGENT_ACTIVATION][SNAPSHOT] notFound agentDefinitionId=" + agentDefinitionId);
            return Optional.empty();
        }

        AgentDefinition definition = definitionResult.get();
        String latestCompilationReferenceId = agentDefinitionRepository
                .findLatestCompilationReferenceId(agentDefinitionId)
                .orElse(null);
        List<AgentDefinitionDayOfWeek> dayRows =
                agentDefinitionRepository.findActivationDaysOfWeek(agentDefinitionId);
        List<AgentDefinitionRequiredSource> sourceRows =
                agentDefinitionRepository.findActivationRequiredSources(agentDefinitionId);
        List<AgentDefinitionAllowedTool> toolRows =
                agentDefinitionRepository.findActivationAllowedTools(agentDefinitionId);

        AgentCompilation latestCompilation = null;
        if (latestCompilationReferenceId != null) {
            latestCompilation = agentCompilationRepository.findByCompilationId(latestCompilationReferenceId)
                    .orElse(null);
            if (latestCompilation == null) {
                System.out.println("[IIA][AGENT_ACTIVATION][SNAPSHOT][WARN] latestCompilationReferenceMissing agentDefinitionId="
                        + agentDefinitionId + " latestCompilationRef=" + latestCompilationReferenceId);
            }
        }

        AgentActivationSnapshot snapshot = toSnapshot(
                definition,
                latestCompilationReferenceId,
                latestCompilation,
                dayRows,
                sourceRows,
                toolRows);

        System.out.println("[IIA][AGENT_ACTIVATION][SNAPSHOT] loaded agentDefinitionId="
                + snapshot.agentDefinitionId()
                + " status=" + snapshot.status()
                + " profileId=" + (snapshot.profile() == null ? null : snapshot.profile().id())
                + " latestCompilationRef=" + latestCompilationReferenceId
                + " compilationLoaded=" + (snapshot.latestCompilation() != null)
                + " dslArtifactPresent=" + (snapshot.dslArtifact() != null)
                + " requiredSourcesCount=" + snapshot.requirements().requiredSources().size()
                + " allowedToolsCount=" + snapshot.requirements().allowedTools().size()
                + " transactionActive=" + isTransactionActive());
        System.out.println("[IIA][AGENT_ACTIVATION][SNAPSHOT] detached agentDefinitionId="
                + snapshot.agentDefinitionId() + " transactionBoundEntities=false");
        return Optional.of(snapshot);
    }

    private AgentActivationSnapshot toSnapshot(
            AgentDefinition definition,
            String latestCompilationReferenceId,
            AgentCompilation latestCompilation,
            List<AgentDefinitionDayOfWeek> dayRows,
            List<AgentDefinitionRequiredSource> sourceRows,
            List<AgentDefinitionAllowedTool> toolRows) {
        Map<String, Object> runtimeContract = copyMap(definition.getJsnRuntimecontract());
        Map<String, Object> blueprint = copyMap(definition.getJsnBlueprint());
        String interpreterType = firstString(
                runtimeContract == null ? null : runtimeContract.get("interpreterType"),
                definition.getCodAlert() == null || definition.getCodAlert().getSglInterpretertype() == null
                        ? null
                        : definition.getCodAlert().getSglInterpretertype().getSglInterpretertype());
        String triggerType = firstString(
                runtimeContract == null ? null : runtimeContract.get("triggerType"),
                blueprint == null ? null : blueprint.get("triggerType"));

        return new AgentActivationSnapshot(
                definition.getCodAgentdefinition(),
                definition.getDscName(),
                definition.getDscDescription(),
                definition.getSglStatus() == null ? null : definition.getSglStatus().getSglStatus(),
                definition.getSglGenerationmode() == null ? null : definition.getSglGenerationmode().getSglGenerationmode(),
                definition.getSglComplexity() == null ? null : definition.getSglComplexity().getSglComplexity(),
                interpreterType,
                triggerType,
                definition.getDscInputmodel(),
                definition.getDscOutputmodel(),
                definition.getCodCreatedby(),
                definition.getDtCreatedat(),
                definition.getDtUpdatedat(),
                toAlertSnapshot(definition),
                toProfileSnapshot(definition.getCodAgentprofile()),
                toPolicySnapshot(definition, dayRows),
                toRequirementsSnapshot(definition, sourceRows, toolRows, runtimeContract, blueprint),
                toArtifactSnapshot(definition),
                new AgentActivationSnapshot.AgentActivationCompilationSummarySnapshot(
                        latestCompilationReferenceId,
                        definition.getSglLatestcompilationstatus() == null
                                ? null
                                : definition.getSglLatestcompilationstatus().getSglStatus(),
                        definition.getDscLatestcompilationstep(),
                        definition.getDtLatestcompilationcompletedat()),
                latestCompilation == null ? null : toCompilationSnapshot(latestCompilation));
    }

    private AgentActivationSnapshot.AgentActivationAlertSnapshot toAlertSnapshot(AgentDefinition definition) {
        Alert alert = definition.getCodAlert();
        return new AgentActivationSnapshot.AgentActivationAlertSnapshot(
                alert == null ? null : alert.getCodAlert(),
                alert == null ? null : alert.getDscName(),
                definition.getNumAlertversion());
    }

    private AgentActivationSnapshot.AgentActivationProfileSnapshot toProfileSnapshot(AgentProfile profile) {
        if (profile == null) {
            return null;
        }
        return new AgentActivationSnapshot.AgentActivationProfileSnapshot(
                profile.getCodAgentprofile(),
                profile.getDscName(),
                profile.getDscDescription(),
                profile.getFlgEnabled(),
                copyList(profile.getJsnRecommendedfor()),
                profile.getNumCpurequestmillicores(),
                profile.getNumCpulimitmillicores(),
                profile.getNumMemoryrequestmib(),
                profile.getNumMemorylimitmib(),
                profile.getDscNetworkpolicy(),
                profile.getNumMaxruntimeconcurrency(),
                profile.getDtCreatedat(),
                profile.getDtUpdatedat());
    }

    private AgentActivationSnapshot.AgentActivationPolicySnapshot toPolicySnapshot(
            AgentDefinition definition,
            List<AgentDefinitionDayOfWeek> dayRows) {
        LinkedHashSet<String> days = new LinkedHashSet<>();
        for (AgentDefinitionDayOfWeek row : dayRows) {
            String value = row.getSglDayofweek() == null
                    ? row.getId().getSglDayofweek()
                    : row.getSglDayofweek().getSglDayofweek();
            if (value != null) {
                days.add(value);
            }
        }
        return new AgentActivationSnapshot.AgentActivationPolicySnapshot(
                definition.getSglActivationtype() == null ? null : definition.getSglActivationtype().getSglActivationtype(),
                definition.getDscTimezone(),
                definition.getDtValidfrom(),
                definition.getDtValidto(),
                definition.getDValidfromdate(),
                definition.getDValidtodate(),
                definition.getTDailystarttime(),
                definition.getTDailyendtime(),
                List.copyOf(days),
                copyMap(definition.getJsnActivationpolicy()));
    }

    private AgentActivationSnapshot.AgentActivationRequirementsSnapshot toRequirementsSnapshot(
            AgentDefinition definition,
            List<AgentDefinitionRequiredSource> sourceRows,
            List<AgentDefinitionAllowedTool> toolRows,
            Map<String, Object> runtimeContract,
            Map<String, Object> blueprint) {
        List<AgentActivationSnapshot.AgentActivationSourceSnapshot> sources = sourceRows.stream()
                .map(row -> new AgentActivationSnapshot.AgentActivationSourceSnapshot(
                        row.getSglCategory() == null ? row.getId().getSglCategory() : row.getSglCategory().getSglCategory(),
                        row.getFlgRequired(),
                        row.getDscDescription()))
                .toList();
        List<AgentActivationSnapshot.AgentActivationAllowedToolSnapshot> tools = toolRows.stream()
                .map(row -> new AgentActivationSnapshot.AgentActivationAllowedToolSnapshot(
                        row.getId().getDscToolname(),
                        copyMap(row.getJsnOperations())))
                .toList();
        return new AgentActivationSnapshot.AgentActivationRequirementsSnapshot(
                sources,
                copyList(definition.getJsnRequiredpermissions()),
                tools,
                copyList(definition.getJsnAllowedtools()),
                copyList(definition.getJsnGenerationwarnings()),
                runtimeContract,
                blueprint);
    }

    private AgentActivationSnapshot.AgentActivationArtifactSnapshot toArtifactSnapshot(AgentDefinition definition) {
        return new AgentActivationSnapshot.AgentActivationArtifactSnapshot(
                definition.getSglArtifacttype() == null ? null : definition.getSglArtifacttype().getSglArtifacttype(),
                definition.getDscArtifacturi(),
                definition.getDscArtifacthash(),
                definition.getSglSignaturestatus() == null ? null : definition.getSglSignaturestatus().getSglSignaturestatus(),
                definition.getDscRuntimeimage(),
                definition.getDscSdkversion(),
                definition.getDscImplementationsummary());
    }

    private AgentActivationSnapshot.AgentActivationCompilationSnapshot toCompilationSnapshot(AgentCompilation compilation) {
        Map<String, Object> resultJson = copyMap(compilation.getJsnResult());
        return new AgentActivationSnapshot.AgentActivationCompilationSnapshot(
                compilation.getCodAgentcompilation(),
                compilation.getCodAgentdefinition() == null ? null : compilation.getCodAgentdefinition().getCodAgentdefinition(),
                compilation.getSglStatus() == null ? null : compilation.getSglStatus().getSglStatus(),
                compilation.getDscCurrentstep(),
                compilation.getDscRequestedmode(),
                compilation.getFlgForce(),
                copyMap(compilation.getJsnRequest()),
                resultJson,
                compilation.getDscErrormessage(),
                compilation.getCodRequestedby(),
                compilation.getDtRequestedat(),
                compilation.getDtStartedat(),
                compilation.getDtCompletedat(),
                compilation.getDtUpdatedat(),
                copyMapFromValue(resultJson == null ? null : resultJson.get("dslArtifact")));
    }

    private String firstString(Object first, Object second) {
        String firstString = stringValue(first);
        return firstString == null ? stringValue(second) : firstString;
    }

    private String stringValue(Object value) {
        String text = value == null ? null : String.valueOf(value).trim();
        return text == null || text.isBlank() ? null : text;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> copyMapFromValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return copyMap((Map<String, Object>) map);
        }
        return null;
    }

    private Map<String, Object> copyMap(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        value.forEach((key, nestedValue) -> copy.put(key, copyJsonValue(nestedValue)));
        return Collections.unmodifiableMap(copy);
    }

    private List<Object> copyList(List<Object> value) {
        if (value == null) {
            return List.of();
        }
        List<Object> copy = new ArrayList<>();
        for (Object item : value) {
            copy.add(copyJsonValue(item));
        }
        return Collections.unmodifiableList(copy);
    }

    @SuppressWarnings("unchecked")
    private Object copyJsonValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((key, nestedValue) -> copy.put(String.valueOf(key), copyJsonValue(nestedValue)));
            return Collections.unmodifiableMap(copy);
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            for (Object item : list) {
                copy.add(copyJsonValue(item));
            }
            return Collections.unmodifiableList(copy);
        }
        if (value instanceof JsonNode jsonNode) {
            return jsonNode.deepCopy();
        }
        return value;
    }

    private boolean isTransactionActive() {
        return transactionSynchronizationRegistry != null
                && transactionSynchronizationRegistry.getTransactionKey() != null;
    }
}
