package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.utility;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.validation.AssistantApiInputValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;

import java.util.regex.Pattern;

/**
 * Validates and normalizes text improvement input and provider output.
 */
@ApplicationScoped
public class TextImproveValidator {

    private static final String EMPTY_OUTPUT_ERROR = "IIA-UTL-TXI-500-001";
    private static final Pattern OUTPUT_PREFIX_PATTERN = Pattern.compile(
            "^(?i)\\s*(ecco\\s+il\\s+testo\\s+corretto|testo\\s+migliorato)\\s*:\\s*");

    public String validateAndNormalizeInput(String inputText) {
        return AssistantApiInputValidator.validateTextImproveInput(inputText);
    }

    public String validateAndNormalizeOutput(String outputText) {
        System.out.println("PRIMA " + outputText);
        if (outputText == null) {
            throw new InternalServerErrorException(EMPTY_OUTPUT_ERROR);
        }

        String normalizedOutput = OUTPUT_PREFIX_PATTERN.matcher(outputText).replaceFirst("").trim();
        if (normalizedOutput.isEmpty()) {
            throw new InternalServerErrorException(EMPTY_OUTPUT_ERROR);
        }
        System.out.println("DOPO " + normalizedOutput);
        return normalizedOutput;
    }
}
