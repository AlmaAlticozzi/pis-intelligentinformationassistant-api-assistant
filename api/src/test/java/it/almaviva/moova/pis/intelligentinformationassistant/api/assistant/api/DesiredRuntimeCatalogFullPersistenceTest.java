package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(DesiredRuntimeCatalogFullTestProfile.class)
class DesiredRuntimeCatalogFullPersistenceTest {

    @Inject
    DesiredRuntimeCatalogRepository repository;

    @Test
    void fullPageQueryExecutesAgainstPostgresql() {
        long upperSequence = repository.findCurrentUpperSequence();
        var firstPage = repository.findFullSnapshotPage(upperSequence, null, null, 101);

        assertThat(firstPage).isNotNull();
        if (!firstPage.isEmpty()) {
            var last = firstPage.getLast();
            var continuation = repository.findFullSnapshotPage(upperSequence,
                    last.sourceUpdatedAt(), last.agentDefinitionId(), 101);
            assertThat(continuation).isNotNull()
                    .allMatch(row -> row.sourceUpdatedAt().isAfter(last.sourceUpdatedAt())
                            || row.sourceUpdatedAt().isEqual(last.sourceUpdatedAt())
                            && row.agentDefinitionId().compareTo(last.agentDefinitionId()) > 0);
        }
    }
}
