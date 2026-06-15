package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScheduledAlertLocationUnderstandingFallbackClassifier {

    private static final double MONITORING_CONFIDENCE = 0.82;
    private static final double CONDITIONAL_CONFIDENCE = 0.78;
    private static final Pattern AMBIGUOUS_BETWEEN = Pattern.compile("\\btra\\s+.+\\s+e\\s+.+", Pattern.CASE_INSENSITIVE);
    private static final String STOP_MARKERS =
            "(?=\\s+(?:con\\s+destinazione|destinazione|dirett[ie]\\s+a|verso|per|con\\s+origine|origine|provenient[ie]\\s+da|che\\s+passano\\s+da|passano\\s+da|passano\\s+per|via|in\\s+transito\\s+da|in\\s+transito\\s+a|ogni|quando|se)\\b|[,.!?;:]|$)";

    public Optional<ScheduledAlertLocationUnderstandingResult> classify(
            String alertId,
            String prompt,
            ScheduledAlertLocationUnderstandingHints hints,
            String technicalReason) {
        String reason = technicalReason == null || technicalReason.isBlank()
                ? "unknown scheduled-location LLM failure"
                : technicalReason;
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FALLBACK] LLM scheduled location failed alertId="
                + alertId + " reason=" + reason);
        if (prompt == null || prompt.isBlank()) {
            return notApplicable(alertId, "blank-prompt");
        }
        if (isAmbiguousBetweenPrompt(prompt)) {
            return notApplicable(alertId, "ambiguous-between-locations");
        }

        List<ScheduledAlertLocationMention> monitoringCandidates = new ArrayList<>();
        List<ScheduledAlertLocationMention> conditionalCandidates = new ArrayList<>();

        addMonitoringCandidate(monitoringCandidates, prompt, "ci\\s+sono\\s+a\\s+", alertId);
        addMonitoringCandidate(monitoringCandidates, prompt, "presenti\\s+a\\s+", alertId);
        addMonitoringCandidate(monitoringCandidates, prompt, "alla\\s+fermata\\s+", alertId);
        addMonitoringCandidate(monitoringCandidates, prompt, "nella\\s+stazione\\s+", alertId);
        addMonitoringCandidate(monitoringCandidates, prompt, "in\\s+partenza\\s+ci\\s+sono\\s+da\\s+", alertId);
        addMonitoringCandidate(monitoringCandidates, prompt, "partenze\\s+da\\s+", alertId);
        addMonitoringCandidate(monitoringCandidates, prompt, "in\\s+arrivo\\s+ci\\s+sono\\s+a\\s+", alertId);
        addMonitoringCandidate(monitoringCandidates, prompt, "arrivi\\s+a\\s+", alertId);
        addMonitoringCandidate(monitoringCandidates, prompt, "quante\\s+corse\\s+a\\s+", alertId);

        if (monitoringCandidates.isEmpty()) {
            addDaAFormCandidates(prompt, monitoringCandidates, conditionalCandidates, alertId);
        }
        monitoringCandidates = deduplicateByTextAndRole(monitoringCandidates);
        if (monitoringCandidates.size() != 1) {
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FALLBACK] candidates alertId=" + alertId
                    + " monitoringCandidates=" + rawTexts(monitoringCandidates)
                    + " conditionalCandidates=" + rawTexts(conditionalCandidates));
            return notApplicable(alertId, "monitoring-location-not-unique");
        }

        addConditionalCandidate(conditionalCandidates, prompt,
                "(?:con\\s+destinazione|destinazione|dirett[ie]\\s+a|verso)\\s+",
                ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                ScheduledAlertLocationRelation.DESTINATION_FILTER,
                alertId);
        addConditionalCandidate(conditionalCandidates, prompt,
                "per\\s+",
                ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                ScheduledAlertLocationRelation.DESTINATION_FILTER,
                alertId);
        addConditionalCandidate(conditionalCandidates, prompt,
                "(?:con\\s+origine|origine|provenient[ie]\\s+da)\\s+",
                ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT,
                ScheduledAlertLocationRelation.ORIGIN_FILTER,
                alertId);
        addConditionalCandidate(conditionalCandidates, prompt,
                "(?:che\\s+passano\\s+da|passano\\s+da|passano\\s+per|via|in\\s+transito\\s+da|in\\s+transito\\s+a)\\s+",
                ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT,
                ScheduledAlertLocationRelation.ROUTE_FILTER,
                alertId);

        List<ScheduledAlertLocationMention> deduplicated = deduplicate(monitoringCandidates, conditionalCandidates);
        if (deduplicated.stream().noneMatch(location -> location.role() == ScheduledAlertLocationRole.MONITORED_STOP_POINT)) {
            return notApplicable(alertId, "monitoring-location-missing");
        }
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FALLBACK] candidates alertId=" + alertId
                + " monitoringCandidates=" + rawTexts(monitoringCandidates)
                + " conditionalCandidates=" + rawTexts(conditionalCandidates));
        String warning = "Scheduled location LLM failed with " + reason
                + "; deterministic scheduled-location fallback selected monitoring scope from prompt structure.";
        ScheduledAlertLocationUnderstandingResult result = new ScheduledAlertLocationUnderstandingResult(
                true,
                "it",
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                deduplicated,
                List.of(),
                List.of(warning));
        long monitoredCount = deduplicated.stream()
                .filter(location -> location.role() == ScheduledAlertLocationRole.MONITORED_STOP_POINT)
                .count();
        long conditionalCount = deduplicated.size() - monitoredCount;
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FALLBACK] result built alertId=" + alertId
                + " monitoringScope=EXPLICIT_STOP_POINTS monitoredCount=" + monitoredCount
                + " conditionalCount=" + conditionalCount);
        return Optional.of(result);
    }

    private void addMonitoringCandidate(
            List<ScheduledAlertLocationMention> candidates,
            String prompt,
            String markerRegex,
            String alertId) {
        Matcher matcher = Pattern.compile(markerRegex + "(.+?)" + STOP_MARKERS, Pattern.CASE_INSENSITIVE)
                .matcher(prompt);
        while (matcher.find()) {
            String text = cleanCandidate(matcher.group(1));
            if (isUsableCandidate(text)) {
                candidates.add(monitored(text, "G1"));
                System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FALLBACK] selected monitoring location alertId="
                        + alertId + " text=" + text);
            }
        }
    }

    private void addConditionalCandidate(
            List<ScheduledAlertLocationMention> candidates,
            String prompt,
            String markerRegex,
            ScheduledAlertLocationRole role,
            ScheduledAlertLocationRelation relation,
            String alertId) {
        Matcher matcher = Pattern.compile(markerRegex + "(.+?)" + STOP_MARKERS, Pattern.CASE_INSENSITIVE)
                .matcher(prompt);
        while (matcher.find()) {
            String text = cleanCandidate(matcher.group(1));
            if (isUsableCandidate(text)) {
                candidates.add(filter(text, role, relation, "G" + (candidates.size() + 2)));
                System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FALLBACK] selected conditional location alertId="
                        + alertId + " role=" + role + " text=" + text);
            }
        }
    }

    private void addDaAFormCandidates(
            String prompt,
            List<ScheduledAlertLocationMention> monitoringCandidates,
            List<ScheduledAlertLocationMention> conditionalCandidates,
            String alertId) {
        Matcher matcher = Pattern.compile(
                        "\\bquante\\s+corse\\s+da\\s+(.+?)\\s+a\\s+(.+?)" + STOP_MARKERS,
                        Pattern.CASE_INSENSITIVE)
                .matcher(prompt);
        if (matcher.find()) {
            String origin = cleanCandidate(matcher.group(1));
            String destination = cleanCandidate(matcher.group(2));
            if (isUsableCandidate(origin) && isUsableCandidate(destination)) {
                monitoringCandidates.add(monitored(origin, "G1"));
                conditionalCandidates.add(filter(destination,
                        ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                        ScheduledAlertLocationRelation.DESTINATION_FILTER,
                        "G2"));
                System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FALLBACK] selected monitoring location alertId="
                        + alertId + " text=" + origin);
                System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FALLBACK] selected conditional location alertId="
                        + alertId + " role=" + ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT
                        + " text=" + destination);
            }
        }
    }

    private List<ScheduledAlertLocationMention> deduplicate(
            List<ScheduledAlertLocationMention> monitoringCandidates,
            List<ScheduledAlertLocationMention> conditionalCandidates) {
        List<ScheduledAlertLocationMention> result = new ArrayList<>();
        for (ScheduledAlertLocationMention location : monitoringCandidates) {
            addIfMissing(result, location);
        }
        for (ScheduledAlertLocationMention location : conditionalCandidates) {
            if (result.stream().noneMatch(existing -> sameText(existing.rawText(), location.rawText())
                    && existing.role() == ScheduledAlertLocationRole.MONITORED_STOP_POINT)) {
                addIfMissing(result, location);
            }
        }
        return List.copyOf(result);
    }

    private List<ScheduledAlertLocationMention> deduplicateByTextAndRole(List<ScheduledAlertLocationMention> locations) {
        List<ScheduledAlertLocationMention> result = new ArrayList<>();
        for (ScheduledAlertLocationMention location : locations) {
            addIfMissing(result, location);
        }
        return result;
    }

    private void addIfMissing(List<ScheduledAlertLocationMention> result, ScheduledAlertLocationMention location) {
        boolean present = result.stream()
                .anyMatch(existing -> sameText(existing.rawText(), location.rawText())
                        && existing.role() == location.role());
        if (!present) {
            result.add(location);
        }
    }

    private ScheduledAlertLocationMention monitored(String text, String group) {
        return new ScheduledAlertLocationMention(
                text,
                text,
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationRelation.SERVICE_DATA_API_QUERY_STOP_POINT,
                true,
                true,
                ScheduledAlertLocationPolarity.INCLUDE,
                group,
                MONITORING_CONFIDENCE);
    }

    private ScheduledAlertLocationMention filter(
            String text,
            ScheduledAlertLocationRole role,
            ScheduledAlertLocationRelation relation,
            String group) {
        return new ScheduledAlertLocationMention(
                text,
                text,
                role,
                relation,
                false,
                true,
                ScheduledAlertLocationPolarity.INCLUDE,
                group,
                CONDITIONAL_CONFIDENCE);
    }

    private Optional<ScheduledAlertLocationUnderstandingResult> notApplicable(String alertId, String reason) {
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FALLBACK] not applicable alertId="
                + alertId + " reason=" + reason);
        return Optional.empty();
    }

    private boolean isAmbiguousBetweenPrompt(String prompt) {
        return AMBIGUOUS_BETWEEN.matcher(normalize(prompt)).find();
    }

    private boolean isUsableCandidate(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = normalize(value);
        return !normalized.contains(" quante ")
                && !normalized.matches(".*\\b(corse|corsa|treni|treno|journeys|journey|train|trains)\\b.*")
                && !normalized.matches(".*\\b(soppresse|soppressa|soppresso|cancellate|cancellata|cancelled|suppressed)\\b.*");
    }

    private boolean sameText(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private List<String> rawTexts(List<ScheduledAlertLocationMention> locations) {
        return locations.stream()
                .map(ScheduledAlertLocationMention::rawText)
                .toList();
    }

    private String cleanCandidate(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.trim()
                .replaceAll("^[\"'`]+|[\"'`]+$", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*(?:[,.!?;:])+$", "")
                .trim();
        return cleaned.replaceAll("^(?:la|il|lo|l'|le|gli|i)\\s+", "").trim();
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
