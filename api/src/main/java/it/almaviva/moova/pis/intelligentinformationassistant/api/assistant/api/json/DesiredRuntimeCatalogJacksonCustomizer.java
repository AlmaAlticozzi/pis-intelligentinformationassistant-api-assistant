package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogRemovalItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogUpsertItem;
import jakarta.inject.Singleton;

@Singleton
public class DesiredRuntimeCatalogJacksonCustomizer implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper objectMapper) {
        objectMapper.addMixIn(DesiredRuntimeCatalogItem.class, ExistingActionDiscriminatorMixin.class);
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "action",
            visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = DesiredRuntimeCatalogRemovalItem.class, name = "REMOVE"),
            @JsonSubTypes.Type(value = DesiredRuntimeCatalogUpsertItem.class, name = "UPSERT")
    })
    private abstract static class ExistingActionDiscriminatorMixin { }
}
