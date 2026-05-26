package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "iia.temporal")
public interface TemporalConfiguration {

    @WithDefault("Europe/Rome")
    String defaultZone();
}
