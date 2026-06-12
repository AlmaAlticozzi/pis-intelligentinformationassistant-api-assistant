package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class ScheduledSnapshotEvaluationCanonicalizer {

    public CanonicalOutcome normalizeScheduledTechnicalSpecificationAndBlueprint(
            AlertVerificationOutcome outcome,
            ScheduledAlertJourneyCancellationHints hints) {
        if (outcome == null
                || outcome.decision() != AlertVerificationDecision.VERIFIED
                || outcome.technicalSpecification() == null) {
            return new CanonicalOutcome(outcome, List.of());
        }

        List<String> warnings = new ArrayList<>();
        Map<String, Object> technicalSpecification = copyMap(outcome.technicalSpecification());
        Map<String, Object> snapshotEvaluation = copyMap(technicalSpecification.get("snapshotEvaluation"));
        boolean normalizedCancellation = false;

        ScheduledAlertJourneyCancellationConstraint constraint = firstIncludedJourneyCancellationConstraint(hints);
        if (constraint != null
                && !snapshotEvaluation.isEmpty()
                && containsScheduledJourneyCancellationSignal(snapshotEvaluation.get("condition"))) {
            Object before = snapshotEvaluation.get("condition");
            Map<String, Object> canonicalCondition = journeyCancellationCondition(constraint);
            snapshotEvaluation.put("condition", canonicalCondition);
            technicalSpecification.put("snapshotEvaluation", snapshotEvaluation);
            normalizedCancellation = true;
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][SUPPRESSION_NORMALIZATION] intent="
                    + normalizationIntent(constraint)
                    + " action=canonicalized"
                    + " beforeHash=" + stableHash(before)
                    + " afterHash=" + stableHash(canonicalCondition));
        } else if (constraint != null && !snapshotEvaluation.isEmpty()) {
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][SUPPRESSION_NORMALIZATION] intent="
                    + normalizationIntent(constraint)
                    + " action=rejected reason=no_cancellation_signal");
        }

        Map<String, Object> agentBlueprintPreview = syncBlueprintWithTechnicalSpecification(
                outcome.agentBlueprintPreview(),
                technicalSpecification,
                warnings);
        Map<String, Object> requirementCoverage = normalizedCancellation
                ? canonicalizeJourneyCancellationCoverage(outcome.requirementCoverage(), constraint)
                : outcome.requirementCoverage();

        return new CanonicalOutcome(new AlertVerificationOutcome(
                outcome.decision(),
                outcome.summary(),
                outcome.rejectedReason(),
                outcome.confidence(),
                outcome.provider(),
                outcome.model(),
                outcome.promptVersion(),
                outcome.requiredSources(),
                outcome.interpreterType(),
                outcome.inputModel(),
                outcome.outputModel(),
                outcome.triggerType(),
                outcome.evaluationMode(),
                outcome.interpretedEventNames(),
                outcome.interpretedTargetTypes(),
                technicalSpecification,
                agentBlueprintPreview,
                requirementCoverage,
                mergeWarnings(outcome.warnings(), warnings),
                outcome.safetyChecks()), List.copyOf(warnings));
    }

    private ScheduledAlertJourneyCancellationConstraint firstIncludedJourneyCancellationConstraint(
            ScheduledAlertJourneyCancellationHints hints) {
        if (hints == null || !hints.hasJourneyCancellationConstraint() || hints.constraints() == null) {
            return null;
        }
        return hints.constraints().stream()
                .filter(constraint -> constraint.polarity() == ScheduledAlertJourneyCancellationConstraint.Polarity.INCLUDE)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> copyMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        return new LinkedHashMap<>();
    }

    private Map<String, Object> syncBlueprintWithTechnicalSpecification(
            Map<String, Object> blueprint,
            Map<String, Object> technicalSpecification,
            List<String> warnings) {
        if (blueprint == null) {
            return null;
        }
        Map<String, Object> synced = new LinkedHashMap<>(blueprint);
        Map<String, Object> parameters = copyMap(synced.get("parameters"));
        if (parameters.isEmpty()) {
            String warning = "agentBlueprintPreview.parameters is missing; cannot sync Scheduled snapshot parameters.";
            warnings.add(warning);
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][BLUEPRINT_SYNC][WARN] " + warning);
            return synced;
        }

        Map<String, Object> snapshotEvaluation = copyMap(technicalSpecification.get("snapshotEvaluation"));
        Map<String, Object> blueprintSnapshotEvaluation = copyMap(parameters.get("snapshotEvaluation"));
        if (blueprintSnapshotEvaluation.isEmpty()) {
            String warning = "agentBlueprintPreview.parameters.snapshotEvaluation is missing; creating it from technicalSpecification.";
            warnings.add(warning);
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][BLUEPRINT_SYNC][WARN] " + warning);
        }
        copyIfPresent(snapshotEvaluation, blueprintSnapshotEvaluation, "mode");
        copyIfPresent(snapshotEvaluation, blueprintSnapshotEvaluation, "journeyPath");
        copyIfPresent(snapshotEvaluation, blueprintSnapshotEvaluation, "condition");
        parameters.put("snapshotEvaluation", blueprintSnapshotEvaluation);
        copyIfPresent(technicalSpecification, parameters, "serviceDataQuery");
        copyIfPresent(technicalSpecification, parameters, "outputPolicy");
        synced.put("parameters", parameters);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][BLUEPRINT_SYNC] snapshotConditionSynced="
                + blueprintSnapshotEvaluation.containsKey("condition"));
        return synced;
    }

    private void copyIfPresent(Map<String, Object> source, Map<String, Object> target, String key) {
        if (source != null && source.containsKey(key)) {
            target.put(key, source.get(key));
        }
    }

    private boolean containsScheduledJourneyCancellationSignal(Object condition) {
        String text = String.valueOf(condition);
        return text.contains("field=changes")
                || text.contains("\"field\":\"changes\"")
                || text.contains("field=arrivalStatuses[].status")
                || text.contains("\"field\":\"arrivalStatuses[].status\"")
                || text.contains("field=departureStatuses[].status")
                || text.contains("\"field\":\"departureStatuses[].status\"")
                || text.contains("field=stopPointsJourneyDetails[].arrivalStatuses[].status")
                || text.contains("field=stopPointsJourneyDetails[].departureStatuses[].status")
                || containsPseudoCancelledCallJourneyIntent(condition);
    }

    private boolean containsPseudoCancelledCallJourneyIntent(Object condition) {
        String text = String.valueOf(condition);
        if (!text.contains("nextCancelledCalls")) {
            return false;
        }
        String normalized = normalize(text);
        boolean journeyWord = normalized.matches(".*\\b(corsa|corse|treno|treni|servizio|servizi|journey|journeys|train|trains|service|services)\\b.*");
        boolean cancellationWord = normalized.matches(".*\\b(soppressa|soppresse|soppresso|soppressi|cancellata|cancellate|cancellato|cancellati|cancellazione|cancellazioni|suppressed|cancelled|canceled|cancellation|cancellations)\\b.*");
        return journeyWord && cancellationWord;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private Map<String, Object> journeyCancellationCondition(ScheduledAlertJourneyCancellationConstraint constraint) {
        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH");
        Map<String, Object> anyElement = new LinkedHashMap<>();
        anyElement.put("path", "stopPointsJourneyDetails[]");
        anyElement.put("conditions", journeyCancellationConditions(constraint));
        condition.put("anyElement", anyElement);
        return condition;
    }

    private String normalizationIntent(ScheduledAlertJourneyCancellationConstraint constraint) {
        if (constraint == null || constraint.cancellationIntent() == null) {
            return "UNKNOWN";
        }
        return switch (constraint.cancellationIntent()) {
            case GENERIC_JOURNEY_CANCELLATION -> "GENERIC";
            case ARRIVAL_JOURNEY_CANCELLATION -> "ARRIVAL";
            case DEPARTURE_JOURNEY_CANCELLATION -> "DEPARTURE";
            case ARRIVAL_ONLY_JOURNEY_CANCELLATION -> "ONLY_ARRIVAL";
            case DEPARTURE_ONLY_JOURNEY_CANCELLATION -> "ONLY_DEPARTURE";
        };
    }

    private Map<String, Object> journeyCancellationConditions(ScheduledAlertJourneyCancellationConstraint constraint) {
        return switch (constraint.cancellationIntent()) {
            case ARRIVAL_JOURNEY_CANCELLATION -> Map.of("all", List.of(
                    leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION")));
            case ARRIVAL_ONLY_JOURNEY_CANCELLATION -> Map.of("all", List.of(
                    leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"),
                    leaf("departureStatuses[].status", "NOT_CONTAINS", "DEPARTURE_CANCELLATION")));
            case DEPARTURE_JOURNEY_CANCELLATION -> Map.of("all", List.of(
                    leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION")));
            case DEPARTURE_ONLY_JOURNEY_CANCELLATION -> Map.of("all", List.of(
                    leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"),
                    leaf("arrivalStatuses[].status", "NOT_CONTAINS", "ARRIVAL_CANCELLATION")));
            case GENERIC_JOURNEY_CANCELLATION -> Map.of("any", List.of(
                    Map.of("all", List.of(
                            leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"),
                            leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"))),
                    Map.of("all", List.of(
                            leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"),
                            leaf("passingType", "EQUALS", "DESTINATION"))),
                    Map.of("all", List.of(
                            leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"),
                            leaf("passingType", "EQUALS", "ORIGIN")))));
        };
    }

    private Map<String, Object> leaf(String field, String operator, Object value) {
        Map<String, Object> leaf = new LinkedHashMap<>();
        leaf.put("field", field);
        leaf.put("operator", operator);
        leaf.put("value", value);
        return leaf;
    }

    private Map<String, Object> canonicalizeJourneyCancellationCoverage(
            Map<String, Object> coverage,
            ScheduledAlertJourneyCancellationConstraint constraint) {
        if (coverage == null || coverage.isEmpty() || constraint == null) {
            return coverage;
        }
        Map<String, Object> updated = new LinkedHashMap<>(coverage);
        Object requirementsValue = updated.get("requirements");
        if (!(requirementsValue instanceof List<?> requirements)) {
            return updated;
        }
        List<Object> rewrittenRequirements = new ArrayList<>();
        for (Object item : requirements) {
            Map<String, Object> requirement = copyMap(item);
            if (requirement.isEmpty()) {
                rewrittenRequirements.add(item);
                continue;
            }
            requirement.put("mappedBy", journeyCancellationMappedBy(constraint));
            rewrittenRequirements.add(requirement);
        }
        updated.put("requirements", rewrittenRequirements);
        return updated;
    }

    private List<String> journeyCancellationMappedBy(ScheduledAlertJourneyCancellationConstraint constraint) {
        List<String> mappedBy = new ArrayList<>();
        mappedBy.add("serviceDataQuery.stopPoints");
        mappedBy.add("schedule.frequencySeconds");
        if (constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_JOURNEY_CANCELLATION
                || constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_ONLY_JOURNEY_CANCELLATION) {
            mappedBy.add("stopPointsJourneyDetails[].departureStatuses[].status");
        } else {
            mappedBy.add("stopPointsJourneyDetails[].arrivalStatuses[].status");
        }
        if (constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.ARRIVAL_ONLY_JOURNEY_CANCELLATION) {
            mappedBy.add("stopPointsJourneyDetails[].departureStatuses[].status");
        }
        if (constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_ONLY_JOURNEY_CANCELLATION) {
            mappedBy.add("stopPointsJourneyDetails[].arrivalStatuses[].status");
        }
        if (constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.GENERIC_JOURNEY_CANCELLATION) {
            mappedBy.add("stopPointsJourneyDetails[].departureStatuses[].status");
            mappedBy.add("stopPointsJourneyDetails[].passingType");
        }
        mappedBy.add("outputPolicy.emit");
        mappedBy.add("outputPolicy.includeCount");
        return List.copyOf(mappedBy);
    }

    private List<String> mergeWarnings(List<String> existing, List<String> added) {
        if (added == null || added.isEmpty()) {
            return existing;
        }
        List<String> merged = new ArrayList<>(existing == null ? List.of() : existing);
        merged.addAll(added);
        return List.copyOf(merged);
    }

    private int stableHash(Object value) {
        return String.valueOf(value).hashCode();
    }

    public record CanonicalOutcome(
            AlertVerificationOutcome outcome,
            List<String> warnings) {
    }
}
