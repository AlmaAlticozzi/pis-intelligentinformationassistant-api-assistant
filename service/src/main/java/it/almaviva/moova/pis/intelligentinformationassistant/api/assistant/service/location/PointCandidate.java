package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

public record PointCandidate(
        String id,
        String nameLong,
        String nameShort,
        String transportMode,
        double score,
        PointMatchType matchType,
        boolean selected) {
}
