package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.text.Normalizer;
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
        if (!matcher.find()) {
            return null;
        }
        int number = number(matcher.group(1));
        String unit = matcher.group(2);
        return new TemporalMatch(minutes(number, unit), rawText(originalPrompt, matcher));
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
