package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertLocationUnderstandingPromptBuilderTest {

    private final AlertLocationUnderstandingPromptBuilder builder = new AlertLocationUnderstandingPromptBuilder();

    @Test
    void buildsLocationUnderstandingUseCaseRequest() {
        LlmRequest request = builder.build("Dimmi quando parte da Garibaldi", "ALRT1");

        assertThat(request.useCase()).isEqualTo(AiUseCase.ALERT_LOCATION_UNDERSTANDING);
        assertThat(request.correlationId()).isEqualTo("ALRT1");
    }

    @Test
    void promptStatesItMustNotGenerateTechnicalSpecification() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.systemPrompt())
                .contains("Do not generate technicalSpecification");
        assertThat(request.userPrompt())
                .contains("Analyze this Alert prompt semantically");
    }

    @Test
    void promptDistinguishesLocationsFromPlatforms() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.userPrompt())
                .contains("Platform/binario/track/quay/banchina/marciapiede constraints are not locations")
                .contains("nonLocationConstraints");
    }

    @Test
    void promptContainsMinimumRoles() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.userPrompt())
                .contains("MAIN_EVENT_LOCATION")
                .contains("ORIGIN_LOCATION")
                .contains("DESTINATION_LOCATION")
                .contains("ROUTE_OR_NEXT_CALL_LOCATION")
                .contains("TRANSIT_LOCATION")
                .contains("CANCELLED_CALL_LOCATION")
                .contains("REPLACEMENT_LOCATION")
                .contains("GENERIC_LOCATION");
    }

    @Test
    void promptRequiresConfidenceJsonAndNoInventedLocations() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.systemPrompt())
                .contains("Return only valid raw JSON");
        assertThat(request.userPrompt())
                .contains("Do not invent locations")
                .contains("Provide confidence for mainEvent and for every location")
                .contains("\"confidence\"");
    }

    @Test
    void promptRequiresCrossLanguageUnderstanding() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.userPrompt())
                .contains("Italian, English, or another language")
                .contains("cross-language and multilingual")
                .contains("do not rely on a closed list of verbs");
    }

    @Test
    void promptStatesExcludedLocationsRemainRequiredCoverage() {
        LlmRequest request = builder.build("La corsa non deve avere come destinazione Bologna", "ALRT1");

        assertThat(request.userPrompt())
                .contains("Negated or excluded locations are still mandatory user constraints")
                .contains("requiredCoverage=true and polarity=EXCLUDE")
                .contains("Do not set requiredCoverage=false just because polarity=EXCLUDE");
    }
}
