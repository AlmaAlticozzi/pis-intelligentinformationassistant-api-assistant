package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScheduledAlertTemporalHintsExtractor {

    private static final Map<String, Integer> SMALL_NUMBERS = Map.ofEntries(
            Map.entry("one", 1),
            Map.entry("two", 2),
            Map.entry("three", 3),
            Map.entry("four", 4),
            Map.entry("five", 5),
            Map.entry("uno", 1),
            Map.entry("una", 1),
            Map.entry("due", 2),
            Map.entry("tre", 3),
            Map.entry("quattro", 4),
            Map.entry("cinque", 5));

    private static final Pattern FREQUENCY_WITH_NUMBER = Pattern.compile(
            "\\b(?:every|ogni)\\s+(\\d+|one|two|three|four|five|uno|una|due|tre|quattro|cinque)\\s+(minutes?|mins?|minuti|min|hours?|h|ore)\\b");
    private static final Pattern FREQUENCY_HOURLY = Pattern.compile("\\b(every\\s+hour|hourly|ogni\\s+ora)\\b");
    private static final Pattern FREQUENCY_DAILY = Pattern.compile("\\b(daily|every\\s+day|ogni\\s+giorno)\\b");
    private static final Pattern LOOKAHEAD = Pattern.compile(
            "\\b(?:(?:in\\s+the\\s+)?next|from\\s+now\\s+for|nelle\\s+prossime|nei\\s+prossimi|prossime|prossimi|da\\s+ora\\s+per)\\s+"
                    + "(\\d+|one|two|three|four|five|uno|una|due|tre|quattro|cinque)\\s+"
                    + "(minutes?|mins?|minuti|min|hours?|h|ore)\\b");
    private static final Pattern LOOKAHEAD_TYPO_TOLERANT = Pattern.compile(
            "\\b(?:nelle\\s+(?:possime|prossim|prosim|prosime)|nei\\s+(?:possim[i]?|prossim|prosim|prosim[i]?))\\s+"
                    + "(\\d+|one|two|three|four|five|uno|una|due|tre|quattro|cinque)\\s+"
                    + "(minutes?|mins?|minuti|min|hours?|h|ore)\\b");
    private static final String TIME = "([0-2]?\\d(?::[0-5]\\d)?)";
    private static final Pattern JOURNEY_BETWEEN = Pattern.compile(
            "\\b(?:between|from|tra\\s+le|tra|dalle|dalla)\\s+" + TIME
                    + "\\s+(?:and|to|e|alle|alla|a)(?:\\s+le|\\s+ore)?\\s+" + TIME + "\\b");
    private static final Pattern JOURNEY_AFTER = Pattern.compile("\\b(?:after|dopo\\s+le|dopo)\\s+" + TIME + "\\b");
    private static final Pattern JOURNEY_BEFORE = Pattern.compile("\\b(?:before|prima\\s+delle|prima\\s+di|prima)\\s+" + TIME + "\\b");

    @ConfigProperty(name = "iia.alert.scheduled-verify.default-frequency-seconds", defaultValue = "600")
    int defaultFrequencySeconds = 600;

    @ConfigProperty(name = "iia.alert.scheduled-verify.min-frequency-seconds", defaultValue = "60")
    int minFrequencySeconds = 60;

    @ConfigProperty(name = "iia.alert.scheduled-verify.max-frequency-seconds", defaultValue = "86400")
    int maxFrequencySeconds = 86400;

    @ConfigProperty(name = "iia.alert.scheduled-verify.default-lookahead-minutes", defaultValue = "480")
    int defaultLookaheadMinutes = 480;

    @ConfigProperty(name = "iia.alert.scheduled-verify.min-lookahead-minutes", defaultValue = "1")
    int minLookaheadMinutes = 1;

    @ConfigProperty(name = "iia.alert.scheduled-verify.max-lookahead-minutes", defaultValue = "1440")
    int maxLookaheadMinutes = 1440;

    public ScheduledAlertTemporalHints extract(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null) {
            return defaultHints();
        }

        TemporalMatch frequency = frequency(normalized, prompt);
        TemporalMatch lookahead = lookahead(normalized, prompt);
        List<ScheduledAlertJourneyTimeFilter> journeyTimeFilters = journeyTimeFilters(normalized, prompt);
        return new ScheduledAlertTemporalHints(
                frequency != null,
                frequency == null ? defaultFrequencySeconds : clamp(frequency.value(), minFrequencySeconds, maxFrequencySeconds),
                frequency == null ? null : frequency.rawText(),
                frequency == null,
                lookahead != null,
                lookahead == null ? defaultLookaheadMinutes : clamp(lookahead.value(), minLookaheadMinutes, maxLookaheadMinutes),
                lookahead == null ? null : lookahead.rawText(),
                lookahead == null,
                defaultFrequencySeconds,
                minFrequencySeconds,
                maxFrequencySeconds,
                defaultLookaheadMinutes,
                minLookaheadMinutes,
                maxLookaheadMinutes,
                !journeyTimeFilters.isEmpty(),
                journeyTimeFilters,
                List.of());
    }

    private ScheduledAlertTemporalHints defaultHints() {
        return new ScheduledAlertTemporalHints(
                false,
                defaultFrequencySeconds,
                null,
                true,
                false,
                defaultLookaheadMinutes,
                null,
                true,
                defaultFrequencySeconds,
                minFrequencySeconds,
                maxFrequencySeconds,
                defaultLookaheadMinutes,
                minLookaheadMinutes,
                maxLookaheadMinutes,
                false,
                List.of(),
                List.of());
    }

    private TemporalMatch frequency(String normalized, String originalPrompt) {
        Matcher numbered = FREQUENCY_WITH_NUMBER.matcher(normalized);
        if (numbered.find()) {
            int number = number(numbered.group(1));
            String unit = numbered.group(2);
            return new TemporalMatch(seconds(number, unit), rawText(originalPrompt, numbered));
        }
        Matcher hourly = FREQUENCY_HOURLY.matcher(normalized);
        if (hourly.find()) {
            return new TemporalMatch(3600, rawText(originalPrompt, hourly));
        }
        Matcher daily = FREQUENCY_DAILY.matcher(normalized);
        if (daily.find()) {
            return new TemporalMatch(86400, rawText(originalPrompt, daily));
        }
        return null;
    }

    private TemporalMatch lookahead(String normalized, String originalPrompt) {
        Matcher matcher = LOOKAHEAD.matcher(normalized);
        if (matcher.find()) {
            int number = number(matcher.group(1));
            String unit = matcher.group(2);
            return new TemporalMatch(minutes(number, unit), rawText(originalPrompt, matcher));
        }
        matcher = LOOKAHEAD_TYPO_TOLERANT.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }
        int number = number(matcher.group(1));
        String unit = matcher.group(2);
        return new TemporalMatch(minutes(number, unit), rawText(originalPrompt, matcher));
    }

    private List<ScheduledAlertJourneyTimeFilter> journeyTimeFilters(String normalized, String originalPrompt) {
        List<ScheduledAlertJourneyTimeFilter> filters = new ArrayList<>();
        ScheduledAlertJourneyTimeFilter.Direction direction = direction(normalized);
        ScheduledAlertJourneyTimeFilter.TimetabledPreference timetabledPreference = timetabledPreference(normalized);
        ScheduledAlertJourneyTimeFilter.TargetRoleHint targetRoleHint = targetRoleHint(direction);

        Matcher between = JOURNEY_BETWEEN.matcher(normalized);
        while (between.find()) {
            filters.add(new ScheduledAlertJourneyTimeFilter(
                    rawText(originalPrompt, between),
                    ScheduledAlertJourneyTimeFilter.TimeRelation.BETWEEN,
                    normalizeTime(between.group(1)),
                    normalizeTime(between.group(2)),
                    null,
                    direction,
                    timetabledPreference,
                    targetRoleHint,
                    "Europe/Rome"));
        }
        if (!filters.isEmpty()) {
            return List.copyOf(filters);
        }

        Matcher after = JOURNEY_AFTER.matcher(normalized);
        while (after.find()) {
            String start = normalizeTime(after.group(1));
            filters.add(new ScheduledAlertJourneyTimeFilter(
                    rawText(originalPrompt, after),
                    ScheduledAlertJourneyTimeFilter.TimeRelation.AFTER,
                    start,
                    "23:59:59",
                    start,
                    direction,
                    timetabledPreference,
                    targetRoleHint,
                    "Europe/Rome"));
        }

        Matcher before = JOURNEY_BEFORE.matcher(normalized);
        while (before.find()) {
            String end = normalizeTime(before.group(1));
            filters.add(new ScheduledAlertJourneyTimeFilter(
                    rawText(originalPrompt, before),
                    ScheduledAlertJourneyTimeFilter.TimeRelation.BEFORE,
                    "00:00:00",
                    end,
                    end,
                    direction,
                    timetabledPreference,
                    targetRoleHint,
                    "Europe/Rome"));
        }
        return List.copyOf(filters);
    }

    private ScheduledAlertJourneyTimeFilter.Direction direction(String normalized) {
        if (Pattern.compile("\\b(departure|departing|leaves|starts|partenza|partenze|parte|partono)\\b").matcher(normalized).find()) {
            return ScheduledAlertJourneyTimeFilter.Direction.DEPARTURE;
        }
        if (Pattern.compile("\\b(arrival|arriving|arrives|arrivo|arrivi|arriva|arrivano)\\b").matcher(normalized).find()) {
            return ScheduledAlertJourneyTimeFilter.Direction.ARRIVAL;
        }
        if (Pattern.compile("\\b(passing|transit|passes|passa|passano|transita|transitano)\\b").matcher(normalized).find()) {
            return ScheduledAlertJourneyTimeFilter.Direction.PASSING;
        }
        return ScheduledAlertJourneyTimeFilter.Direction.UNKNOWN;
    }

    private ScheduledAlertJourneyTimeFilter.TimetabledPreference timetabledPreference(String normalized) {
        if (Pattern.compile("\\b(scheduled|planned|timetabled|programmed|programmata|programmato|orario\\s+previsto)\\b").matcher(normalized).find()) {
            return ScheduledAlertJourneyTimeFilter.TimetabledPreference.TIMETABLED;
        }
        if (Pattern.compile("\\b(actual|effective|current|reale|effettiva|effettivo)\\b").matcher(normalized).find()) {
            return ScheduledAlertJourneyTimeFilter.TimetabledPreference.ACTUAL_OR_EFFECTIVE;
        }
        return ScheduledAlertJourneyTimeFilter.TimetabledPreference.UNSPECIFIED;
    }

    private ScheduledAlertJourneyTimeFilter.TargetRoleHint targetRoleHint(
            ScheduledAlertJourneyTimeFilter.Direction direction) {
        return switch (direction) {
            case DEPARTURE -> ScheduledAlertJourneyTimeFilter.TargetRoleHint.ORIGIN_CALL;
            case ARRIVAL -> ScheduledAlertJourneyTimeFilter.TargetRoleHint.DESTINATION_CALL;
            case PASSING -> ScheduledAlertJourneyTimeFilter.TargetRoleHint.TRANSIT_CALL;
            case UNKNOWN -> ScheduledAlertJourneyTimeFilter.TargetRoleHint.UNKNOWN;
        };
    }

    private String normalizeTime(String value) {
        String[] parts = value.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return "%02d:%02d:00".formatted(hour, minute);
    }

    private int seconds(int number, String unit) {
        return minutes(number, unit) * 60;
    }

    private int minutes(int number, String unit) {
        String normalizedUnit = unit.toLowerCase(Locale.ROOT);
        if (normalizedUnit.startsWith("hour") || normalizedUnit.equals("h") || normalizedUnit.startsWith("or")) {
            return number * 60;
        }
        return number;
    }

    private int number(String value) {
        if (value == null) {
            return 0;
        }
        if (value.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(value);
        }
        return SMALL_NUMBERS.getOrDefault(value.toLowerCase(Locale.ROOT), 0);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private String rawText(String originalPrompt, Matcher matcher) {
        if (originalPrompt == null || matcher.start() < 0 || matcher.end() > originalPrompt.length()) {
            return matcher.group();
        }
        return originalPrompt.substring(matcher.start(), matcher.end());
    }

    private record TemporalMatch(int value, String rawText) {
    }
}
