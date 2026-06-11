package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class PromptTemplateLoader {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([A-Z0-9_]+)}}");

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String load(String classpathLocation) {
        return cache.computeIfAbsent(classpathLocation, this::loadFromClasspath);
    }

    public String render(String classpathLocation, Map<String, String> variables) {
        String rendered = load(classpathLocation);
        Map<String, String> safeVariables = variables == null ? Map.of() : variables;
        for (Map.Entry<String, String> entry : safeVariables.entrySet()) {
            String value = entry.getValue();
            if (value == null) {
                System.out.println("[IIA][PROMPT_TEMPLATE][WARN] null placeholder path="
                        + classpathLocation + " name=" + entry.getKey());
                value = "";
            }
            rendered = rendered.replace("{{" + entry.getKey() + "}}", value);
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(rendered);
        while (matcher.find()) {
            System.out.println("[IIA][PROMPT_TEMPLATE][WARN] missing placeholder path="
                    + classpathLocation + " name=" + matcher.group(1));
        }
        System.out.println("[IIA][PROMPT_TEMPLATE] render path=" + classpathLocation
                + " renderedLength=" + rendered.length());
        return rendered;
    }

    private String loadFromClasspath(String classpathLocation) {
        String normalizedLocation = normalize(classpathLocation);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = PromptTemplateLoader.class.getClassLoader();
        }
        try (InputStream inputStream = classLoader.getResourceAsStream(normalizedLocation)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Prompt template not found on classpath: " + classpathLocation);
            }
            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("[IIA][PROMPT_TEMPLATE] loaded path=" + normalizedLocation
                    + " length=" + template.length());
            return template;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load prompt template from classpath: " + classpathLocation, ex);
        }
    }

    private String normalize(String classpathLocation) {
        if (classpathLocation == null || classpathLocation.isBlank()) {
            throw new IllegalArgumentException("classpathLocation must not be blank");
        }
        return classpathLocation.startsWith("/") ? classpathLocation.substring(1) : classpathLocation;
    }
}
