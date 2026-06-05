package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ApplicationScoped
public class ScheduledAlertLocationUnderstandingValidator {

    public ScheduledAlertLocationUnderstandingResult validate(
            ScheduledAlertLocationUnderstandingResult result,
            String prompt,
            ScheduledAlertLocationUnderstandingHints hints) {
        ScheduledAlertLocationUnderstandingHints safeHints =
                hints == null ? ScheduledAlertLocationUnderstandingHints.empty() : hints;
        if (result == null) {
            return ScheduledAlertLocationUnderstandingResult.emptyWithWarnings(
                    List.of("Scheduled location understanding failed: no result."));
        }

        List<String> warnings = new ArrayList<>(result.warnings());
        List<ScheduledAlertLocationMention> locations = new ArrayList<>();
        List<ScheduledAlertNonLocationConstraint> nonLocationConstraints =
                new ArrayList<>(result.nonLocationConstraints());
        Set<String> locationKeys = new LinkedHashSet<>();
        boolean removedPlatformLocation = false;

        for (ScheduledAlertLocationMention location : result.locations()) {
            if (isPlatformLike(location.rawText())) {
                removedPlatformLocation = true;
                warnings.add("Location mention '" + location.rawText()
                        + "' looks like a platform/binario constraint and was moved to nonLocationConstraints.");
                addNonLocationConstraintIfMissing(
                        nonLocationConstraints,
                        ScheduledAlertNonLocationConstraintType.PLATFORM,
                        location.rawText());
                continue;
            }
            String key = normalize(location.rawText()) + "|" + location.role();
            if (!locationKeys.add(key)) {
                warnings.add("Duplicate scheduled location mention ignored: " + location.rawText()
                        + " role=" + location.role());
                continue;
            }
            locations.add(location);
        }

        if (safeHints.containsPlatformExpression() && !hasPlatformConstraint(nonLocationConstraints)) {
            addNonLocationConstraintIfMissing(
                    nonLocationConstraints,
                    ScheduledAlertNonLocationConstraintType.PLATFORM,
                    "platform/binario expression");
            warnings.add("Backend hints detected a platform/binario expression; added PLATFORM non-location constraint.");
        }
        String normalizedPrompt = normalize(prompt);
        if (normalizedPrompt.contains("ritardo") || normalizedPrompt.contains("delay")) {
            addNonLocationConstraintIfMissing(
                    nonLocationConstraints,
                    ScheduledAlertNonLocationConstraintType.DELAY,
                    "delay expression");
        }
        if (normalizedPrompt.contains("carrozze")
                || normalizedPrompt.contains("vagoni")
                || normalizedPrompt.contains("carriages")
                || normalizedPrompt.contains("coaches")) {
            addNonLocationConstraintIfMissing(
                    nonLocationConstraints,
                    ScheduledAlertNonLocationConstraintType.UNSUPPORTED_CAPABILITY,
                    "train composition/carriages");
        }

        ScheduledAlertMonitoringScope monitoringScope = result.monitoringScope();
        boolean hasMonitored = locations.stream()
                .anyMatch(location -> location.role() == ScheduledAlertLocationRole.MONITORED_STOP_POINT);
        if (monitoringScope == ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS && !hasMonitored) {
            monitoringScope = ScheduledAlertMonitoringScope.UNSPECIFIED;
            warnings.add("A monitored stop point is required for ServiceData API snapshot, but only filter/control locations were identified.");
        }
        if (safeHints.containsAllLocationsExpression()) {
            monitoringScope = ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS;
        }
        if (removedPlatformLocation && locations.isEmpty() && monitoringScope == ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS) {
            monitoringScope = ScheduledAlertMonitoringScope.UNSPECIFIED;
        }

        return new ScheduledAlertLocationUnderstandingResult(
                !locations.isEmpty(),
                result.language(),
                monitoringScope,
                locations,
                nonLocationConstraints,
                warnings);
    }

    private void addNonLocationConstraintIfMissing(
            List<ScheduledAlertNonLocationConstraint> constraints,
            ScheduledAlertNonLocationConstraintType type,
            String rawText) {
        String normalizedRawText = normalize(rawText);
        boolean present = constraints.stream()
                .anyMatch(constraint -> constraint.type() == type
                        && normalize(constraint.rawText()).equals(normalizedRawText));
        if (!present) {
            constraints.add(new ScheduledAlertNonLocationConstraint(type, rawText));
        }
    }

    private boolean hasPlatformConstraint(List<ScheduledAlertNonLocationConstraint> constraints) {
        return constraints.stream()
                .anyMatch(constraint -> constraint.type() == ScheduledAlertNonLocationConstraintType.PLATFORM
                        || constraint.type() == ScheduledAlertNonLocationConstraintType.PLATFORM_NUMERIC);
    }

    private boolean isPlatformLike(String value) {
        String normalized = normalize(value);
        return normalized.matches(".*\\b(binario|platform|track|quay|banchina|marciapiede)\\b.*");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
