package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlertRouteUnderstandingValidatorTest {

    private final AlertRouteUnderstandingValidator validator = new AlertRouteUnderstandingValidator();

    @Test
    void routesEventPromptToEventInterpreterServiceDataKafkaEvent() {
        AlertRouteUnderstandingResult validated = validator.validate(eventRoute(),
                "Avvisami quando una corsa parte da Garibaldi");

        assertThat(validated.decision()).isEqualTo(AlertRouteDecision.ROUTED);
        assertThat(validated.interpreterType()).isEqualTo(AlertRouteInterpreterType.EVENT_INTERPRETER);
        assertThat(validated.dataDomains()).containsExactly("SERVICE_DATA");
        assertThat(validated.accessMode()).isEqualTo(AlertRouteAccessMode.KAFKA_EVENT);
    }

    @Test
    void routesScheduledReportToSnapshotEveryRunReport() {
        AlertRouteUnderstandingResult validated = validator.validate(scheduledRoute(
                        AlertRouteIntentKind.SNAPSHOT_REPORT,
                        AlertRouteOutputMode.EVERY_RUN_REPORT,
                        true,
                        false,
                        true),
                "Ogni 10 minuti dimmi quante corse in ritardo ci sono a Garibaldi");

        assertThat(validated.decision()).isEqualTo(AlertRouteDecision.ROUTED);
        assertThat(validated.interpreterType()).isEqualTo(AlertRouteInterpreterType.SCHEDULED_INTERPRETER);
        assertThat(validated.dataDomains()).containsExactly("SERVICE_DATA");
        assertThat(validated.accessMode()).isEqualTo(AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT);
        assertThat(validated.outputMode()).isEqualTo(AlertRouteOutputMode.EVERY_RUN_REPORT);
    }

    @Test
    void routesScheduledConditionWithAggregationAndCardinalityThreshold() {
        AlertRouteUnderstandingResult validated = validator.validate(scheduledRoute(
                        AlertRouteIntentKind.SNAPSHOT_CONDITION,
                        AlertRouteOutputMode.ON_MATCH,
                        true,
                        true,
                        false),
                "Fammi sapere quando ci sono almeno tre bus in ritardo in partenza a Seveso");

        assertThat(validated.decision()).isEqualTo(AlertRouteDecision.ROUTED);
        assertThat(validated.interpreterType()).isEqualTo(AlertRouteInterpreterType.SCHEDULED_INTERPRETER);
        assertThat(validated.intentKind()).isEqualTo(AlertRouteIntentKind.SNAPSHOT_CONDITION);
        assertThat(validated.outputMode()).isEqualTo(AlertRouteOutputMode.ON_MATCH);
        assertThat(validated.hasAggregation()).isTrue();
        assertThat(validated.hasCardinalityThreshold()).isTrue();
    }

    @Test
    void routesMultiLocationSnapshotToScheduledInterpreter() {
        AlertRouteUnderstandingResult validated = validator.validate(scheduledRoute(
                        AlertRouteIntentKind.SNAPSHOT_CONDITION,
                        AlertRouteOutputMode.ON_MATCH,
                        true,
                        true,
                        false),
                "Fammi sapere quando a Varedo e Palazzolo Milanese ci sono due treni in arrivo");

        assertThat(validated.decision()).isEqualTo(AlertRouteDecision.ROUTED);
        assertThat(validated.interpreterType()).isEqualTo(AlertRouteInterpreterType.SCHEDULED_INTERPRETER);
        assertThat(validated.accessMode()).isEqualTo(AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT);
    }

    @Test
    void rejectsUnsupportedDomainGenericQuestionAndUnsupportedServiceDataField() {
        assertThat(validator.validate(eventRoute(), "Avvisami quando piove").decision())
                .isEqualTo(AlertRouteDecision.REJECTED);
        assertThat(validator.validate(eventRoute(), "Quanto fa 2+2?").decision())
                .isEqualTo(AlertRouteDecision.REJECTED);
        AlertRouteUnderstandingResult wifi = validator.validate(eventRoute(),
                "Fammi sapere il numero di treni con wifi a bordo a Portello");

        assertThat(wifi.decision()).isEqualTo(AlertRouteDecision.REJECTED);
        assertThat(wifi.rejectedReason()).contains("wifi");
    }

    @Test
    void rejectsUnsafeInconsistentFlagsAndNonServiceDataDomain() {
        AlertRouteUnderstandingResult inconsistent = new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.EVENT_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                AlertRouteIntentKind.EVENT_OCCURRENCE,
                AlertRouteOutputMode.ON_MATCH,
                false,
                true,
                false,
                false,
                false,
                false,
                0.7,
                "Bad flags.",
                null,
                List.of());
        AlertRouteUnderstandingResult weatherDomain = new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA", "WEATHER"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.EVENT_INTERPRETER,
                AlertRouteAccessMode.KAFKA_EVENT,
                AlertRouteIntentKind.EVENT_OCCURRENCE,
                AlertRouteOutputMode.ON_MATCH,
                false,
                false,
                true,
                false,
                false,
                false,
                0.7,
                "Bad domain.",
                null,
                List.of());

        assertThat(validator.validate(inconsistent).decision()).isEqualTo(AlertRouteDecision.REJECTED);
        assertThat(validator.validate(weatherDomain).decision()).isEqualTo(AlertRouteDecision.REJECTED);
    }

    private AlertRouteUnderstandingResult eventRoute() {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.EVENT_INTERPRETER,
                AlertRouteAccessMode.KAFKA_EVENT,
                AlertRouteIntentKind.EVENT_OCCURRENCE,
                AlertRouteOutputMode.ON_MATCH,
                false,
                false,
                true,
                false,
                false,
                false,
                0.86,
                "The alert can be routed to event-based ServiceData verification.",
                null,
                List.of());
    }

    private AlertRouteUnderstandingResult scheduledRoute(
            AlertRouteIntentKind intentKind,
            AlertRouteOutputMode outputMode,
            boolean hasAggregation,
            boolean hasCardinalityThreshold,
            boolean hasReportIntent) {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.SCHEDULED_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                intentKind,
                outputMode,
                true,
                true,
                false,
                hasAggregation,
                hasCardinalityThreshold,
                hasReportIntent,
                0.82,
                "The alert requires a scheduled ServiceData snapshot.",
                null,
                List.of());
    }
}
