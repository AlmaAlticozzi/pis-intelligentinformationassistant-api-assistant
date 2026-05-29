package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class PointNormalizer {

    private static final Pattern ACCENTS = Pattern.compile("\\p{M}+");
    private static final Pattern PUNCTUATION = Pattern.compile("[^A-Z0-9 ]+");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    public String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = ACCENTS.matcher(normalized).replaceAll("");
        normalized = normalized.toUpperCase(Locale.ROOT).trim();
        normalized = normalizeAbbreviations(normalized);
        normalized = PUNCTUATION.matcher(normalized).replaceAll(" ");
        normalized = normalizeAbbreviations(normalized);
        return SPACES.matcher(normalized).replaceAll(" ").trim();
    }

    public List<String> tokens(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return List.of();
        }
        return Arrays.stream(normalized.split(" "))
                .filter(token -> !token.isBlank())
                .toList();
    }

    private String normalizeAbbreviations(String value) {
        String normalized = value;
        normalized = normalized.replaceAll("\\bTERMINAL\\s*1\\b", "T1");
        normalized = normalized.replaceAll("\\bTERMINAL\\s*2\\b", "T2");
        normalized = normalized.replaceAll("\\bT\\s*\\.?\\s*1\\b", "T1");
        normalized = normalized.replaceAll("\\bT\\s*\\.?\\s*2\\b", "T2");
        normalized = normalized.replaceAll("\\bP\\s*\\.?\\s*TA\\b", "PORTA");
        normalized = normalized.replaceAll("\\bSANTO\\b", "SAN");
        normalized = normalized.replaceAll("\\bS\\s*\\.", "SAN ");
        normalized = normalized.replaceAll("\\bS\\b", "SAN");
        return normalized;
    }
}
