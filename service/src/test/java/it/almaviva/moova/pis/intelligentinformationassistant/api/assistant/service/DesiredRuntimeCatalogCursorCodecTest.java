package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DesiredRuntimeCatalogCursorCodecTest {
    private final DesiredRuntimeCatalogCursorCodec codec = new DesiredRuntimeCatalogCursorCodec(
            JsonMapper.builder().findAndAddModules().build());

    @Test void roundTripPreservesSnapshotAndPosition() {
        DesiredRuntimeCatalogCursor value = cursor("FULL");
        assertThat(codec.decode(codec.encode(value))).isEqualTo(value);
    }
    @Test void malformedCursorIsRejected() {
        assertThatThrownBy(() -> codec.decode("%%%"))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
    }
    @Test void wrongModeIsRejected() {
        assertThatThrownBy(() -> codec.decode(codec.encode(cursor("TARGETED"))))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
    }
    @Test void targetedRoundTripPreservesFilterAndSnapshot() {
        String fingerprint = "a".repeat(64);
        var value = new DesiredRuntimeCatalogCursor(1, "TARGETED", 42L,
                OffsetDateTime.parse("2026-06-30T13:00:00Z"), null, "AGDF1", fingerprint);
        assertThat(codec.decodeTargeted(codec.encode(value), fingerprint)).isEqualTo(value);
        assertThatThrownBy(() -> codec.decodeTargeted(codec.encode(value), "b".repeat(64)))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
        assertThatThrownBy(() -> codec.decode(codec.encode(value)))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
    }
    @Test void targetFingerprintIsIndependentOfInputOrder() {
        assertThat(DesiredRuntimeCatalogTargetFilter.fingerprint(
                DesiredRuntimeCatalogTargetFilter.sorted(java.util.Set.of("B", "A"))))
                .isEqualTo(DesiredRuntimeCatalogTargetFilter.fingerprint(java.util.List.of("A", "B")));
    }
    @Test void incrementalCursorPreservesStablePositionAndRejectsOtherModesOrFilters() {
        String fingerprint = "c".repeat(64);
        var value = new DesiredRuntimeCatalogCursor(1, "INCREMENTAL", 99L,
                OffsetDateTime.parse("2026-06-30T15:00:00Z"),
                OffsetDateTime.parse("2026-06-30T14:59:00Z"), "AGDF9", fingerprint);
        String encoded = codec.encode(value);
        assertThat(codec.decodeIncremental(encoded, fingerprint)).isEqualTo(value);
        assertThatThrownBy(() -> codec.decodeIncremental(encoded, "d".repeat(64)))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
        assertThatThrownBy(() -> codec.decode(encoded))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
    }
    @Test void checkpointHasStableFutureFormat() {
        var mapper = JsonMapper.builder().findAndAddModules().build();
        var checkpointCodec = new DesiredRuntimeCatalogCheckpointCodec(mapper);
        var checkpoint = new DesiredRuntimeCatalogCheckpoint(1, 42L,
                OffsetDateTime.parse("2026-06-30T13:00:00Z"));
        assertThat(checkpointCodec.decode(checkpointCodec.encode(checkpoint))).isEqualTo(checkpoint);
    }

    private DesiredRuntimeCatalogCursor cursor(String mode) {
        return new DesiredRuntimeCatalogCursor(1, mode, 42L,
                OffsetDateTime.parse("2026-06-30T13:00:00Z"),
                OffsetDateTime.parse("2026-06-30T12:30:00Z"), "AGDF1", null);
    }
}
