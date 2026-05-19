package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.utility;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;

import java.util.regex.Pattern;

/**
 * Validates and normalizes text improvement input and provider output.
 */
@ApplicationScoped
public class TextImproveValidator {

    private static final int INPUT_MAX_LENGTH = 8000;
    private static final String INPUT_NULL_ERROR = "IIA-UTL-TXI-400-001";
    private static final String INPUT_BLANK_ERROR = "IIA-UTL-TXI-400-003";
    private static final String INPUT_TOO_LONG_ERROR = "IIA-UTL-TXI-400-004";
    private static final String EMPTY_OUTPUT_ERROR = "IIA-UTL-TXI-500-001";
    private static final Pattern OUTPUT_PREFIX_PATTERN = Pattern.compile(
            "^(?i)\\s*(ecco\\s+il\\s+testo\\s+corretto|testo\\s+migliorato)\\s*:\\s*");

    public String validateAndNormalizeInput(String inputText) {
        if (inputText == null) {
            throw new BadRequestException(INPUT_NULL_ERROR);
        }

        String normalizedInput = inputText.trim();
        if (normalizedInput.isEmpty()) {
            throw new BadRequestException(INPUT_BLANK_ERROR);
        }
        if (normalizedInput.length() > INPUT_MAX_LENGTH) {
            throw new BadRequestException(INPUT_TOO_LONG_ERROR);
        }

        return normalizedInput;
    }

    public String validateAndNormalizeOutput(String outputText) {
        if (outputText == null) {
            throw new InternalServerErrorException(EMPTY_OUTPUT_ERROR);
        }

        String normalizedOutput = OUTPUT_PREFIX_PATTERN.matcher(outputText).replaceFirst("").trim();
        if (normalizedOutput.isEmpty()) {
            throw new InternalServerErrorException(EMPTY_OUTPUT_ERROR);
        }

        return normalizedOutput;
    }
}
