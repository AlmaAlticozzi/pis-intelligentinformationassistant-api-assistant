package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config;

import io.smallrye.config.ConfigMapping;

/**
 * Application configuration for AI provider selection and use-case defaults.
 */
@ConfigMapping(prefix = "iia.ai")
public interface AiConfiguration {

    String provider();

    TextImprove textImprove();

    interface TextImprove {

        boolean enabled();

        String model();

        Double temperature();

        Integer maxOutputTokens();

        Integer timeoutSeconds();
    }
}
