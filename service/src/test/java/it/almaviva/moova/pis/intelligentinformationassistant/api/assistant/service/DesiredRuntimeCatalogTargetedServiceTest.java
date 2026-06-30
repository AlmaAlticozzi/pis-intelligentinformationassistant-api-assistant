package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeAgentSubmission;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogUpsertItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogAgentState;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRow;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DesiredRuntimeCatalogTargetedServiceTest {
    @Test void mixedOutcomesAreOrderedAndCheckpointFree() {
        var repository = mock(DesiredRuntimeCatalogRepository.class);
        var mapper = mock(DesiredRuntimeCatalogMapper.class);
        var json = JsonMapper.builder().findAndAddModules().build();
        var service = new DesiredRuntimeCatalogService(new DesiredRuntimeCatalogRequestValidator(),
                new DesiredRuntimeCatalogCursorCodec(json), new DesiredRuntimeCatalogCheckpointCodec(json),
                repository, mapper, Clock.fixed(Instant.parse("2026-06-30T15:00:00Z"), ZoneOffset.UTC));
        Set<String> ids = Set.of("AGDF_UNKNOWN", "AGDF_READY", "AGDF_DISABLED", "AGDF_ACTIVE");
        when(repository.findCurrentUpperSequence()).thenReturn(9L);
        DesiredRuntimeCatalogRow active = row("AGDF_ACTIVE", "UPSERT", "ACTIVE");
        DesiredRuntimeCatalogRow disabled = row("AGDF_DISABLED", "REMOVE", "DISABLED");
        when(repository.findLatestCatalogChangesForAgentIds(ids, 9L))
                .thenReturn(Map.of("AGDF_ACTIVE", active, "AGDF_DISABLED", disabled));
        when(repository.findAgentDefinitionsByIds(ids)).thenReturn(Map.of(
                "AGDF_ACTIVE", state("AGDF_ACTIVE", "ACTIVE"),
                "AGDF_DISABLED", state("AGDF_DISABLED", "DISABLED"),
                "AGDF_READY", state("AGDF_READY", "READY")));
        when(mapper.map(active)).thenReturn(new DesiredRuntimeCatalogUpsertItem()
                .action(DesiredRuntimeCatalogUpsertItem.ActionEnum.UPSERT).agentDefinitionId("AGDF_ACTIVE")
                .sourceStatus(DesiredRuntimeCatalogUpsertItem.SourceStatusEnum.ACTIVE)
                .runtimePackage(new DesiredRuntimeAgentSubmission().submissionId("SUB")));

        var response = service.get(new DesiredRuntimeCatalogRequest(DesiredRuntimeCatalogMode.TARGETED,
                null, null, ids, null, 100));

        assertThat(response.getMode()).isEqualTo(DesiredRuntimeCatalogMode.TARGETED);
        assertThat((java.util.List<?>) response.getItems())
                .extracting(item -> item.getClass().getSimpleName())
                .containsExactly("DesiredRuntimeCatalogUpsertItem", "DesiredRuntimeCatalogRemovalItem",
                        "DesiredRuntimeCatalogRemovalItem", "DesiredRuntimeCatalogRemovalItem");
        assertThat(response.getPage().getReturned()).isEqualTo(4);
        assertThat(response.getSourceCheckpoint()).isNull();
        assertThat(response.getNextCheckpoint()).isNull();
    }

    private DesiredRuntimeCatalogRow row(String id, String action, String status) {
        return new DesiredRuntimeCatalogRow(1, "RTCH", id, action, status,
                OffsetDateTime.parse("2026-06-30T14:00:00Z"), action.equals("UPSERT") ? "RTPK" : null,
                action.equals("UPSERT") ? 1 : 0, action.equals("UPSERT") ? "a".repeat(64) : null,
                action.equals("UPSERT") ? id : null, action.equals("UPSERT") ? 1 : 0,
                action.equals("UPSERT") ? "SUB" : null, action.equals("UPSERT") ? "a".repeat(64) : null,
                action.equals("UPSERT") ? Map.of("submissionId", "SUB") : null);
    }
    private DesiredRuntimeCatalogAgentState state(String id, String status) {
        return new DesiredRuntimeCatalogAgentState(id, status, OffsetDateTime.parse("2026-06-30T14:00:00Z"));
    }
}
