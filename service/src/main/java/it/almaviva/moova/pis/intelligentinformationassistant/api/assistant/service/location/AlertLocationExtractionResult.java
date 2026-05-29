package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import java.util.List;

public record AlertLocationExtractionResult(
        boolean hasLocationMentions,
        List<AlertLocationMention> mentions,
        List<String> warnings) {

    public static AlertLocationExtractionResult empty() {
        return new AlertLocationExtractionResult(false, List.of(), List.of());
    }
}
