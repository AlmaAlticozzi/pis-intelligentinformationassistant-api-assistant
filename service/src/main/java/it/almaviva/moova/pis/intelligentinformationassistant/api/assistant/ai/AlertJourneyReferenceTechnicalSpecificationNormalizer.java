package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AlertJourneyReferenceTechnicalSpecificationNormalizer {

    private static final String JOURNEY_DETAILS_PATH = "payload.stopPointJourney.stopPointsJourneyDetails[]";
    private static final String VEHICLE_JOURNEY_NAME = "vehicleJourneyName";
    private static final String LINE = "line.dsc";
    private static final String SERVICE_CATEGORY = "serviceCategory.dsc";
    private static final String TRANSPORT_OPERATOR = "transportOperator.dsc";
    private static final Set<String> COMPATIBLE_UNQUALIFIED_FIELDS =
            Set.of(VEHICLE_JOURNEY_NAME, LINE, SERVICE_CATEGORY, TRANSPORT_OPERATOR);
    private static final Set<String> CANONICAL_UNQUALIFIED_FIELDS =
            Set.of(LINE, SERVICE_CATEGORY, TRANSPORT_OPERATOR);
    private static final Set<String> POSITIVE_TEXT_OPERATORS =
            Set.of("EQUALS_NORMALIZED", "CONTAINS_NORMALIZED");

    public AlertVerificationOutcome normalize(
            AlertVerificationOutcome outcome,
            AlertVerificationLocationContext locationContext,
            String alertId) {
        if (outcome == null
                || outcome.decision() != AlertVerificationDecision.VERIFIED
                || locationContext == null
                || outcome.technicalSpecification() == null) {
            return outcome;
        }
        List<AlertVerificationLocationContext.NonLocationConstraint> references = locationContext
                .nonLocationConstraints()
                .stream()
                .filter(constraint -> "JOURNEY_REFERENCE".equalsIgnoreCase(constraint.type()))
                .filter(AlertVerificationLocationContext.NonLocationConstraint::requiredCoverage)
                .toList();
        if (references.size() != 1) {
            return outcome;
        }
        AlertVerificationLocationContext.NonLocationConstraint reference = references.getFirst();
        AlertJourneyReferenceKind kind = parseKind(reference.kind());
        if (kind != AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR) {
            return outcome;
        }
        String expectedValue = firstNonBlank(reference.normalizedValue(), reference.rawText());
        if (expectedValue == null || expectedValue.isBlank()) {
            return outcome;
        }

        Map<String, Object> technicalSpecification = mutableMap(outcome.technicalSpecification());
        NormalizationResult technical = normalizeCondition(
                technicalSpecification.get("condition"),
                expectedValue);
        if (!technical.changed()) {
            log(alertId, reference, technical, "technicalSpecification");
            return outcome;
        }

        Map<String, Object> agentBlueprintPreview = mutableMap(outcome.agentBlueprintPreview());
        NormalizationResult preview = NormalizationResult.noop("preview-missing");
        if (agentBlueprintPreview != null
                && agentBlueprintPreview.get("parameters") instanceof Map<?, ?> parametersMap) {
            preview = normalizeCondition(((Map<?, ?>) parametersMap).get("condition"), expectedValue);
            if (!preview.changed() && !"already-canonical".equals(preview.reason())) {
                ((Map<String, Object>) parametersMap).put("condition",
                        mutableValue(technicalSpecification.get("condition")));
                preview = new NormalizationResult(true, "aligned-from-normalized-technical-specification");
            }
        }
        log(alertId, reference, technical, "technicalSpecification");
        if (agentBlueprintPreview != null) {
            log(alertId, reference, preview, "agentBlueprintPreview");
        }

        return new AlertVerificationOutcome(
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
                outcome.requirementCoverage(),
                outcome.warnings(),
                outcome.safetyChecks());
    }

    private NormalizationResult normalizeCondition(Object condition, String expectedValue) {
        if (condition == null) {
            return NormalizationResult.noop("missing-condition");
        }
        List<LeafRef> compatibleLeaves = new ArrayList<>();
        List<String> canonicalGroups = new ArrayList<>();
        collect(condition, null, compatibleLeaves, canonicalGroups);
        if (canonicalGroups.size() == 1 && compatibleLeaves.isEmpty()) {
            return NormalizationResult.noop("already-canonical");
        }
        List<LeafRef> matchingLeaves = compatibleLeaves.stream()
                .filter(leaf -> sameNormalizedValue(expectedValue, stringValue(leaf.node().get("value"))))
                .toList();
        if (compatibleLeaves.size() != 1 || matchingLeaves.size() != 1 || !canonicalGroups.isEmpty()) {
            return NormalizationResult.noop("ambiguous-or-contradictory");
        }
        LeafRef match = matchingLeaves.getFirst();
        match.node().clear();
        match.node().put("any", canonicalDescriptorAny(expectedValue));
        return new NormalizationResult(true, "unqualified-descriptor-leaf-to-correlated-or");
    }

    @SuppressWarnings("unchecked")
    private void collect(
            Object node,
            String arrayPath,
            List<LeafRef> compatibleLeaves,
            List<String> canonicalGroups) {
        if (node instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = (Map<String, Object>) rawMap;
            String nextArrayPath = arrayPath;
            Object anyElement = map.get("anyElement");
            if (anyElement instanceof Map<?, ?> anyElementMap) {
                nextArrayPath = resolveArrayPath(arrayPath, stringValue(anyElementMap.get("path")));
            }
            if (JOURNEY_DETAILS_PATH.equals(nextArrayPath)
                    && isPositiveCompatibleLeaf(map, nextArrayPath)) {
                compatibleLeaves.add(new LeafRef(map));
                return;
            }
            if (JOURNEY_DETAILS_PATH.equals(arrayPath) && isCanonicalAnyGroup(map)) {
                canonicalGroups.add(String.valueOf(System.identityHashCode(map)));
                return;
            }
            for (Object value : map.values()) {
                collect(value, nextArrayPath, compatibleLeaves, canonicalGroups);
            }
        } else if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                collect(item, arrayPath, compatibleLeaves, canonicalGroups);
            }
        }
    }

    private boolean isPositiveCompatibleLeaf(Map<String, Object> map, String arrayPath) {
        String field = stringValue(map.get("field"));
        String operator = stringValue(map.get("operator"));
        if (!JOURNEY_DETAILS_PATH.equals(arrayPath)
                || field == null
                || operator == null
                || !COMPATIBLE_UNQUALIFIED_FIELDS.contains(field)
                || !POSITIVE_TEXT_OPERATORS.contains(operator)) {
            return false;
        }
        return ServiceDataCapabilityCatalog.isAllowedOperator(JOURNEY_DETAILS_PATH + "." + field, operator);
    }

    private boolean isCanonicalAnyGroup(Map<String, Object> map) {
        Object any = map.get("any");
        if (!(any instanceof List<?> list) || list.size() != 3) {
            return false;
        }
        Set<String> fields = new java.util.LinkedHashSet<>();
        String value = null;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> leaf)) {
                return false;
            }
            String field = stringValue(leaf.get("field"));
            String operator = stringValue(leaf.get("operator"));
            String leafValue = stringValue(leaf.get("value"));
            if (!CANONICAL_UNQUALIFIED_FIELDS.contains(field)
                    || !POSITIVE_TEXT_OPERATORS.contains(operator)
                    || !ServiceDataCapabilityCatalog.isAllowedOperator(JOURNEY_DETAILS_PATH + "." + field, operator)) {
                return false;
            }
            if (value == null) {
                value = normalizeText(leafValue);
            } else if (!value.equals(normalizeText(leafValue))) {
                return false;
            }
            fields.add(field);
        }
        return fields.equals(CANONICAL_UNQUALIFIED_FIELDS);
    }

    private List<Map<String, Object>> canonicalDescriptorAny(String value) {
        return List.of(
                canonicalLeaf(LINE, value),
                canonicalLeaf(SERVICE_CATEGORY, value),
                canonicalLeaf(TRANSPORT_OPERATOR, value));
    }

    private Map<String, Object> canonicalLeaf(String field, String value) {
        Map<String, Object> leaf = new LinkedHashMap<>();
        leaf.put("field", field);
        leaf.put("operator", "EQUALS_NORMALIZED");
        leaf.put("value", value);
        return leaf;
    }

    private String resolveArrayPath(String parentArrayPath, String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return parentArrayPath;
        }
        if (rawPath.startsWith("payload.")) {
            return rawPath;
        }
        if (parentArrayPath == null || parentArrayPath.isBlank()) {
            return rawPath;
        }
        return parentArrayPath + "." + rawPath;
    }

    private AlertJourneyReferenceKind parseKind(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return AlertJourneyReferenceKind.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean sameNormalizedValue(String expected, String actual) {
        String normalizedExpected = normalizeText(expected);
        String normalizedActual = normalizeText(actual);
        return normalizedExpected != null && normalizedExpected.equals(normalizedActual);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Map<String, Object> mutableMap(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key, mutableValue(value)));
        return result;
    }

    private Object mutableValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(String.valueOf(key), mutableValue(item)));
            return result;
        }
        if (value instanceof Collection<?> collection) {
            List<Object> result = new ArrayList<>();
            collection.forEach(item -> result.add(mutableValue(item)));
            return result;
        }
        return value;
    }

    private void log(
            String alertId,
            AlertVerificationLocationContext.NonLocationConstraint reference,
            NormalizationResult result,
            String target) {
        System.out.println("[IIA][ALERT_VERIFY][JOURNEY_REFERENCE_NORMALIZATION] alertId=" + alertId
                + " target=" + target
                + " kind=" + reference.kind()
                + " rawText=" + reference.rawText()
                + " normalizedValue=" + reference.normalizedValue()
                + " changed=" + result.changed()
                + " reason=" + result.reason());
    }

    private record LeafRef(Map<String, Object> node) {
    }

    private record NormalizationResult(boolean changed, String reason) {
        static NormalizationResult noop(String reason) {
            return new NormalizationResult(false, reason);
        }
    }
}
