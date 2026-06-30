package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DesiredRuntimeCatalogRequestValidatorTest {
    private final DesiredRuntimeCatalogRequestValidator validator = new DesiredRuntimeCatalogRequestValidator();

    @Test void fullUsesDefaultLimit() { assertThat(validator.validate(request(null))).isEqualTo(100); }
    @Test void boundaryLimitsAreAccepted() {
        assertThat(validator.validate(request(1))).isEqualTo(1);
        assertThat(validator.validate(request(500))).isEqualTo(500);
    }
    @Test void missingModeIsRejected() { rejected(new DesiredRuntimeCatalogRequest(null, null, null, null, null, null)); }
    @Test void fullChangedAfterIsRejected() { rejected(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.FULL, OffsetDateTime.now(), null, null, null, null)); }
    @Test void fullCheckpointIsRejected() { rejected(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.FULL, null, "cp", null, null, null)); }
    @Test void fullIdsAreRejected() { rejected(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.FULL, null, null, Set.of("AGDF1"), null, null)); }
    @Test void incrementalRemainsRejected() {
        rejected(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.INCREMENTAL, null, null, null, null, null));
    }
    @Test void targetedBoundariesAreAccepted() {
        assertThat(validator.validate(targeted(Set.of("AGDF1"), null))).isEqualTo(100);
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (int i = 0; i < 500; i++) ids.add("AGDF" + i);
        assertThat(validator.validate(targeted(ids, 500))).isEqualTo(500);
    }
    @Test void targetedInvalidIdsAreRejected() {
        rejected(targeted(null, null));
        java.util.Set<String> tooMany = new java.util.HashSet<>();
        for (int i = 0; i < 501; i++) tooMany.add("AGDF" + i);
        rejected(targeted(tooMany, null));
        rejected(targeted(Set.of(" "), null));
        rejected(targeted(Set.of("A".repeat(51)), null));
    }
    @Test void targetedChangedAfterAndCheckpointAreRejected() {
        rejected(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.TARGETED,
                OffsetDateTime.now(), null, Set.of("AGDF1"), null, null));
        rejected(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.TARGETED,
                null, "cp", Set.of("AGDF1"), null, null));
    }
    @Test void outOfRangeLimitsAreRejected() { rejected(request(0)); rejected(request(501)); }

    private DesiredRuntimeCatalogRequest request(Integer limit) {
        return new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.FULL, null, null, null, null, limit);
    }
    private DesiredRuntimeCatalogRequest targeted(Set<String> ids, Integer limit) {
        return new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.TARGETED,
                null, null, ids, null, limit);
    }
    private void rejected(DesiredRuntimeCatalogRequest request) {
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
    }
}
