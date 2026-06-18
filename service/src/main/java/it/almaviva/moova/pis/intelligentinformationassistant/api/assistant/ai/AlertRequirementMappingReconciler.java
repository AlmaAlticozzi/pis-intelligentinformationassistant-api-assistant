package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AlertRequirementMappingReconciler {

    private static final String JOURNEY_DETAILS_PATH = "payload.stopPointJourney.stopPointsJourneyDetails[]";
    private static final String VEHICLE_JOURNEY_NAME = JOURNEY_DETAILS_PATH + ".vehicleJourneyName";
    private static final String LINE = JOURNEY_DETAILS_PATH + ".line.dsc";
    private static final String SERVICE_CATEGORY = JOURNEY_DETAILS_PATH + ".serviceCategory.dsc";
    private static final String TRANSPORT_OPERATOR = JOURNEY_DETAILS_PATH + ".transportOperator.dsc";
    private static final Set<String> COMPATIBLE_JOURNEY_FIELDS =
            Set.of(VEHICLE_JOURNEY_NAME, LINE, SERVICE_CATEGORY, TRANSPORT_OPERATOR);
    private static final List<String> CANONICAL_UNQUALIFIED_MAPPED_BY =
            List.of(LINE, SERVICE_CATEGORY, TRANSPORT_OPERATOR);

    public AlertVerificationOutcome reconcile(
            AlertVerificationOutcome outcome,
            AlertVerificationLocationContext locationContext,
            String alertId) {
        if (outcome == null
                || outcome.decision() != AlertVerificationDecision.VERIFIED
                || locationContext == null
                || outcome.technicalSpecification() == null
                || outcome.requirementCoverage() == null) {
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
        List<String> expectedValues = expectedValues(reference);
        if (expectedValues.isEmpty()) {
            return outcome;
        }
        if (!containsCanonicalUnqualifiedDescriptorOr(
                outcome.technicalSpecification().get("condition"),
                expectedValues,
                null)) {
            return outcome;
        }

        Map<String, Object> requirementCoverage = mutableMap(outcome.requirementCoverage());
        ReconciliationResult result = reconcileRequirementCoverage(requirementCoverage, reference, expectedValues);
        Map<String, Object> agentBlueprintPreview = mutableMap(outcome.agentBlueprintPreview());
        if (result.changed() && agentBlueprintPreview != null) {
            reconcileBlueprintMetadata(agentBlueprintPreview, result.originalMappedBy(), result.effectiveMappedBy());
        }
        if (!result.changed()) {
            return outcome;
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
                outcome.technicalSpecification(),
                agentBlueprintPreview,
                requirementCoverage,
                outcome.warnings(),
                outcome.safetyChecks());
    }

    @SuppressWarnings("unchecked")
    private ReconciliationResult reconcileRequirementCoverage(
            Map<String, Object> requirementCoverage,
            AlertVerificationLocationContext.NonLocationConstraint reference,
            List<String> expectedValues) {
        Object requirements = requirementCoverage.get("requirements");
        if (!(requirements instanceof List<?> list)) {
            return ReconciliationResult.noop();
        }
        boolean changed = false;
        List<String> firstOriginal = List.of();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> rawRequirement)) {
                continue;
            }
            Map<String, Object> requirement = (Map<String, Object>) rawRequirement;
            String text = stringValue(requirement.get("text"));
            List<String> mappedBy = stringList(requirement.get("mappedBy"));
            if (!isOwnedByJourneyReference(text, mappedBy, expectedValues)) {
                continue;
            }
            if (mappedBy.contains(VEHICLE_JOURNEY_NAME)) {
                System.out.println("[IIA][ALERT_VERIFY][REQUIREMENT_MAPPING_STALE]\n"
                        + "requirement=" + text + "\n"
                        + "staleField=" + VEHICLE_JOURNEY_NAME + "\n"
                        + "replacementOwner=JOURNEY_REFERENCE");
            }
            List<String> effectiveMappedBy = List.copyOf(CANONICAL_UNQUALIFIED_MAPPED_BY);
            requirement.put("mappedBy", effectiveMappedBy);
            requirement.put("semanticOwner", "JOURNEY_REFERENCE");
            requirement.put("semanticKind", reference.kind());
            requirement.put("normalizedValue", expectedValues.getFirst());
            requirement.put("normalizedValues", expectedValues);
            System.out.println("[IIA][ALERT_VERIFY][REQUIREMENT_MAPPING_RECONCILIATION]\n"
                    + "requirement=" + text + "\n"
                    + "semanticOwner=JOURNEY_REFERENCE\n"
                    + "kind=" + reference.kind() + "\n"
                    + "normalizedValue=" + expectedValues.getFirst() + "\n"
                    + "normalizedValues=" + expectedValues + "\n"
                    + "originalMappedBy=" + mappedBy + "\n"
                    + "effectiveMappedBy=" + effectiveMappedBy + "\n"
                    + "covered=true\n"
                    + "reason=canonical-unqualified-descriptor-or");
            changed = true;
            if (firstOriginal.isEmpty()) {
                firstOriginal = mappedBy;
            }
        }
        return changed
                ? new ReconciliationResult(true, firstOriginal, List.copyOf(CANONICAL_UNQUALIFIED_MAPPED_BY))
                : ReconciliationResult.noop();
    }

    private boolean isOwnedByJourneyReference(String text, List<String> mappedBy, List<String> expectedValues) {
        if (expectedValues.isEmpty()) {
            return false;
        }
        String normalizedText = normalizeText(text);
        boolean sameValue = expectedValues.stream()
                .allMatch(value -> normalizedText.contains(normalizeText(value)));
        boolean journeyMapped = mappedBy.stream()
                .map(this::normalizeMappedByField)
                .anyMatch(COMPATIBLE_JOURNEY_FIELDS::contains);
        return sameValue && journeyMapped;
    }

    @SuppressWarnings("unchecked")
    private void reconcileBlueprintMetadata(Object node, List<String> originalMappedBy, List<String> effectiveMappedBy) {
        if (node instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = (Map<String, Object>) rawMap;
            Object mappedBy = map.get("mappedBy");
            if (mappedBy instanceof List<?> list) {
                List<String> fields = stringList(list);
                boolean stale = fields.stream()
                        .map(this::normalizeMappedByField)
                        .anyMatch(originalMappedBy::contains);
                if (stale) {
                    map.put("mappedBy", effectiveMappedBy);
                    map.put("semanticOwner", "JOURNEY_REFERENCE");
                }
            }
            for (Object value : map.values()) {
                reconcileBlueprintMetadata(value, originalMappedBy, effectiveMappedBy);
            }
        } else if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                reconcileBlueprintMetadata(item, originalMappedBy, effectiveMappedBy);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean containsCanonicalUnqualifiedDescriptorOr(Object node, List<String> expectedValues, String arrayPath) {
        if (node instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = (Map<String, Object>) rawMap;
            String nextArrayPath = arrayPath;
            Object anyElement = map.get("anyElement");
            if (anyElement instanceof Map<?, ?> anyElementMap) {
                nextArrayPath = resolveArrayPath(arrayPath, stringValue(anyElementMap.get("path")));
            }
            if (JOURNEY_DETAILS_PATH.equals(arrayPath) && expectedValues.stream()
                    .allMatch(value -> isCanonicalAnyGroup(map, value) || containsCanonicalUnqualifiedDescriptorOr(map.get("any"), List.of(value), arrayPath))) {
                return true;
            }
            for (Object value : map.values()) {
                if (containsCanonicalUnqualifiedDescriptorOr(value, expectedValues, nextArrayPath)) {
                    return true;
                }
            }
        } else if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (containsCanonicalUnqualifiedDescriptorOr(item, expectedValues, arrayPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCanonicalAnyGroup(Map<String, Object> map, String expectedValue) {
        Object any = map.get("any");
        if (!(any instanceof List<?> list) || list.size() != 3) {
            return false;
        }
        Set<String> fields = new LinkedHashSet<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> leaf)) {
                return false;
            }
            String field = normalizeMappedByField(stringValue(leaf.get("field")));
            String value = stringValue(leaf.get("value"));
            String operator = stringValue(leaf.get("operator"));
            if (!CANONICAL_UNQUALIFIED_MAPPED_BY.contains(field)
                    || !"EQUALS_NORMALIZED".equals(operator)
                    || !sameNormalizedValue(expectedValue, value)) {
                return false;
            }
            fields.add(field);
        }
        return fields.equals(new LinkedHashSet<>(CANONICAL_UNQUALIFIED_MAPPED_BY));
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
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean sameNormalizedValue(String expected, String actual) {
        String normalizedExpected = normalizeText(expected);
        String normalizedActual = normalizeText(actual);
        return !normalizedExpected.isBlank() && normalizedExpected.equals(normalizedActual);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeMappedByField(String field) {
        if (field == null || field.isBlank()) {
            return "";
        }
        String trimmed = field.trim();
        if (trimmed.startsWith(JOURNEY_DETAILS_PATH + ".")) {
            return trimmed;
        }
        if (trimmed.equals("vehicleJourneyName")
                || trimmed.equals("line.dsc")
                || trimmed.equals("serviceCategory.dsc")
                || trimmed.equals("transportOperator.dsc")) {
            return JOURNEY_DETAILS_PATH + "." + trimmed;
        }
        return trimmed;
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private List<String> expectedValues(AlertVerificationLocationContext.NonLocationConstraint reference) {
        if (reference.normalizedValues() != null && !reference.normalizedValues().isEmpty()) {
            return reference.normalizedValues();
        }
        String expectedValue = firstNonBlank(reference.normalizedValue(), reference.rawText());
        return expectedValue == null || expectedValue.isBlank() ? List.of() : List.of(expectedValue);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private List<String> stringList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::stringValue)
                    .filter(item -> item != null && !item.isBlank())
                    .map(this::normalizeMappedByField)
                    .toList();
        }
        return List.of();
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

    private record ReconciliationResult(boolean changed, List<String> originalMappedBy, List<String> effectiveMappedBy) {
        static ReconciliationResult noop() {
            return new ReconciliationResult(false, List.of(), List.of());
        }
    }
}
