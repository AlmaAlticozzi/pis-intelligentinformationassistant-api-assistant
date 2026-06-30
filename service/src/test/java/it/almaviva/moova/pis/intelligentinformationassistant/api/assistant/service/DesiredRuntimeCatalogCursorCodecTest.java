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
                OffsetDateTime.parse("2026-06-30T12:30:00Z"), "AGDF1");
    }
}
