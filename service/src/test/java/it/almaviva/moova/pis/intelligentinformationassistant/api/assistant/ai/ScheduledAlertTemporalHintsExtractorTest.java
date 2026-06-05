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
