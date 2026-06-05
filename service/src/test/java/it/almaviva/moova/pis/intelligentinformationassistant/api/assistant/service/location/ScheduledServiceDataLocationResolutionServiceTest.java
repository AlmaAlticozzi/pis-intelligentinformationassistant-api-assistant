package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationMention;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationPolarity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationRelation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationRole;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertMonitoringScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledServiceDataLocationResolutionServiceTest {

    private final PointResolver pointResolver = new PointResolver(new PointRegistry(), new PointNormalizer());
    private final ScheduledServiceDataLocationResolutionService service =
            new ScheduledServiceDataLocationResolutionService(pointResolver);

    @Test
    void resolvesMonitoredGaribaldiFsToServiceDataApiStopPoints() {
        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored("Garibaldi FS")));

        assertThat(context.hasUnresolvedRequiredMonitoredLocations()).isFalse();
        assertThat(context.monitoredLocations()).hasSize(1);
        assertThat(context.serviceDataApiStopPoints()).containsExactlyElementsOf(selectedIds("Garibaldi FS"));
        assertThat(context.monitoredLocations().getFirst().targetFieldHints()).containsExactly("body.stopPoints[]");
    }

    @Test
    void resolvesMonitoredAndOriginFilterWithoutAddingFilterToApiStopPoints() {
        List<String> peroIds = selectedIds("Pero");
        List<String> garibaldiIds = selectedIds("Garibaldi FS");

        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored("Pero"),
                filter("Garibaldi FS", ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT)));

        assertThat(context.serviceDataApiStopPoints()).containsExactlyElementsOf(peroIds);
        assertThat(context.serviceDataApiStopPoints()).doesNotContainAnyElementsOf(garibaldiIds);
        assertThat(context.filterLocations()).hasSize(1);
        assertThat(context.filterLocations().getFirst().rawText()).isEqualTo("Garibaldi FS");
        assertThat(context.filterLocations().getFirst().targetFieldHints())
                .contains("stopPointsJourneyDetails[].callStart.stopPoint.id");
    }

    @Test
    void platformPromptUsesOnlyMonitoredCenisioForApiStopPoints() {
        List<String> cenisioIds = selectedIds("Cenisio");

        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored("Cenisio")));

        assertThat(context.serviceDataApiStopPoints()).containsExactlyElementsOf(cenisioIds);
        assertThat(context.filterLocations()).isEmpty();
    }

    @Test
    void filterLocationsAreNeverAddedToApiStopPoints() {
        List<String> buonarrotiIds = selectedIds("Buonarroti");
        List<String> treTorriIds = selectedIds("Tre Torri");

        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored("Buonarroti"),
                filter("Tre Torri", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT)));

        assertThat(context.serviceDataApiStopPoints()).containsExactlyElementsOf(buonarrotiIds);
        assertThat(context.serviceDataApiStopPoints()).doesNotContainAnyElementsOf(treTorriIds);
        assertThat(context.filterLocations()).hasSize(1);
    }

    @Test
    void duplicateMonitoredStopPointsAreDeduplicatedInApiStopPointsPreservingOrder() {
        List<String> varedoIds = selectedIds("Varedo");
        List<String> palazzoloIds = selectedIds("Palazzolo Milanese");

        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored("Varedo"),
                monitored("Varedo"),
                monitored("Palazzolo Milanese")));

        assertThat(context.serviceDataApiStopPoints())
                .containsExactlyElementsOf(concat(varedoIds, palazzoloIds));
    }

    @Test
    void resolvesMultiMonitoredLocationsIntoApiStopPointUnion() {
        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored("Varedo"),
                monitored("Palazzolo Milanese")));

        assertThat(context.serviceDataApiStopPoints())
                .containsExactlyElementsOf(concat(selectedIds("Varedo"), selectedIds("Palazzolo Milanese")));
        assertThat(context.monitoredLocations()).hasSize(2);
    }

    @Test
    void unresolvedRequiredMonitoredLocationIsValidationFailureWithoutFallback() {
        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored("Fermata Inventata")));

        assertThat(context.hasUnresolvedRequiredMonitoredLocations()).isTrue();
        assertThat(context.unresolvedRequiredMonitoredLocationTexts()).containsExactly("Fermata Inventata");
        assertThat(context.serviceDataApiStopPoints()).isEmpty();
        assertThat(context.monitoredLocations().getFirst().fallbackToNameLong()).isFalse();
        assertThat(context.monitoredLocations().getFirst().fallbackAllowed()).isFalse();
        assertThat(context.warnings()).anyMatch(warning ->
                warning.contains("Monitored stop point 'Fermata Inventata' could not be resolved"));
    }

    @Test
    void unresolvedFilterLocationAllowsTextualFallbackWhenRoleSupportsIt() {
        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                filter("Fermata Inventata", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT)));

        assertThat(context.hasUnresolvedRequiredMonitoredLocations()).isFalse();
        assertThat(context.filterLocations()).hasSize(1);
        ScheduledServiceDataResolvedLocation filter = context.filterLocations().getFirst();
        assertThat(filter.resolutionStatus()).isEqualTo(ScheduledServiceDataLocationResolutionStatus.UNRESOLVED);
        assertThat(filter.fallbackAllowed()).isTrue();
        assertThat(filter.fallbackToNameLong()).isTrue();
        assertThat(filter.targetFieldHints()).contains("stopPointsJourneyDetails[].callEnd.stopPoint.nameLong");
    }

    @Test
    void platformMentionIsNotResolvedAndDoesNotProduceApiStopPoints() {
        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                monitored("binario 3")));

        assertThat(context.serviceDataApiStopPoints()).isEmpty();
        assertThat(context.hasUnresolvedRequiredMonitoredLocations()).isFalse();
        assertThat(context.monitoredLocations().getFirst().resolutionStatus())
                .isEqualTo(ScheduledServiceDataLocationResolutionStatus.NOT_APPLICABLE);
        assertThat(context.warnings()).anyMatch(warning -> warning.contains("platform/binario constraint"));
    }

    @Test
    void allKnownStopPointsScopeDoesNotRequireExplicitResolution() {
        ScheduledServiceDataLocationContext context = service.resolve(result(
                ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS,
                List.of()));

        assertThat(context.requiresAllKnownStopPoints()).isTrue();
        assertThat(context.hasUnresolvedRequiredMonitoredLocations()).isFalse();
        assertThat(context.serviceDataApiStopPoints()).isEmpty();
        assertThat(context.apiQueryContext().requiresAllKnownStopPoints()).isTrue();
        assertThat(context.warnings()).contains(
                "All known stop points scope requested; ids will be materialized by runtime or later verification phase.");
    }

    private ScheduledAlertLocationUnderstandingResult result(
            ScheduledAlertMonitoringScope scope,
            ScheduledAlertLocationMention... mentions) {
        return result(scope, List.of(mentions));
    }

    private ScheduledAlertLocationUnderstandingResult result(
            ScheduledAlertMonitoringScope scope,
            List<ScheduledAlertLocationMention> mentions) {
        return new ScheduledAlertLocationUnderstandingResult(
                !mentions.isEmpty(),
                "it",
                scope,
                mentions,
                List.of(),
                List.of());
    }

    private ScheduledAlertLocationMention monitored(String rawText) {
        return mention(rawText, ScheduledAlertLocationRole.MONITORED_STOP_POINT, true, true);
    }

    private ScheduledAlertLocationMention filter(String rawText, ScheduledAlertLocationRole role) {
        return mention(rawText, role, false, true);
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
                        : ScheduledAlertLocationRelation.UNKNOWN,
                requiredForApiQuery,
                requiredCoverage,
                ScheduledAlertLocationPolarity.INCLUDE,
                "G1",
                0.95);
    }

    private List<String> selectedIds(String rawText) {
        PointResolutionResult result = pointResolver.resolve(rawText);
        if (result.status() == PointResolutionStatus.RESOLVED_AMBIGUOUS) {
            return result.candidates().stream()
                    .map(PointCandidate::id)
                    .toList();
        }
        return result.candidates().stream()
                .filter(PointCandidate::selected)
                .map(PointCandidate::id)
                .toList();
    }

    private List<String> concat(List<String> first, List<String> second) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>(first);
        for (String value : second) {
            if (!values.contains(value)) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }
}
