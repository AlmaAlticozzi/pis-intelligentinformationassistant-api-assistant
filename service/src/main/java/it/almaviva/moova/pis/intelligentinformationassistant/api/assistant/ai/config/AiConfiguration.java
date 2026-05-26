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

    AgentBlueprintGenerate agentBlueprintGenerate();

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

        @WithDefault("5000")
        Integer maxOutputTokens();

        /**
         * Application-level timeout metadata for ALERT_VERIFY. The effective OpenAI HTTP client
         * timeout is controlled by quarkus.langchain4j.openai.timeout.
         */
        @WithDefault("60")
        Integer timeoutSeconds();

        @WithDefault("false")
        boolean simulateProviderTimeout();
    }

    interface AgentBlueprintGenerate {

        @WithDefault("gpt-4.1-mini")
        String model();

        @WithDefault("0.1")
        Double temperature();

        @WithDefault("2500")
        Integer maxOutputTokens();
    }
}
