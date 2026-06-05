package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertTemporalHintsExtractorTest {

    @Test
    void extractsItalianExplicitFrequencyInMinutes() {
        ScheduledAlertTemporalHints hints = extractor().extract(
                "Ogni 10 minuti dimmi quante corse in ritardo ci sono a Garibaldi FS");

        assertThat(hints.hasExplicitFrequency()).isTrue();
        assertThat(hints.frequencySeconds()).isEqualTo(600);
        assertThat(hints.frequencyRawText()).containsIgnoringCase("ogni 10 minuti");
        assertThat(hints.frequencyDefaulted()).isFalse();
        assertThat(hints.hasExplicitLookaheadWindow()).isFalse();
        assertThat(hints.lookaheadMinutes()).isEqualTo(480);
        assertThat(hints.lookaheadDefaulted()).isTrue();
    }

    @Test
    void extractsItalianHourlyFrequency() {
        ScheduledAlertTemporalHints hints = extractor().extract(
                "Ogni ora fammi sapere quanti treni sono in ritardo");

        assertThat(hints.hasExplicitFrequency()).isTrue();
        assertThat(hints.frequencySeconds()).isEqualTo(3600);
        assertThat(hints.frequencyRawText()).containsIgnoringCase("ogni ora");
    }

    @Test
    void extractsEnglishExplicitFrequencyInMinutes() {
        ScheduledAlertTemporalHints hints = extractor().extract(
                "Every 5 minutes tell me how many delayed services there are at Garibaldi");

        assertThat(hints.hasExplicitFrequency()).isTrue();
        assertThat(hints.frequencySeconds()).isEqualTo(300);
        assertThat(hints.frequencyRawText()).containsIgnoringCase("every 5 minutes");
    }

    @Test
    void extractsItalianExplicitLookaheadInHours() {
        ScheduledAlertTemporalHints hints = extractor().extract(
                "Fammi sapere se ci sono almeno 10 treni in ritardo nelle prossime 2 ore");

        assertThat(hints.hasExplicitFrequency()).isFalse();
        assertThat(hints.frequencySeconds()).isEqualTo(600);
        assertThat(hints.hasExplicitLookaheadWindow()).isTrue();
        assertThat(hints.lookaheadMinutes()).isEqualTo(120);
        assertThat(hints.lookaheadRawText()).containsIgnoringCase("prossime 2 ore");
        assertThat(hints.lookaheadDefaulted()).isFalse();
    }

    @Test
    void extractsFrequencyAndLookaheadSeparately() {
        ScheduledAlertTemporalHints hints = extractor().extract(
                "Every 10 minutes tell me if there are delayed trains in the next 2 hours");

        assertThat(hints.hasExplicitFrequency()).isTrue();
        assertThat(hints.frequencySeconds()).isEqualTo(600);
        assertThat(hints.hasExplicitLookaheadWindow()).isTrue();
        assertThat(hints.lookaheadMinutes()).isEqualTo(120);
    }

    @Test
    void defaultsFrequencyAndLookaheadWhenNoExplicitTemporalExpressionExists() {
        ScheduledAlertTemporalHints hints = extractor().extract(
                "Fammi sapere se a Garibaldi ci sono treni in ritardo");

        assertThat(hints.hasExplicitFrequency()).isFalse();
        assertThat(hints.frequencySeconds()).isEqualTo(600);
        assertThat(hints.frequencyDefaulted()).isTrue();
        assertThat(hints.hasExplicitLookaheadWindow()).isFalse();
        assertThat(hints.lookaheadMinutes()).isEqualTo(480);
        assertThat(hints.lookaheadDefaulted()).isTrue();
    }

    @Test
    void extractsItalianFrequencyInHours() {
        ScheduledAlertTemporalHints hints = extractor().extract("ogni 2 ore");

        assertThat(hints.hasExplicitFrequency()).isTrue();
        assertThat(hints.frequencySeconds()).isEqualTo(7200);
    }

    @Test
    void extractsItalianLookaheadInMinutes() {
        ScheduledAlertTemporalHints hints = extractor().extract("nei prossimi 30 minuti");

        assertThat(hints.hasExplicitLookaheadWindow()).isTrue();
        assertThat(hints.lookaheadMinutes()).isEqualTo(30);
        assertThat(hints.lookaheadRawText()).containsIgnoringCase("prossimi 30 minuti");
    }

    @Test
    void extractsDepartureBetweenJourneyTimeFilter() {
        ScheduledAlertTemporalHints hints = extractor().extract("partono tra le 10:00 e le 12:00");

        assertThat(hints.hasJourneyTimeFilter()).isTrue();
        assertThat(hints.journeyTimeFilters()).hasSize(1);
        ScheduledAlertJourneyTimeFilter filter = hints.journeyTimeFilters().getFirst();
        assertThat(filter.direction()).isEqualTo(ScheduledAlertJourneyTimeFilter.Direction.DEPARTURE);
        assertThat(filter.timeRelation()).isEqualTo(ScheduledAlertJourneyTimeFilter.TimeRelation.BETWEEN);
        assertThat(filter.startLocalTime()).isEqualTo("10:00:00");
        assertThat(filter.endLocalTime()).isEqualTo("12:00:00");
        assertThat(filter.timezone()).isEqualTo("Europe/Rome");
    }

    @Test
    void extractsArrivalBetweenJourneyTimeFilterWithoutMinutes() {
        ScheduledAlertTemporalHints hints = extractor().extract("arrivano tra le 14 e le 16");

        assertThat(hints.hasJourneyTimeFilter()).isTrue();
        ScheduledAlertJourneyTimeFilter filter = hints.journeyTimeFilters().getFirst();
        assertThat(filter.direction()).isEqualTo(ScheduledAlertJourneyTimeFilter.Direction.ARRIVAL);
        assertThat(filter.startLocalTime()).isEqualTo("14:00:00");
        assertThat(filter.endLocalTime()).isEqualTo("16:00:00");
    }

    @Test
    void extractsDepartureAfterJourneyTimeFilter() {
        ScheduledAlertTemporalHints hints = extractor().extract("corse che partono dopo le 18:30");

        ScheduledAlertJourneyTimeFilter filter = hints.journeyTimeFilters().getFirst();
        assertThat(filter.direction()).isEqualTo(ScheduledAlertJourneyTimeFilter.Direction.DEPARTURE);
        assertThat(filter.timeRelation()).isEqualTo(ScheduledAlertJourneyTimeFilter.TimeRelation.AFTER);
        assertThat(filter.startLocalTime()).isEqualTo("18:30:00");
        assertThat(filter.endLocalTime()).isEqualTo("23:59:59");
    }

    @Test
    void extractsArrivalBeforeJourneyTimeFilter() {
        ScheduledAlertTemporalHints hints = extractor().extract("corse che arrivano prima delle 09:00");

        ScheduledAlertJourneyTimeFilter filter = hints.journeyTimeFilters().getFirst();
        assertThat(filter.direction()).isEqualTo(ScheduledAlertJourneyTimeFilter.Direction.ARRIVAL);
        assertThat(filter.timeRelation()).isEqualTo(ScheduledAlertJourneyTimeFilter.TimeRelation.BEFORE);
        assertThat(filter.startLocalTime()).isEqualTo("00:00:00");
        assertThat(filter.endLocalTime()).isEqualTo("09:00:00");
    }

    @Test
    void extractsFrequencyLookaheadAndJourneyTimeFilterSeparately() {
        ScheduledAlertTemporalHints hints = extractor().extract(
                "Ogni 10 minuti dimmi quante corse partono da Garibaldi FS tra le 18 e le 20 nelle prossime 2 ore");

        assertThat(hints.hasExplicitFrequency()).isTrue();
        assertThat(hints.frequencySeconds()).isEqualTo(600);
        assertThat(hints.hasExplicitLookaheadWindow()).isTrue();
        assertThat(hints.lookaheadMinutes()).isEqualTo(120);
        assertThat(hints.hasJourneyTimeFilter()).isTrue();
        ScheduledAlertJourneyTimeFilter filter = hints.journeyTimeFilters().getFirst();
        assertThat(filter.startLocalTime()).isEqualTo("18:00:00");
        assertThat(filter.endLocalTime()).isEqualTo("20:00:00");
    }

    private ScheduledAlertTemporalHintsExtractor extractor() {
        ScheduledAlertTemporalHintsExtractor extractor = new ScheduledAlertTemporalHintsExtractor();
        extractor.defaultFrequencySeconds = 600;
        extractor.minFrequencySeconds = 60;
        extractor.maxFrequencySeconds = 86400;
        extractor.defaultLookaheadMinutes = 480;
        extractor.minLookaheadMinutes = 1;
        extractor.maxLookaheadMinutes = 1440;
        return extractor;
    }
}
