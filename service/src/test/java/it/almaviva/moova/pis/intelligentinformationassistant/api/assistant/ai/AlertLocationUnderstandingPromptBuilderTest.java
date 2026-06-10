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

    @Test
    void promptContainsGeneralCurrentEventOriginDestinationRules() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.userPrompt())
                .contains("A preposition such as Italian \"da\" or English \"from\" is not enough to classify a location as journey origin")
                .contains("normally means X is MAIN_EVENT_LOCATION, not ORIGIN_LOCATION")
                .contains("ORIGIN_LOCATION requires explicit journey-origin wording")
                .contains("normally means X is MAIN_EVENT_LOCATION, not DESTINATION_LOCATION")
                .contains("DESTINATION_LOCATION requires explicit journey-destination wording")
                .contains("ROUTE_OR_NEXT_CALL_LOCATION is for future route/call wording")
                .contains("TRANSIT_LOCATION is only for explicit transit wording")
                .contains("Use CANCELLED_CALL_LOCATION only when the wording explicitly says the stop/call itself is cancelled");
    }

    @Test
    void promptDistinguishesCancelledJourneyFromCancelledStopCallLocation() {
        LlmRequest request = builder.build("Notify me when a train is cancelled at Lecco", "ALRT1");

        assertThat(request.userPrompt())
                .contains("Cancellation/suppression of a journey at a location means the named location is the monitored/current event location")
                .contains("Cancellation/suppression of a stop/call inside the journey means the named location is a cancelled/skipped call constraint")
                .contains("Do not classify \"cancelled journey/train/service at X\"")
                .contains("a train is cancelled at X")
                .contains("X: role MAIN_EVENT_LOCATION, relationToMainEvent EVENT_STOP_POINT")
                .contains("a journey has stop X cancelled/skipped")
                .contains("X: role CANCELLED_CALL_LOCATION, relationToMainEvent CANCELLED_CALL_CONSTRAINT");
    }

    @Test
    void promptContainsGenericExamplesWithoutStationNames() {
        LlmRequest request = builder.build("prompt", "ALRT1");

        assertThat(request.userPrompt())
                .contains("una corsa e in partenza da X e passera da Y")
                .contains("X: role MAIN_EVENT_LOCATION, relationToMainEvent EVENT_LOCATION")
                .contains("Y: role ROUTE_OR_NEXT_CALL_LOCATION, relationToMainEvent FUTURE_ROUTE_CONSTRAINT")
                .contains("una corsa ha origine a X e destinazione Z")
                .contains("Do not invent a MAIN_EVENT_LOCATION when no current event location is present");
    }
}
