package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeAgentSubmission;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogUpsertItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DesiredRuntimeCatalogServiceTest {
    private DesiredRuntimeCatalogRepository repository;
    private DesiredRuntimeCatalogMapper mapper;
    private DesiredRuntimeCatalogCursorCodec cursorCodec;
    private DesiredRuntimeCatalogCheckpointCodec checkpointCodec;
    private DesiredRuntimeCatalogService service;

    @BeforeEach void setUp() {
        var objectMapper = JsonMapper.builder().findAndAddModules().build();
        repository = mock(DesiredRuntimeCatalogRepository.class);
        mapper = mock(DesiredRuntimeCatalogMapper.class);
        cursorCodec = new DesiredRuntimeCatalogCursorCodec(objectMapper);
        checkpointCodec = new DesiredRuntimeCatalogCheckpointCodec(objectMapper);
        service = new DesiredRuntimeCatalogService(new DesiredRuntimeCatalogRequestValidator(), cursorCodec,
                checkpointCodec, repository, mapper,
                Clock.fixed(Instant.parse("2026-06-30T13:00:00Z"), ZoneOffset.UTC));
    }

    @Test void emptySnapshotReturnsFinalCheckpointAndNonNullItems() {
        when(repository.findCurrentUpperSequence()).thenReturn(0L);
        when(repository.findFullSnapshotPage(0L, null, null, 101)).thenReturn(List.of());

        var response = service.get(request(null, null));

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getMode()).isEqualTo(DesiredRuntimeCatalogMode.FULL);
        assertThat(response.getCatalogAsOf()).isEqualTo("2026-06-30T13:00:00Z");
        assertThat(response.getPage().getHasMore()).isFalse();
        assertThat(response.getPage().getNextCursor()).isNull();
        assertThat(response.getNextCheckpoint()).isNotBlank();
    }

    @Test void limitPlusOneCreatesStableCursorAndSecondPageCheckpoint() {
        DesiredRuntimeCatalogRow first = row("AGDF1", "2026-06-30T10:00:00Z");
        DesiredRuntimeCatalogRow extra = row("AGDF2", "2026-06-30T11:00:00Z");
        when(repository.findCurrentUpperSequence()).thenReturn(42L);
        when(repository.findFullSnapshotPage(42L, null, null, 2)).thenReturn(List.of(first, extra));
        when(mapper.map(any())).thenReturn(item());

        var firstPage = service.get(request(null, 1));

        assertThat(firstPage.getPage().getReturned()).isEqualTo(1);
        assertThat(firstPage.getPage().getHasMore()).isTrue();
        assertThat(firstPage.getPage().getNextCursor()).isNotBlank();
        assertThat(firstPage.getNextCheckpoint()).isNull();
        DesiredRuntimeCatalogCursor cursor = cursorCodec.decode(firstPage.getPage().getNextCursor());
        assertThat(cursor.catalogUpperSequence()).isEqualTo(42L);
        assertThat(cursor.catalogAsOf()).isEqualTo(firstPage.getCatalogAsOf());
        assertThat(cursor.lastAgentDefinitionId()).isEqualTo("AGDF1");

        when(repository.findFullSnapshotPage(42L, cursor.lastSourceUpdatedAt(), "AGDF1", 2))
                .thenReturn(List.of(extra));
        var finalPage = service.get(request(firstPage.getPage().getNextCursor(), 1));
        assertThat(finalPage.getCatalogAsOf()).isEqualTo(firstPage.getCatalogAsOf());
        assertThat(finalPage.getPage().getHasMore()).isFalse();
        assertThat(finalPage.getPage().getNextCursor()).isNull();
        assertThat(finalPage.getNextCheckpoint()).isNotBlank();
        verify(repository).findFullSnapshotPage(42L, cursor.lastSourceUpdatedAt(), "AGDF1", 2);
    }

    private DesiredRuntimeCatalogRequest request(String cursor, Integer limit) {
        return new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.FULL, null, null, null, cursor, limit);
    }
    private DesiredRuntimeCatalogRow row(String id, String time) {
        return new DesiredRuntimeCatalogRow(1, "RTCH", id, "UPSERT", "ACTIVE", OffsetDateTime.parse(time),
                "RTPK", 1, "a".repeat(64), id, 1, "SUB", "a".repeat(64), java.util.Map.of());
    }
    private DesiredRuntimeCatalogUpsertItem item() {
        return new DesiredRuntimeCatalogUpsertItem().action(DesiredRuntimeCatalogUpsertItem.ActionEnum.UPSERT)
                .agentDefinitionId("AGDF1").sourceStatus(DesiredRuntimeCatalogUpsertItem.SourceStatusEnum.ACTIVE)
                .sourceUpdatedAt(OffsetDateTime.parse("2026-06-30T10:00:00Z"))
                .packageFingerprint("a".repeat(64)).runtimePackage(new DesiredRuntimeAgentSubmission());
    }
}
