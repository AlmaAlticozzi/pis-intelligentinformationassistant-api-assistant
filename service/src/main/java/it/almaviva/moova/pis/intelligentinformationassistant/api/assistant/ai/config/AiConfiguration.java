package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Application configuration for AI provider selection and use-case defaults.
 */
@ConfigMapping(prefix = "iia.ai")
public interface AiConfiguration {

    String provider();

    TextImprove textImprove();

    AlertVerify alertVerify();

    interface TextImprove {

        boolean enabled();

        String model();

        Double temperature();

        Integer maxOutputTokens();

        Integer timeoutSeconds();
    }

    interface AlertVerify {

        @WithDefault("true")
        boolean enabled();

        @WithDefault("gpt-4.1-mini")
        String model();

        @WithDefault("0.1")
        Double temperature();

        @WithDefault("2000")
        Integer maxOutputTokens();

        @WithDefault("20")
        Integer timeoutSeconds();
    }
}
