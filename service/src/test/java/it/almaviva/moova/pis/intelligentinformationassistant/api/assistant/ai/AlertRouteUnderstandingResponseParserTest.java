package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertRouteUnderstandingResponseParserTest {

    private final AlertRouteUnderstandingResponseParser parser = new AlertRouteUnderstandingResponseParser();

    @Test
    void parsesJsonAndNormalizesEnumStrings() {
        AlertRouteUnderstandingResult result = parser.parse("""
                {
                  "decision":"routed",
                  "dataDomains":["service_data"],
                  "primaryDataDomain":"service_data",
                  "interpreterType":"event_interpreter",
                  "accessMode":"kafka_event",
                  "intentKind":"event_occurrence",
                  "outputMode":"on_match",
                  "requiresPolling":false,
                  "requiresServiceDataApi":false,
                  "requiresKafkaEvent":true,
                  "hasAggregation":false,
                  "hasCardinalityThreshold":false,
                  "hasReportIntent":false,
                  "confidence":0.88,
                  "summary":"Event route.",
                  "rejectedReason":null,
                  "warnings":[]
                }
                """).orElseThrow();

        assertThat(result.decision()).isEqualTo(AlertRouteDecision.ROUTED);
        assertThat(result.dataDomains()).containsExactly("SERVICE_DATA");
        assertThat(result.primaryDataDomain()).isEqualTo("SERVICE_DATA");
        assertThat(result.interpreterType()).isEqualTo(AlertRouteInterpreterType.EVENT_INTERPRETER);
        assertThat(result.accessMode()).isEqualTo(AlertRouteAccessMode.KAFKA_EVENT);
        assertThat(result.intentKind()).isEqualTo(AlertRouteIntentKind.EVENT_OCCURRENCE);
        assertThat(result.outputMode()).isEqualTo(AlertRouteOutputMode.ON_MATCH);
    }

    @Test
    void rejectsEmptyNonJsonAndMissingDecisionAsControlledParseFailures() {
        assertThat(parser.parseDetailed(" ").result()).isEmpty();
        assertThat(parser.parseDetailed("not-json").result()).isEmpty();
        AlertRouteUnderstandingResponseParser.ParseResult missingDecision = parser.parseDetailed("{}");

        assertThat(missingDecision.result()).isEmpty();
        assertThat(missingDecision.failureReason()).contains("decision");
    }
}
