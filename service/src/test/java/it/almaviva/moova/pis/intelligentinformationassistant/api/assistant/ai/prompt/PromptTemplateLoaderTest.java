package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemplateLoaderTest {

    private final PromptTemplateLoader loader = new PromptTemplateLoader();

    @Test
    void loadsExistingClasspathTemplate() {
        String template = loader.load("iia/prompts/test/simple.md");

        assertThat(template).contains("Hello {{NAME}}.");
    }

    @Test
    void renderReplacesPlaceholder() {
        String rendered = loader.render("iia/prompts/test/simple.md", Map.of("NAME", "Scheduled"));

        assertThat(rendered).contains("Hello Scheduled.");
    }

    @Test
    void missingPlaceholderRemainsVisible() {
        String rendered = loader.render("iia/prompts/test/simple.md", Map.of("NAME", "Scheduled"));

        assertThat(rendered).contains("{{MISSING}}");
    }
}
