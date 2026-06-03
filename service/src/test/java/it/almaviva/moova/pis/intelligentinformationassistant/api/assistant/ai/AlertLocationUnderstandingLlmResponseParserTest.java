package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertLocationUnderstandingLlmResponseParserTest {

    private final AlertLocationUnderstandingLlmResponseParser parser =
            new AlertLocationUnderstandingLlmResponseParser();

    @Test
    void parsesValidJsonWithTwoLocations() {
        AlertLocationUnderstandingResult result = parser.parse("""
                {
                  "hasLocations": true,
                  "language": "it",
                  "mainEvent": {"eventIntent": "DEPARTURE", "confidence": 0.92},
                  "locations": [
                    {
                      "rawText": "Garibaldi",
                      "normalizedText": "Garibaldi",
                      "role": "MAIN_EVENT_LOCATION",
                      "relationToMainEvent": "EVENT_STOP_POINT",
                      "requiredCoverage": true,
                      "polarity": "INCLUDE",
                      "logicalGroup": "G1",
                      "confidence": 0.94
                    },
                    {
                      "rawText": "Venezia",
                      "normalizedText": "Venezia",
                      "role": "ROUTE_OR_NEXT_CALL_LOCATION",
                      "relationToMainEvent": "FUTURE_ROUTE_CONSTRAINT",
                      "requiredCoverage": true,
                      "polarity": "EXCLUDE",
                      "logicalGroup": "G2",
                      "confidence": 0.91
                    }
                  ],
                  "nonLocationConstraints": [{"type": "PLATFORM", "rawText": "binario 1"}],
                  "warnings": []
                }
                """);

        assertThat(result.hasLocations()).isTrue();
        assertThat(result.locations()).hasSize(2);
        assertThat(result.mainEvent().eventIntent()).isEqualTo(AlertLocationMainEventIntent.DEPARTURE);
        assertThat(result.mainEvent().confidence()).isEqualTo(0.92);
        assertThat(result.locations().getFirst().role()).isEqualTo(AlertLocationRole.MAIN_EVENT_LOCATION);
        assertThat(result.locations().getFirst().polarity()).isEqualTo(AlertLocationPolarity.INCLUDE);
        assertThat(result.locations().getFirst().confidence()).isEqualTo(0.94);
        assertThat(result.locations().get(1).role()).isEqualTo(AlertLocationRole.ROUTE_OR_NEXT_CALL_LOCATION);
        assertThat(result.locations().get(1).polarity()).isEqualTo(AlertLocationPolarity.EXCLUDE);
        assertThat(result.nonLocationConstraints()).hasSize(1);
        assertThat(result.nonLocationConstraints().getFirst().type())
                .isEqualTo(AlertLocationNonLocationConstraintType.PLATFORM);
    }

    @Test
    void parsesNoLocationsJson() {
        AlertLocationUnderstandingResult result = parser.parse("""
                {
                  "hasLocations": false,
                  "language": "en",
                  "mainEvent": {"eventIntent": "UNKNOWN", "confidence": 0.1},
                  "locations": [],
                  "nonLocationConstraints": [],
                  "warnings": []
                }
                """);

        assertThat(result.hasLocations()).isFalse();
        assertThat(result.locations()).isEmpty();
    }

    @Test
    void mapsUnknownRoleToGenericWithWarning() {
        AlertLocationUnderstandingResult result = parser.parse("""
                {
                  "hasLocations": true,
                  "locations": [
                    {"rawText": "Foo", "role": "MAGIC_PLACE", "confidence": 0.7}
                  ]
                }
                """);

        assertThat(result.locations()).hasSize(1);
        assertThat(result.locations().getFirst().role()).isEqualTo(AlertLocationRole.GENERIC_LOCATION);
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("MAGIC_PLACE")
                && warning.contains("GENERIC_LOCATION"));
    }

    @Test
    void mapsUnknownPolarityToIncludeWithWarning() {
        AlertLocationUnderstandingResult result = parser.parse("""
                {
                  "hasLocations": true,
                  "locations": [
                    {"rawText": "Foo", "polarity": "MAYBE", "confidence": 0.7}
                  ]
                }
                """);

        assertThat(result.locations().getFirst().polarity()).isEqualTo(AlertLocationPolarity.INCLUDE);
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("MAYBE")
                && warning.contains("INCLUDE"));
    }

    @Test
    void clampsConfidenceWithWarning() {
        AlertLocationUnderstandingResult result = parser.parse("""
                {
                  "hasLocations": true,
                  "mainEvent": {"eventIntent": "ARRIVAL", "confidence": 1.5},
                  "locations": [
                    {"rawText": "Foo", "confidence": -0.2},
                    {"rawText": "Bar", "confidence": 1.2}
                  ]
                }
                """);

        assertThat(result.mainEvent().confidence()).isEqualTo(1.0);
        assertThat(result.locations().getFirst().confidence()).isEqualTo(0.0);
        assertThat(result.locations().get(1).confidence()).isEqualTo(1.0);
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("clamped"));
    }

    @Test
    void malformedJsonReturnsSafeResult() {
        AlertLocationUnderstandingLlmResponseParser.ParseResult parseResult =
                parser.parseDetailed("{\"hasLocations\": true,");

        assertThat(parseResult.parsed()).isFalse();
        assertThat(parseResult.result().hasLocations()).isFalse();
        assertThat(parseResult.result().locations()).isEmpty();
        assertThat(parseResult.warnings()).isNotEmpty();
    }

    @Test
    void stripsMarkdownCodeFence() {
        AlertLocationUnderstandingResult result = parser.parse("""
                ```json
                {
                  "hasLocations": true,
                  "locations": [
                    {"rawText": "Garibaldi", "role": "MAIN_EVENT_LOCATION", "confidence": 0.8}
                  ]
                }
                ```
                """);

        assertThat(result.hasLocations()).isTrue();
        assertThat(result.locations()).hasSize(1);
        assertThat(result.locations().getFirst().rawText()).isEqualTo("Garibaldi");
    }
}
