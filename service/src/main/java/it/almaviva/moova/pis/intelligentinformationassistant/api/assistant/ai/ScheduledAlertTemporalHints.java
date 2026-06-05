package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record ScheduledAlertTemporalHints(
        boolean hasExplicitFrequency,
        Integer frequencySeconds,
        String frequencyRawText,
        boolean frequencyDefaulted,
        boolean hasExplicitLookaheadWindow,
        Integer lookaheadMinutes,
        String lookaheadRawText,
        boolean lookaheadDefaulted,
        int defaultFrequencySeconds,
        int minFrequencySeconds,
        int maxFrequencySeconds,
        int defaultLookaheadMinutes,
        int minLookaheadMinutes,
        int maxLookaheadMinutes,
        boolean hasJourneyTimeFilter,
        List<ScheduledAlertJourneyTimeFilter> journeyTimeFilters,
        List<String> warnings) {

    public ScheduledAlertTemporalHints(
            boolean hasExplicitFrequency,
            Integer frequencySeconds,
            String frequencyRawText,
            boolean frequencyDefaulted,
            boolean hasExplicitLookaheadWindow,
            Integer lookaheadMinutes,
            String lookaheadRawText,
            boolean lookaheadDefaulted,
            int defaultFrequencySeconds,
            int minFrequencySeconds,
            int maxFrequencySeconds,
            int defaultLookaheadMinutes,
            int minLookaheadMinutes,
            int maxLookaheadMinutes,
            List<String> warnings) {
        this(
                hasExplicitFrequency,
                frequencySeconds,
                frequencyRawText,
                frequencyDefaulted,
                hasExplicitLookaheadWindow,
                lookaheadMinutes,
                lookaheadRawText,
                lookaheadDefaulted,
                defaultFrequencySeconds,
                minFrequencySeconds,
                maxFrequencySeconds,
                defaultLookaheadMinutes,
                minLookaheadMinutes,
                maxLookaheadMinutes,
                false,
                List.of(),
                warnings);
    }

    public String compactPromptSection() {
        return """
                Backend-derived temporal hints:
                - hasExplicitFrequency: %s
                - frequencySeconds: %s
                - frequencyRawText: %s
                - frequencyDefaulted: %s
                - hasExplicitLookaheadWindow: %s
                - lookaheadMinutes: %s
                - lookaheadRawText: %s
                - lookaheadDefaulted: %s
                - defaultFrequencySeconds: %d
                - minFrequencySeconds: %d
                - maxFrequencySeconds: %d
                - defaultLookaheadMinutes: %d
                - minLookaheadMinutes: %d
                - maxLookaheadMinutes: %d
                - hasJourneyTimeFilter: %s
                - journeyTimeFilters: %s
                - warnings: %s

                Temporal rules:
                - Schedule frequency is how often the Scheduled Agent runs.
                - ServiceData API visibility window is the lookahead interval for POST /v2/stoppointjourneys.
                - Journey time filters are conditions inside snapshotEvaluation.condition over returned journeys.
                - "every 10 minutes" is schedule frequency, not ServiceData lookahead.
                - "next 2 hours" is ServiceData lookahead, not schedule frequency.
                - "departures between 10:00 and 12:00" is a journey time filter, not schedule frequency and not ServiceData lookahead.
                - startMode is always NOW_TRUNCATED_TO_MINUTE for this MVP.
                - If hasExplicitFrequency=true, technicalSpecification.schedule.frequencySeconds must equal frequencySeconds, schedule.defaulted must be false, and schedule.rawText should be frequencyRawText.
                - If hasExplicitFrequency=false, technicalSpecification.schedule.frequencySeconds must equal defaultFrequencySeconds, schedule.defaulted must be true, and schedule.rawText must be null or absent.
                - If hasExplicitLookaheadWindow=true, serviceDataQuery.timeWindow.endMode must be NOW_PLUS_DURATION, lookaheadMinutes must equal the backend hint, timeWindow.defaulted must be false, and rawText should be lookaheadRawText.
                - If hasExplicitLookaheadWindow=false, serviceDataQuery.timeWindow.endMode must be NOW_PLUS_DEFAULT_LOOKAHEAD, lookaheadMinutes must equal defaultLookaheadMinutes, timeWindow.defaulted must be true, and rawText must be null or absent.
                - If hasJourneyTimeFilter=true, snapshotEvaluation.condition must include matching LOCAL_TIME_BETWEEN on a direction-compatible journey time field inside stopPointsJourneyDetails[] correlation.
                """.formatted(
                hasExplicitFrequency,
                frequencySeconds,
                frequencyRawText,
                frequencyDefaulted,
                hasExplicitLookaheadWindow,
                lookaheadMinutes,
                lookaheadRawText,
                lookaheadDefaulted,
                defaultFrequencySeconds,
                minFrequencySeconds,
                maxFrequencySeconds,
                defaultLookaheadMinutes,
                minLookaheadMinutes,
                maxLookaheadMinutes,
                hasJourneyTimeFilter,
                journeyTimeFilters == null ? List.of() : journeyTimeFilters,
                warnings == null ? List.of() : warnings);
    }
}
