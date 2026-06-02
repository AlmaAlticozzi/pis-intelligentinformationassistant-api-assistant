package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlatformNormalizer {

    private static final String PREFIX = "(?:BINARIO|PLATFORM|TRACK|QUAY|BANCHINA|MARCIAPIEDE|PLAT|PL|BIN)";
    private static final Pattern ACCENTS = Pattern.compile("\\p{M}+");
    private static final Pattern PUNCTUATION = Pattern.compile("[^A-Z0-9 ]+");
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final Pattern PREFIX_TOKEN = Pattern.compile("(?:^|\\s)" + PREFIX + "(?=\\s|\\d|$)");
    private static final Pattern PREFIX_THEN_PLATFORM = Pattern.compile(
            "(?:^|\\s)" + PREFIX + "\\s*(\\d+)\\s*([A-Z])?(?=\\s|$)");
    private static final Pattern PLATFORM_THEN_PREFIX = Pattern.compile(
            "(?:^|\\s)(\\d+)\\s*" + PREFIX + "(?=\\s|$)");
    private static final Pattern BARE_PLATFORM = Pattern.compile("^(\\d+)\\s*([A-Z])?$");

    public NormalizedPlatform normalize(String value) {
        String normalizedText = normalizeText(value);
        if (normalizedText.isBlank()) {
            return new NormalizedPlatform(value, normalizedText, null, null, false, true);
        }

        Optional<PlatformParts> platformParts = findPlatformParts(normalizedText);
        if (platformParts.isPresent()) {
            PlatformParts parts = platformParts.get();
            return new NormalizedPlatform(
                    value,
                    canonicalText(parts),
                    parts.number(),
                    parts.suffix(),
                    false,
                    false);
        }

        boolean malformed = PREFIX_TOKEN.matcher(normalizedText).find();
        return new NormalizedPlatform(value, normalizedText, null, null, malformed, true);
    }

    public boolean samePlatform(String expected, String actual) {
        NormalizedPlatform normalizedExpected = normalize(expected);
        NormalizedPlatform normalizedActual = normalize(actual);
        return normalizedExpected.hasNumber()
                && normalizedActual.hasNumber()
                && normalizedExpected.number().equals(normalizedActual.number())
                && java.util.Objects.equals(normalizedExpected.suffix(), normalizedActual.suffix());
    }

    public boolean inPlatforms(String actual, Collection<String> expectedValues) {
        if (expectedValues == null) {
            return false;
        }
        return expectedValues.stream().anyMatch(expected -> samePlatform(expected, actual));
    }

    public boolean notInPlatforms(String actual, Collection<String> expectedValues) {
        return !inPlatforms(actual, expectedValues);
    }

    public Optional<Integer> extractPlatformNumber(String value) {
        return Optional.ofNullable(normalize(value).number());
    }

    public boolean hasLetterSuffix(String value) {
        return normalize(value).hasLetterSuffix();
    }

    public boolean isEven(String value) {
        return extractPlatformNumber(value).filter(number -> number % 2 == 0).isPresent();
    }

    public boolean isOdd(String value) {
        return extractPlatformNumber(value).filter(number -> number % 2 != 0).isPresent();
    }

    public boolean isDoubleDigit(String value) {
        return extractPlatformNumber(value).filter(number -> number >= 10 && number <= 99).isPresent();
    }

    public boolean isMultipleOf(String value, int divisor) {
        return divisor > 0 && extractPlatformNumber(value).filter(number -> number % divisor == 0).isPresent();
    }

    public boolean isBetween(String value, int min, int max) {
        return min <= max && extractPlatformNumber(value)
                .filter(number -> number >= min && number <= max)
                .isPresent();
    }

    private Optional<PlatformParts> findPlatformParts(String normalizedText) {
        Matcher prefixedMatcher = PREFIX_THEN_PLATFORM.matcher(normalizedText);
        if (prefixedMatcher.find()) {
            return toPlatformParts(prefixedMatcher);
        }

        Matcher suffixPrefixMatcher = PLATFORM_THEN_PREFIX.matcher(normalizedText);
        if (suffixPrefixMatcher.find()) {
            return toPlatformParts(suffixPrefixMatcher.group(1), null);
        }

        Matcher bareMatcher = BARE_PLATFORM.matcher(normalizedText);
        if (bareMatcher.matches()) {
            return toPlatformParts(bareMatcher);
        }

        return Optional.empty();
    }

    private Optional<PlatformParts> toPlatformParts(Matcher matcher) {
        return toPlatformParts(matcher.group(1), matcher.group(2));
    }

    private Optional<PlatformParts> toPlatformParts(String number, String suffix) {
        try {
            return Optional.of(new PlatformParts(Integer.valueOf(number), suffix));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private String canonicalText(PlatformParts parts) {
        return parts.number() + (parts.suffix() == null ? "" : parts.suffix());
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = ACCENTS.matcher(normalized).replaceAll("");
        normalized = normalized.toUpperCase(Locale.ROOT).trim();
        normalized = PUNCTUATION.matcher(normalized).replaceAll(" ");
        return SPACES.matcher(normalized).replaceAll(" ").trim();
    }

    private record PlatformParts(Integer number, String suffix) {
    }
}
