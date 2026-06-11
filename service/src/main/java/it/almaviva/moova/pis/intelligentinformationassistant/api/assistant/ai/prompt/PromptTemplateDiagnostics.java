package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record PromptTemplateDiagnostics(
        String templatePath,
        int templateLength,
        int renderedLength,
        Set<String> declaredPlaceholders,
        Set<String> providedVariables,
        Set<String> unresolvedPlaceholdersAfterRender,
        Set<String> unusedVariables,
        String templateHash,
        String renderedHash,
        String renderedText) {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([A-Z0-9_]+)}}");

    public static Set<String> extractPlaceholders(String text) {
        Set<String> placeholders = new LinkedHashSet<>();
        if (text == null || text.isEmpty()) {
            return placeholders;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }

    public static String shortSha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < hash.length && hex.length() < 12; i++) {
                hex.append("%02x".formatted(hash[i]));
            }
            return hex.substring(0, 12);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    static PromptTemplateDiagnostics fromRender(
            String templatePath,
            String template,
            Map<String, String> providedVariables,
            String rendered) {
        Set<String> declared = extractPlaceholders(template);
        Set<String> provided = new LinkedHashSet<>(providedVariables == null ? Set.of() : providedVariables.keySet());
        Set<String> unresolved = extractPlaceholders(rendered);
        Set<String> unused = new LinkedHashSet<>(provided);
        unused.removeAll(declared);
        return new PromptTemplateDiagnostics(
                templatePath,
                template == null ? 0 : template.length(),
                rendered == null ? 0 : rendered.length(),
                Collections.unmodifiableSet(new LinkedHashSet<>(declared)),
                Collections.unmodifiableSet(new LinkedHashSet<>(provided)),
                Collections.unmodifiableSet(new LinkedHashSet<>(unresolved)),
                Collections.unmodifiableSet(new LinkedHashSet<>(unused)),
                shortSha256(template),
                shortSha256(rendered),
                rendered);
    }

    public String declaredPlaceholdersLogValue() {
        return logValue(declaredPlaceholders);
    }

    public String providedVariablesLogValue() {
        return logValue(providedVariables);
    }

    public String unresolvedPlaceholdersAfterRenderLogValue() {
        return logValue(unresolvedPlaceholdersAfterRender);
    }

    public String unusedVariablesLogValue() {
        return logValue(unusedVariables);
    }

    public static String logValue(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        return List.copyOf(values).toString();
    }
}
