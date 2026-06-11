package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void extractPlaceholdersFindsUniqueNamesInEncounterOrder() {
        assertThat(PromptTemplateDiagnostics.extractPlaceholders("{{A}} {{B}} {{A}} {{C_1}}"))
                .containsExactly("A", "B", "C_1");
    }

    @Test
    void renderWithDiagnosticsReportsDeclaredUnresolvedAndUnusedVariables() {
        PromptTemplateDiagnostics diagnostics = loader.renderWithDiagnostics(
                "iia/prompts/test/simple.md",
                Map.of("NAME", "Scheduled", "UNUSED", "x"));

        assertThat(diagnostics.declaredPlaceholders()).containsExactly("NAME", "MISSING");
        assertThat(diagnostics.providedVariables()).containsExactlyInAnyOrder("NAME", "UNUSED");
        assertThat(diagnostics.unresolvedPlaceholdersAfterRender()).containsExactly("MISSING");
        assertThat(diagnostics.unusedVariables()).containsExactly("UNUSED");
        assertThat(diagnostics.renderedText()).contains("Hello Scheduled.");
    }

    @Test
    void shortSha256IsStableForSameInput() {
        String first = PromptTemplateDiagnostics.shortSha256("same input");
        String second = PromptTemplateDiagnostics.shortSha256("same input");

        assertThat(first)
                .isEqualTo(second)
                .hasSize(12);
    }

    @Test
    void shortSha256ChangesWhenInputChanges() {
        assertThat(PromptTemplateDiagnostics.shortSha256("input one"))
                .isNotEqualTo(PromptTemplateDiagnostics.shortSha256("input two"));
    }

    @Test
    void renderFailsOnUnresolvedPlaceholderWhenConfigured() {
        PromptTemplateLoader strictLoader = new PromptTemplateLoader();
        strictLoader.failOnUnresolvedPlaceholders = true;

        assertThatThrownBy(() -> strictLoader.render(
                "iia/prompts/test/simple.md",
                Map.of("NAME", "Scheduled")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("iia/prompts/test/simple.md")
                .hasMessageContaining("MISSING");
    }

    @Test
    void renderDoesNotFailOnUnresolvedPlaceholderByDefault() {
        PromptTemplateLoader permissiveLoader = new PromptTemplateLoader();
        permissiveLoader.failOnUnresolvedPlaceholders = false;

        PromptTemplateDiagnostics diagnostics = permissiveLoader.renderWithDiagnostics(
                "iia/prompts/test/simple.md",
                Map.of("NAME", "Scheduled"));

        assertThat(diagnostics.unresolvedPlaceholdersAfterRender()).containsExactly("MISSING");
        assertThat(diagnostics.renderedText()).contains("{{MISSING}}");
    }
}
