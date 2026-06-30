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
    @Test void stagedModesAreRejected() {
        rejected(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.INCREMENTAL, null, null, null, null, null));
        rejected(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.TARGETED, null, null, null, null, null));
    }
    @Test void outOfRangeLimitsAreRejected() { rejected(request(0)); rejected(request(501)); }

    private DesiredRuntimeCatalogRequest request(Integer limit) {
        return new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.FULL, null, null, null, null, limit);
    }
    private void rejected(DesiredRuntimeCatalogRequest request) {
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(DesiredRuntimeCatalogInvalidRequestException.class);
    }
}
