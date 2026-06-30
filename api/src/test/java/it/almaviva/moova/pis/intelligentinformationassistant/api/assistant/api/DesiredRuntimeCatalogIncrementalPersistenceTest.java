package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(DesiredRuntimeCatalogFullTestProfile.class)
class DesiredRuntimeCatalogIncrementalPersistenceTest {
    @Inject DesiredRuntimeCatalogRepository repository;

    @Test void allIncrementalQueryVariantsExecuteAgainstPostgresql() {
        long upper = repository.findCurrentUpperSequence("INCREMENTAL");
        var checkpointRows = repository.findIncrementalPage(0L, null, upper, null, null, 101);
        assertThat(checkpointRows).isNotNull();
        if (!checkpointRows.isEmpty()) {
            var last = checkpointRows.getLast();
            assertThat(repository.findIncrementalPage(0L, null, upper,
                    last.sourceUpdatedAt(), last.agentDefinitionId(), 101)).isNotNull();
        }
        OffsetDateTime lowerTime = OffsetDateTime.parse("2000-01-01T00:00:00Z");
        var timestampRows = repository.findIncrementalPage(null, lowerTime, upper, null, null, 101);
        assertThat(timestampRows).isNotNull();
        if (!timestampRows.isEmpty()) {
            var last = timestampRows.getLast();
            assertThat(repository.findIncrementalPage(null, lowerTime, upper,
                    last.sourceUpdatedAt(), last.agentDefinitionId(), 101)).isNotNull();
        }
    }
}
