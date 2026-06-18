package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceDataEventIntentDetectorTest {

    @Test
    void detectsDepartureProgressiveAndCompletedIntents() {
        assertThat(ServiceDataEventIntentDetector.detectIntent("Avvertimi quando una corsa e in partenza"))
                .hasValueSatisfying(intent -> {
                    assertThat(intent.family()).isEqualTo("DEPARTURE_PROGRESSIVE");
                    assertThat(intent.eventTypes()).containsExactly("DEPARTING");
                });

        assertThat(ServiceDataEventIntentDetector.detectIntent("Avvertimi quando una corsa e partita"))
                .hasValueSatisfying(intent -> {
                    assertThat(intent.family()).isEqualTo("DEPARTURE_COMPLETED");
                    assertThat(intent.eventTypes()).containsExactly("DEPARTED");
                });
    }

    @Test
    void detectsArrivalAndPlatformChangeIntents() {
        assertThat(ServiceDataEventIntentDetector.detectIntent("Avvertimi quando una corsa arriva"))
                .hasValueSatisfying(intent -> assertThat(intent.eventTypes()).containsExactly("ARRIVING"));

        assertThat(ServiceDataEventIntentDetector.detectIntent("Avvertimi quando una corsa cambia binario"))
                .hasValueSatisfying(intent -> assertThat(intent.eventTypes())
                        .containsExactly("ARRIVAL_PLATFORM_CHANGED", "DEPARTURE_PLATFORM_CHANGED"));
    }
}
