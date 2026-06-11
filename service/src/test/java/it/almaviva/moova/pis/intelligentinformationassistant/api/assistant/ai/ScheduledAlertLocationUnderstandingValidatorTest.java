package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertLocationUnderstandingValidatorTest {

    private final ScheduledAlertLocationUnderstandingValidator validator =
            new ScheduledAlertLocationUnderstandingValidator();

    @Test
    void movesPlatformLocationToNonLocationConstraint() {
        String prompt = "Fammi sapere il numero di corse che partiranno dal binario 7 a Cenisio";
        ScheduledAlertLocationUnderstandingResult validated = validator.validate(
                result(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        mention("Cenisio", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true),
                        mention("binario 7", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true)),
                prompt,
                ScheduledAlertLocationUnderstandingHints.fromPrompt(prompt));

        assertThat(validated.locations()).extracting(ScheduledAlertLocationMention::rawText)
                .containsExactly("Cenisio");
        assertThat(validated.nonLocationConstraints()).anyMatch(constraint ->
                constraint.type() == ScheduledAlertNonLocationConstraintType.PLATFORM
                        && constraint.rawText().contains("binario"));
    }

    @Test
    void routeOnlyLocationsBecomeUnspecifiedWithWarning() {
        String prompt = "Fammi sapere se la corsa 899 passa a Gerusalemme, Tre Torri e Portello";
        ScheduledAlertLocationUnderstandingResult validated = validator.validate(
                result(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        mention("Gerusalemme", ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT, false),
                        mention("Tre Torri", ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT, false),
                        mention("Portello", ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT, false)),
                prompt,
                ScheduledAlertLocationUnderstandingHints.fromPrompt(prompt));

        assertThat(validated.monitoringScope()).isEqualTo(ScheduledAlertMonitoringScope.UNSPECIFIED);
        assertThat(validated.locations()).allMatch(location ->
                location.role() == ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT);
        assertThat(validated.warnings()).anyMatch(warning -> warning.contains("monitored stop point is required"));
    }

    @Test
    void defaultsRequiredCoverageToTrueForFilterLocationWithoutApiQueryRequirement() {
        String prompt = "Ogni 10 minuti dimmi quante corse a Garibaldi FS hanno origine Monza";
        ScheduledAlertLocationUnderstandingResult validated = validator.validate(
                result(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        mention("Garibaldi FS", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true),
                        mention("Monza", ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT, false, false)),
                prompt,
                ScheduledAlertLocationUnderstandingHints.fromPrompt(prompt));

        ScheduledAlertLocationMention monza = validated.locations().stream()
                .filter(location -> location.rawText().equals("Monza"))
                .findFirst()
                .orElseThrow();
        assertThat(monza.requiredForApiQuery()).isFalse();
        assertThat(monza.requiredCoverage()).isTrue();
    }

    @Test
    void complexPromptKeepsMonitoredExcludedDestinationAndCapabilityConstraints() {
        String prompt = "Fammi sapere se il numero di treni in ritardo e che hanno subito un cambio di binario a Buonarroti e maggiore di 5. L'importante e che non hanno come destinazione Tre Torri";
        ScheduledAlertLocationUnderstandingResult validated = validator.validate(
                result(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        mention("Buonarroti", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true),
                        excluded("Tre Torri", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT)),
                prompt,
                ScheduledAlertLocationUnderstandingHints.fromPrompt(prompt));

        assertThat(validated.locations().getFirst().role()).isEqualTo(ScheduledAlertLocationRole.MONITORED_STOP_POINT);
        assertThat(validated.locations().get(1).role()).isEqualTo(ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT);
        assertThat(validated.locations().get(1).polarity()).isEqualTo(ScheduledAlertLocationPolarity.EXCLUDE);
        assertThat(validated.nonLocationConstraints()).anyMatch(constraint ->
                constraint.type() == ScheduledAlertNonLocationConstraintType.PLATFORM);
        assertThat(validated.nonLocationConstraints()).anyMatch(constraint ->
                constraint.type() == ScheduledAlertNonLocationConstraintType.DELAY);
    }

    @Test
    void unsupportedCarriagesArePreservedAsNonLocationCapability() {
        String prompt = "Fammi sapere il numero di treni con piu di 10 carrozze a Gerusalemme";
        ScheduledAlertLocationUnderstandingResult validated = validator.validate(
                result(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                        mention("Gerusalemme", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true)),
                prompt,
                ScheduledAlertLocationUnderstandingHints.fromPrompt(prompt));

        assertThat(validated.locations()).extracting(ScheduledAlertLocationMention::rawText)
                .containsExactly("Gerusalemme");
        assertThat(validated.nonLocationConstraints()).anyMatch(constraint ->
                constraint.type() == ScheduledAlertNonLocationConstraintType.UNSUPPORTED_CAPABILITY);
    }

    private ScheduledAlertLocationUnderstandingResult result(
            ScheduledAlertMonitoringScope scope,
            ScheduledAlertLocationMention... mentions) {
        return new ScheduledAlertLocationUnderstandingResult(
                mentions.length > 0,
                "it",
                scope,
                List.of(mentions),
                List.of(),
                List.of());
    }

    private ScheduledAlertLocationMention mention(String rawText, ScheduledAlertLocationRole role, boolean requiredForApiQuery) {
        return mention(rawText, role, requiredForApiQuery, true);
    }

    private ScheduledAlertLocationMention mention(
            String rawText,
            ScheduledAlertLocationRole role,
            boolean requiredForApiQuery,
            boolean requiredCoverage) {
        return new ScheduledAlertLocationMention(
                rawText,
                rawText,
                role,
                requiredForApiQuery
                        ? ScheduledAlertLocationRelation.SERVICE_DATA_API_QUERY_STOP_POINT
                        : ScheduledAlertLocationRelation.ROUTE_FILTER,
                requiredForApiQuery,
                requiredCoverage,
                ScheduledAlertLocationPolarity.INCLUDE,
                "G1",
                0.95);
    }

    private ScheduledAlertLocationMention excluded(String rawText, ScheduledAlertLocationRole role) {
        return new ScheduledAlertLocationMention(
                rawText,
                rawText,
                role,
                ScheduledAlertLocationRelation.DESTINATION_FILTER,
                false,
                true,
                ScheduledAlertLocationPolarity.EXCLUDE,
                "G2",
                0.95);
    }
}
