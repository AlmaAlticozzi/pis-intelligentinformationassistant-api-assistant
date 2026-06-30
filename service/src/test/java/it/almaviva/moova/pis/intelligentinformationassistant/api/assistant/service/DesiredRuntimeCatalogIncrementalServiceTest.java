package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeAgentSubmission;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogUpsertItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRow;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DesiredRuntimeCatalogIncrementalServiceTest {
    @Test void checkpointMixedPageUsesSourceAndFinalCheckpoint() {
        var repository = mock(DesiredRuntimeCatalogRepository.class);
        var mapper = mock(DesiredRuntimeCatalogMapper.class);
        var json = JsonMapper.builder().findAndAddModules().build();
        var checkpoints = new DesiredRuntimeCatalogCheckpointCodec(json);
        var service = new DesiredRuntimeCatalogService(new DesiredRuntimeCatalogRequestValidator(),
                new DesiredRuntimeCatalogCursorCodec(json), checkpoints, repository, mapper,
                Clock.fixed(Instant.parse("2026-06-30T15:30:00Z"), ZoneOffset.UTC));
        String source = checkpoints.encode(new DesiredRuntimeCatalogCheckpoint(1, 5,
                OffsetDateTime.parse("2026-06-30T15:00:00Z")));
        DesiredRuntimeCatalogRow upsert = row("AGDF1", "UPSERT", null);
        DesiredRuntimeCatalogRow remove = row("AGDF2", "REMOVE", "NOT_ACTIVE");
        when(repository.findCurrentUpperSequence("INCREMENTAL")).thenReturn(9L);
        when(repository.findIncrementalPage(5L, null, 9L, null, null, 101))
                .thenReturn(List.of(upsert, remove));
        when(mapper.map(upsert)).thenReturn(new DesiredRuntimeCatalogUpsertItem()
                .action(DesiredRuntimeCatalogUpsertItem.ActionEnum.UPSERT).agentDefinitionId("AGDF1")
                .sourceStatus(DesiredRuntimeCatalogUpsertItem.SourceStatusEnum.ACTIVE)
                .runtimePackage(new DesiredRuntimeAgentSubmission().submissionId("SUB")));

        var response = service.get(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.INCREMENTAL,
                null, source, null, null, null));

        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getSourceCheckpoint()).isEqualTo(source);
        assertThat(response.getNextCheckpoint()).isNotBlank();
        assertThat(response.getPage().getHasMore()).isFalse();
        assertThat(checkpoints.decode(response.getNextCheckpoint()).changeSequence()).isEqualTo(9);
    }

    @Test void changedAfterEmptyIntervalHasNoSourceAndStillAdvancesCheckpoint() {
        var repository = mock(DesiredRuntimeCatalogRepository.class);
        var mapper = mock(DesiredRuntimeCatalogMapper.class);
        var json = JsonMapper.builder().findAndAddModules().build();
        var service = new DesiredRuntimeCatalogService(new DesiredRuntimeCatalogRequestValidator(),
                new DesiredRuntimeCatalogCursorCodec(json), new DesiredRuntimeCatalogCheckpointCodec(json),
                repository, mapper, Clock.fixed(Instant.parse("2026-06-30T15:30:00Z"), ZoneOffset.UTC));
        OffsetDateTime changedAfter = OffsetDateTime.parse("2026-06-30T15:00:00Z");
        when(repository.findCurrentUpperSequence("INCREMENTAL")).thenReturn(5L);
        when(repository.findIncrementalPage(null, changedAfter, 5L, null, null, 101)).thenReturn(List.of());
        var response = service.get(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.INCREMENTAL,
                changedAfter, null, null, null, null));
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getSourceCheckpoint()).isNull();
        assertThat(response.getNextCheckpoint()).isNotBlank();
    }

    private DesiredRuntimeCatalogRow row(String id, String action, String reason) {
        boolean upsert = "UPSERT".equals(action);
        return new DesiredRuntimeCatalogRow(upsert ? 6 : 7, "RTCH" + id, id, action,
                upsert ? "ACTIVE" : "DISABLED", reason, OffsetDateTime.parse("2026-06-30T15:10:00Z"),
                upsert ? "RTPK" : null, upsert ? 1 : 0, upsert ? "a".repeat(64) : null,
                upsert ? id : null, upsert ? 1 : 0, upsert ? "SUB" : null,
                upsert ? "a".repeat(64) : null, upsert ? java.util.Map.of("submissionId", "SUB") : null);
    }
}
