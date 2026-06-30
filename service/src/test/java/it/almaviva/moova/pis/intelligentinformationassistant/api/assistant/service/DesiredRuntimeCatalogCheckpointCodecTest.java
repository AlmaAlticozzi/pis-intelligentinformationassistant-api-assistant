package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DesiredRuntimeCatalogCheckpointCodecTest {
    private final DesiredRuntimeCatalogCheckpointCodec codec = new DesiredRuntimeCatalogCheckpointCodec(
            JsonMapper.builder().findAndAddModules().build());

    @Test void fullV1CheckpointRoundTripsAndSequenceGapsAreValid() {
        var value = new DesiredRuntimeCatalogCheckpoint(1, 5,
                OffsetDateTime.parse("2026-06-30T15:17:31.4990985Z"));
        assertThat(codec.decodeForIncremental(codec.encode(value), 9)).isEqualTo(value);
        var zero = new DesiredRuntimeCatalogCheckpoint(1, 0, value.catalogAsOf());
        assertThat(codec.decodeForIncremental(codec.encode(zero), 9)).isEqualTo(zero);
    }

    @Test void malformedAndNegativeAreBadRequests() {
        assertThatThrownBy(() -> codec.decodeForIncremental("%%%", 9))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
        var negative = new DesiredRuntimeCatalogCheckpoint(1, -1, OffsetDateTime.now());
        assertThatThrownBy(() -> codec.decodeForIncremental(codec.encode(negative), 9))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
    }

    @Test void unsupportedVersionAndFutureSequenceAreUnprocessable() {
        var futureVersion = new DesiredRuntimeCatalogCheckpoint(2, 1, OffsetDateTime.now());
        assertThatThrownBy(() -> codec.decodeForIncremental(codec.encode(futureVersion), 9))
                .isInstanceOf(DesiredRuntimeCatalogCheckpointIncompatibleException.class);
        var futureSequence = new DesiredRuntimeCatalogCheckpoint(1, 10, OffsetDateTime.now());
        assertThatThrownBy(() -> codec.decodeForIncremental(codec.encode(futureSequence), 9))
                .isInstanceOf(DesiredRuntimeCatalogCheckpointIncompatibleException.class);
    }

    @Test void semanticFingerprintNormalizesEquivalentInstantsAndBoundTypesDiffer() {
        var first = new DesiredRuntimeCatalogCheckpoint(1, 5,
                OffsetDateTime.parse("2026-06-30T15:00:00Z"));
        var equivalent = new DesiredRuntimeCatalogCheckpoint(1, 5,
                OffsetDateTime.parse("2026-06-30T17:00:00+02:00"));
        assertThat(DesiredRuntimeCatalogIncrementalFilter.checkpoint(first))
                .isEqualTo(DesiredRuntimeCatalogIncrementalFilter.checkpoint(equivalent));
        assertThat(DesiredRuntimeCatalogIncrementalFilter.changedAfter(first.catalogAsOf()))
                .isNotEqualTo(DesiredRuntimeCatalogIncrementalFilter.checkpoint(first));
    }
}
