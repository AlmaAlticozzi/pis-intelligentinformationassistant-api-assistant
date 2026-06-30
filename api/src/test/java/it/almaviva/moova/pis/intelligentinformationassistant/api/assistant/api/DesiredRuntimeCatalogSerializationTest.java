package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeAgentSubmission;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogPage;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogRemovalItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogRemovalReason;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogUpsertItem;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(DesiredRuntimeCatalogFullTestProfile.class)
class DesiredRuntimeCatalogSerializationTest {

    @Inject ObjectMapper objectMapper;

    @Test
    void upsertActionIsSerializedExactlyOnce() throws Exception {
        String json = objectMapper.writeValueAsString(upsert());

        assertThat(objectMapper.readTree(json).get("action").asText()).isEqualTo("UPSERT");
        assertThat(rootFieldCount(json, "action")).isEqualTo(1);
    }

    @Test
    void removalActionIsSerializedExactlyOnce() throws Exception {
        String json = objectMapper.writeValueAsString(removal());

        assertThat(objectMapper.readTree(json).get("action").asText()).isEqualTo("REMOVE");
        assertThat(rootFieldCount(json, "action")).isEqualTo(1);
        assertThat(objectMapper.readValue(json, DesiredRuntimeCatalogRemovalItem.class).getAction())
                .isEqualTo(DesiredRuntimeCatalogRemovalItem.ActionEnum.REMOVE);
    }

    @Test
    void fullResponseContainsOneActionAndStructuredRuntimePackage() throws Exception {
        DesiredRuntimeCatalogResponse response = new DesiredRuntimeCatalogResponse()
                .catalogVersion("iia.desired-runtime-catalog/v1")
                .mode(DesiredRuntimeCatalogMode.FULL)
                .generatedAt(OffsetDateTime.parse("2026-06-30T13:00:00Z"))
                .catalogAsOf(OffsetDateTime.parse("2026-06-30T13:00:00Z"))
                .items(items(upsert()))
                .page(new DesiredRuntimeCatalogPage().limit(100).returned(1).hasMore(false));

        String json = objectMapper.writeValueAsString(response);
        var item = objectMapper.readTree(json).get("items").get(0);

        assertThat(item.get("action").asText()).isEqualTo("UPSERT");
        assertThat(item.get("runtimePackage").isObject()).isTrue();
        assertThat(item.get("runtimePackage").get("submissionId").asText()).isEqualTo("SUB-PERSISTED");
        assertThat(rootFieldCount(item.toString(), "action")).isEqualTo(1);
    }

    @Test
    void targetedMixedResponseSerializesEachDiscriminatorOnce() throws Exception {
        DesiredRuntimeCatalogResponse response = new DesiredRuntimeCatalogResponse()
                .catalogVersion("iia.desired-runtime-catalog/v1").mode(DesiredRuntimeCatalogMode.TARGETED)
                .generatedAt(OffsetDateTime.parse("2026-06-30T13:00:00Z"))
                .catalogAsOf(OffsetDateTime.parse("2026-06-30T13:00:00Z"))
                .items(items(List.of(upsert(), removal())))
                .page(new DesiredRuntimeCatalogPage().limit(2).returned(2).hasMore(false));

        String json = objectMapper.writeValueAsString(response);
        var serializedItems = objectMapper.readTree(json).get("items");
        assertThat(serializedItems.get(0).get("action").asText()).isEqualTo("UPSERT");
        assertThat(serializedItems.get(1).get("action").asText()).isEqualTo("REMOVE");
        assertThat(rootFieldCount(serializedItems.get(0).toString(), "action")).isEqualTo(1);
        assertThat(rootFieldCount(serializedItems.get(1).toString(), "action")).isEqualTo(1);
    }

    @Test
    void incrementalMixedResponsePreservesCheckpointFieldsAndSingleActions() throws Exception {
        DesiredRuntimeCatalogResponse response = new DesiredRuntimeCatalogResponse()
                .catalogVersion("iia.desired-runtime-catalog/v1").mode(DesiredRuntimeCatalogMode.INCREMENTAL)
                .generatedAt(OffsetDateTime.parse("2026-06-30T13:00:00Z"))
                .catalogAsOf(OffsetDateTime.parse("2026-06-30T13:00:00Z"))
                .sourceCheckpoint("source-checkpoint").nextCheckpoint("next-checkpoint")
                .items(items(List.of(upsert(), removal())))
                .page(new DesiredRuntimeCatalogPage().limit(2).returned(2).hasMore(false));
        String json = objectMapper.writeValueAsString(response);
        var tree = objectMapper.readTree(json);
        assertThat(tree.get("sourceCheckpoint").asText()).isEqualTo("source-checkpoint");
        assertThat(tree.get("nextCheckpoint").asText()).isEqualTo("next-checkpoint");
        assertThat(rootFieldCount(tree.get("items").get(0).toString(), "action")).isEqualTo(1);
        assertThat(rootFieldCount(tree.get("items").get(1).toString(), "action")).isEqualTo(1);
        assertThat(tree.get("items").get(0).get("runtimePackage").isObject()).isTrue();
        assertThat(tree.get("items").get(1).hasNonNull("runtimePackage")).isFalse();
    }

    private DesiredRuntimeCatalogUpsertItem upsert() {
        DesiredRuntimeAgentSubmission runtimePackage = new DesiredRuntimeAgentSubmission()
                .submissionId("SUB-PERSISTED").packageVersion(1L)
                .desiredStatus(DesiredRuntimeAgentSubmission.DesiredStatusEnum.ACTIVE);
        return new DesiredRuntimeCatalogUpsertItem()
                .action(DesiredRuntimeCatalogUpsertItem.ActionEnum.UPSERT)
                .agentDefinitionId("AGDF_TEST")
                .sourceStatus(DesiredRuntimeCatalogUpsertItem.SourceStatusEnum.ACTIVE)
                .sourceUpdatedAt(OffsetDateTime.parse("2026-06-30T12:00:00Z"))
                .packageFingerprint("a".repeat(64)).runtimePackage(runtimePackage);
    }

    private DesiredRuntimeCatalogRemovalItem removal() {
        return new DesiredRuntimeCatalogRemovalItem()
                .action(DesiredRuntimeCatalogRemovalItem.ActionEnum.REMOVE)
                .agentDefinitionId("AGDF_TEST")
                .removalReason(DesiredRuntimeCatalogRemovalReason.NOT_ACTIVE)
                .evaluatedAt(OffsetDateTime.parse("2026-06-30T12:00:00Z"));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<DesiredRuntimeCatalogItem> items(DesiredRuntimeCatalogUpsertItem item) {
        return (List) new ArrayList<>(List.of(item));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<DesiredRuntimeCatalogItem> items(List<?> items) {
        return (List) new ArrayList<>(items);
    }

    private int rootFieldCount(String json, String fieldName) throws Exception {
        int count = 0;
        int depth = 0;
        try (JsonParser parser = objectMapper.createParser(json)) {
            while (parser.nextToken() != null) {
                if (parser.currentToken() == JsonToken.START_OBJECT) depth++;
                else if (parser.currentToken() == JsonToken.END_OBJECT) depth--;
                else if (depth == 1 && parser.currentToken() == JsonToken.FIELD_NAME
                        && fieldName.equals(parser.currentName())) count++;
            }
        }
        return count;
    }
}
