package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertLocationUnderstandingResponseParserTest {

    private final ScheduledAlertLocationUnderstandingResponseParser parser =
            new ScheduledAlertLocationUnderstandingResponseParser();

    @Test
    void parsesValidMonitoredLocation() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse(json(location(
                "Garibaldi FS",
                "MONITORED_STOP_POINT",
                "SERVICE_DATA_API_QUERY_STOP_POINT",
                true)));

        assertThat(result.hasLocations()).isTrue();
        assertThat(result.monitoringScope()).isEqualTo(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS);
        assertThat(result.locations()).hasSize(1);
        assertThat(result.locations().getFirst().rawText()).isEqualTo("Garibaldi FS");
        assertThat(result.locations().getFirst().role()).isEqualTo(ScheduledAlertLocationRole.MONITORED_STOP_POINT);
    }

    @Test
    void parsesMonitoredAndOriginFilter() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse(json(
                location("Pero", "MONITORED_STOP_POINT", "SERVICE_DATA_API_QUERY_STOP_POINT", true),
                location("Garibaldi FS", "FILTER_ORIGIN_STOP_POINT", "ORIGIN_FILTER", false)));

        assertThat(result.locations()).extracting(ScheduledAlertLocationMention::rawText)
                .containsExactly("Pero", "Garibaldi FS");
        assertThat(result.locations().getFirst().role()).isEqualTo(ScheduledAlertLocationRole.MONITORED_STOP_POINT);
        assertThat(result.locations().get(1).role()).isEqualTo(ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT);
    }

    @Test
    void parsesMonitoredAndTimetabledOriginFilter() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse(json(
                location("Pero", "MONITORED_STOP_POINT", "SERVICE_DATA_API_QUERY_STOP_POINT", true),
                location("Garibaldi FS", "FILTER_TIMETABLED_ORIGIN_STOP_POINT", "TIMETABLED_ORIGIN_FILTER", false)));

        assertThat(result.locations().get(1).role())
                .isEqualTo(ScheduledAlertLocationRole.FILTER_TIMETABLED_ORIGIN_STOP_POINT);
        assertThat(result.locations().get(1).relationToSnapshot())
                .isEqualTo(ScheduledAlertLocationRelation.TIMETABLED_ORIGIN_FILTER);
    }

    @Test
    void parsesMonitoredAndDestinationFilter() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse(json(
                location("Domodossola", "MONITORED_STOP_POINT", "SERVICE_DATA_API_QUERY_STOP_POINT", true),
                location("Porta Romana", "FILTER_DESTINATION_STOP_POINT", "DESTINATION_FILTER", false)));

        assertThat(result.locations().getFirst().rawText()).isEqualTo("Domodossola");
        assertThat(result.locations().get(1).role()).isEqualTo(ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT);
    }

    @Test
    void parsesRouteStopFilter() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse(json(
                location("Bignami", "MONITORED_STOP_POINT", "SERVICE_DATA_API_QUERY_STOP_POINT", true),
                location("Monumentale", "FILTER_ROUTE_STOP_POINT", "ROUTE_FILTER", false)));

        assertThat(result.locations().get(1).role()).isEqualTo(ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT);
        assertThat(result.locations().get(1).relationToSnapshot()).isEqualTo(ScheduledAlertLocationRelation.ROUTE_FILTER);
    }

    @Test
    void parsesCancelledStopFilter() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse(json(
                location("Milano Bruzzano Parco", "MONITORED_STOP_POINT", "SERVICE_DATA_API_QUERY_STOP_POINT", true),
                location("Palazzolo Milanese", "FILTER_CANCELLED_CALL_STOP_POINT", "CANCELLED_CALL_FILTER", false)));

        assertThat(result.locations().get(1).role())
                .isEqualTo(ScheduledAlertLocationRole.FILTER_CANCELLED_CALL_STOP_POINT);
    }

    @Test
    void parsesMultiMonitoredLocations() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse(json(
                location("Varedo", "MONITORED_STOP_POINT", "SERVICE_DATA_API_QUERY_STOP_POINT", true),
                location("Palazzolo Milanese", "MONITORED_STOP_POINT", "SERVICE_DATA_API_QUERY_STOP_POINT", true)));

        assertThat(result.locations()).hasSize(2);
        assertThat(result.locations()).allMatch(location ->
                location.role() == ScheduledAlertLocationRole.MONITORED_STOP_POINT
                        && location.requiredForApiQuery());
    }

    @Test
    void platformIsParsedAsNonLocationConstraintNotLocation() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse("""
                {
                  "hasLocations": true,
                  "language": "it",
                  "monitoringScope": "EXPLICIT_STOP_POINTS",
                  "locations": [
                    {
                      "rawText": "Gorla",
                      "normalizedText": "Gorla",
                      "role": "MONITORED_STOP_POINT",
                      "relationToSnapshot": "SERVICE_DATA_API_QUERY_STOP_POINT",
                      "requiredForApiQuery": true,
                      "requiredCoverage": true,
                      "polarity": "INCLUDE",
                      "logicalGroup": "G1",
                      "confidence": 0.95
                    }
                  ],
                  "nonLocationConstraints": [
                    {"type": "PLATFORM", "rawText": "binario 3"}
                  ],
                  "warnings": []
                }
                """);

        assertThat(result.locations()).extracting(ScheduledAlertLocationMention::rawText)
                .containsExactly("Gorla")
                .doesNotContain("3", "binario 3");
        assertThat(result.nonLocationConstraints()).hasSize(1);
        assertThat(result.nonLocationConstraints().getFirst().type())
                .isEqualTo(ScheduledAlertNonLocationConstraintType.PLATFORM);
        assertThat(result.nonLocationConstraints().getFirst().rawText()).isEqualTo("binario 3");
    }

    @Test
    void allLocationsDoesNotRequireExplicitMonitoredLocation() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse("""
                {
                  "hasLocations": false,
                  "language": "it",
                  "monitoringScope": "ALL_KNOWN_STOP_POINTS",
                  "locations": [],
                  "nonLocationConstraints": [],
                  "warnings": []
                }
                """);

        assertThat(result.hasLocations()).isFalse();
        assertThat(result.locations()).isEmpty();
        assertThat(result.monitoringScope()).isEqualTo(ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS);
    }

    @Test
    void rejectsEmptyNonJsonAndExplicitScopeWithoutLocations() {
        assertThat(parser.parseDetailed("").parsed()).isFalse();
        assertThat(parser.parseDetailed("```json\n{}\n```").parsed()).isFalse();

        ScheduledAlertLocationUnderstandingResponseParser.ParseResult explicitWithoutLocations = parser.parseDetailed("""
                {
                  "hasLocations": false,
                  "monitoringScope": "EXPLICIT_STOP_POINTS",
                  "locations": [],
                  "nonLocationConstraints": [],
                  "warnings": []
                }
                """);

        assertThat(explicitWithoutLocations.parsed()).isFalse();
        assertThat(explicitWithoutLocations.warnings())
                .anyMatch(warning -> warning.contains("EXPLICIT_STOP_POINTS"));
    }

    @Test
    void normalizesEnumStringsUppercaseAndDefaultsArrays() {
        ScheduledAlertLocationUnderstandingResult result = parser.parse("""
                {
                  "hasLocations": true,
                  "monitoringScope": "explicit_stop_points",
                  "locations": [
                    {
                      "rawText": "Gorla",
                      "role": "monitored_stop_point",
                      "relationToSnapshot": "service_data_api_query_stop_point",
                      "polarity": "include",
                      "requiredForApiQuery": true,
                      "requiredCoverage": true,
                      "confidence": 0.9
                    }
                  ]
                }
                """);

        assertThat(result.monitoringScope()).isEqualTo(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS);
        assertThat(result.locations().getFirst().role()).isEqualTo(ScheduledAlertLocationRole.MONITORED_STOP_POINT);
        assertThat(result.nonLocationConstraints()).isEmpty();
        assertThat(result.warnings()).isEmpty();
    }

    private String json(String... locationItems) {
        return """
                {
                  "hasLocations": true,
                  "language": "it",
                  "monitoringScope": "EXPLICIT_STOP_POINTS",
                  "locations": [
                    %s
                  ],
                  "nonLocationConstraints": [],
                  "warnings": []
                }
                """.formatted(String.join(",\n", locationItems));
    }

    private String location(String rawText, String role, String relation, boolean requiredForApiQuery) {
        return """
                {
                  "rawText": "%s",
                  "normalizedText": "%s",
                  "role": "%s",
                  "relationToSnapshot": "%s",
                  "requiredForApiQuery": %s,
                  "requiredCoverage": true,
                  "polarity": "INCLUDE",
                  "logicalGroup": "G1",
                  "confidence": 0.95
                }
                """.formatted(rawText, rawText, role, relation, requiredForApiQuery);
    }
}
