package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

public record NormalizedPlatform(
        String rawValue,
        String normalizedText,
        Integer number,
        String suffix,
        boolean malformed,
        boolean unknown) {

    public boolean hasNumber() {
        return number != null;
    }

    public boolean hasLetterSuffix() {
        return suffix != null;
    }
}
