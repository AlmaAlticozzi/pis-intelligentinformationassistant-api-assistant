package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(DesiredRuntimeCatalogFullTestProfile.class)
class DesiredRuntimeCatalogTargetedPersistenceTest {

    @Inject DesiredRuntimeCatalogRepository repository;

    @Test
    void batchQueriesResolveAnIsolatedUnknownIdentifierWithoutWrites() {
        Set<String> ids = Set.of("AGDF_C2_TARGETED_UNKNOWN_TEST");
        long upperSequence = repository.findCurrentUpperSequence();

        assertThat(repository.findLatestCatalogChangesForAgentIds(ids, upperSequence)).isEmpty();
        assertThat(repository.findAgentDefinitionsByIds(ids)).isEmpty();
    }
}
