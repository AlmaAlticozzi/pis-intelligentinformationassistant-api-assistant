package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import java.util.List;

record NetworkPoint(
        String id,
        String nameLong,
        String nameShort,
        String transportMode,
        boolean stopPoint,
        String normalizedNameLong,
        String normalizedNameShort,
        List<String> nameLongTokens,
        List<String> nameShortTokens) {
}
