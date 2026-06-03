package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.List;

public record AlertVerificationLocationContext(
        boolean hasLocationMentions,
        List<LocationResolution> resolutions,
        List<NonLocationConstraint> nonLocationConstraints,
        List<String> warnings) {

    public AlertVerificationLocationContext {
        resolutions = resolutions == null ? List.of() : List.copyOf(resolutions);
        nonLocationConstraints = nonLocationConstraints == null ? List.of() : List.copyOf(nonLocationConstraints);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public AlertVerificationLocationContext(boolean hasLocationMentions, List<LocationResolution> resolutions) {
        this(hasLocationMentions, resolutions, List.of(), List.of());
    }

    public static AlertVerificationLocationContext empty() {
        return new AlertVerificationLocationContext(false, List.of(), List.of(), List.of());
    }

    public boolean hasResolvedLocations() {
        return resolutions.stream()
                .anyMatch(resolution -> "RESOLVED".equals(resolution.status())
                        || "RESOLVED_AMBIGUOUS".equals(resolution.status()));
    }

    public boolean hasUnresolvedLocations() {
        return resolutions.stream()
                .anyMatch(resolution -> "UNRESOLVED".equals(resolution.status()));
    }

    public String compactPromptSection() {
        StringBuilder section = new StringBuilder("PIS location resolution:\n");
        if (!hasLocationMentions || resolutions.isEmpty()) {
            section.append("- No PIS location mentions were detected in the user prompt.\n");
            section.append("- If the user did not mention a location, do not add any location condition.\n");
            section.append("\nRules:\n");
            appendRules(section);
            appendFewShotExamples(section);
            return section.toString();
        }

        section.append("Resolved PIS locations:\n");
        for (LocationResolution resolution : resolutions) {
            section.append("- rawText: \"").append(nullToEmpty(resolution.rawText())).append("\"\n");
            if (!nullToEmpty(resolution.normalizedText()).isBlank()) {
                section.append("  normalizedText: \"").append(nullToEmpty(resolution.normalizedText())).append("\"\n");
            }
            section.append("  semanticRole: ").append(nullToEmpty(resolution.semanticRole())).append("\n");
            section.append("  relationToMainEvent: ").append(nullToEmpty(resolution.relationToMainEvent())).append("\n");
            section.append("  requiredCoverage: ").append(resolution.requiredCoverage()).append("\n");
            section.append("  polarity: ").append(nullToEmpty(resolution.polarity())).append("\n");
            section.append("  logicalGroup: ").append(nullToEmpty(resolution.logicalGroup())).append("\n");
            section.append("  understandingConfidence: ").append(resolution.understandingConfidence()).append("\n");
            section.append("  status: ").append(nullToEmpty(resolution.status())).append("\n");
            section.append("  selectedPointIds: ").append(resolution.selectedPointIds()).append("\n");
            section.append("  targetFieldHints: ").append(resolution.targetFieldHints()).append("\n");
            if (resolution.candidates().isEmpty()) {
                section.append("  candidates: []\n");
            } else {
                section.append("  candidates:\n");
                for (LocationCandidate candidate : resolution.candidates()) {
                    section.append("    - id: ").append(nullToEmpty(candidate.id())).append("\n");
                    section.append("      nameLong: ").append(nullToEmpty(candidate.nameLong())).append("\n");
                    section.append("      score: ").append(candidate.score()).append("\n");
                    section.append("      selected: ").append(candidate.selected()).append("\n");
                }
            }
            if (resolution.fallbackToNameLong()) {
                if ("EXCLUDE".equalsIgnoreCase(resolution.polarity())) {
                    section.append("  fallback: use nameLong/nameShort NOT_CONTAINS_NORMALIZED with rawText and lower confidence when catalog-supported\n");
                    if ("DESTINATION_LOCATION".equalsIgnoreCase(resolution.semanticRole())) {
                        section.append("  canonicalNegativeFallback: timetabledCallEnd.stopPoint.nameLong NOT_CONTAINS_NORMALIZED with rawText; do not use any/OR across alternative destination name fields\n");
                    }
                } else {
                    section.append("  fallback: use nameLong/nameShort CONTAINS_NORMALIZED with rawText and lower confidence when catalog-supported\n");
                }
            }
            if (!nullToEmpty(resolution.warningReason()).isBlank()) {
                section.append("  warning: ").append(resolution.warningReason()).append("\n");
            }
        }
        if (!nonLocationConstraints.isEmpty()) {
            section.append("\nRecognized non-location constraints:\n");
            for (NonLocationConstraint constraint : nonLocationConstraints) {
                section.append("- type: ").append(nullToEmpty(constraint.type()))
                        .append(", rawText: \"").append(nullToEmpty(constraint.rawText())).append("\"\n");
            }
        }
        if (!warnings.isEmpty()) {
            section.append("\nLocation understanding warnings:\n");
            for (String warning : warnings) {
                section.append("- ").append(nullToEmpty(warning)).append("\n");
            }
        }
        section.append("\nRules:\n");
        appendRules(section);
        appendFewShotExamples(section);
        return section.toString();
    }

    private static void appendRules(StringBuilder section) {
        section.append("- Locations were already semantically understood by the backend and resolved against points.json when possible.\n");
        section.append("- Every location with requiredCoverage=true must be represented in technicalSpecification.\n");
        section.append("- The location role determines which ServiceData field/path should be used.\n");
        section.append("- Do not put every location on the current stop.\n");
        section.append("- MAIN_EVENT_LOCATION uses current/event stop fields.\n");
        section.append("- For MAIN_EVENT_LOCATION use only payload.stopPointJourney.stopPoint.id/nameLong/nameShort or payload.ongroundServiceEvent.stopPoint.id/nameLong/nameShort.\n");
        section.append("- \"partenza da X\", \"parte da X\", \"arrivo a X\" and \"arriva a X\" mean X is the current/event stop when Location Understanding marked X as MAIN_EVENT_LOCATION.\n");
        section.append("- Do not use callStart/timetabledCallStart for \"parte da X\" when X is MAIN_EVENT_LOCATION.\n");
        section.append("- Do not use callEnd/timetabledCallEnd for \"arriva a X\" when X is MAIN_EVENT_LOCATION.\n");
        section.append("- ORIGIN_LOCATION uses origin/callStart fields.\n");
        section.append("- Use callStart/timetabledCallStart only for ORIGIN_LOCATION or explicit journey-origin wording such as \"origine\", \"localita di origine\", \"ha origine\" or \"corsa con origine X\".\n");
        section.append("- DESTINATION_LOCATION uses destination/callEnd fields.\n");
        section.append("- Use callEnd/timetabledCallEnd only for DESTINATION_LOCATION or explicit journey-destination wording such as \"destinazione\", \"destino\" or \"corsa con destinazione X\".\n");
        section.append("- ROUTE_OR_NEXT_CALL_LOCATION uses nextCalls fields.\n");
        section.append("- TRANSIT_LOCATION uses nextTransitCalls, or nextCalls with passingType TRANSIT only when supported.\n");
        section.append("- CANCELLED_CALL_LOCATION uses nextCancelledCalls fields.\n");
        section.append("- REPLACEMENT_LOCATION uses replacement.stopPointReplacements fields.\n");
        section.append("- If a required location or role needs data absent from the catalog, return REJECTED; do not silently ignore it.\n");
        section.append("- Non-location constraints such as platform/binario/track/quay are not locations; handle them through the ServiceData catalog.\n");
        section.append("- RESOLVED with one selected candidate -> use EQUALS on the correct stopPoint.id field.\n");
        section.append("- RESOLVED_AMBIGUOUS with multiple selected candidates -> use IN on the correct stopPoint.id field.\n");
        section.append("- For polarity=EXCLUDE and RESOLVED/RESOLVED_AMBIGUOUS locations, use NOT_IN on the correct stopPoint.id field with the resolved ids.\n");
        section.append("- For polarity=EXCLUDE and UNRESOLVED locations, use NOT_CONTAINS_NORMALIZED on the correct nameLong/nameShort field when the catalog supports it; lower confidence and add a warning.\n");
        section.append("- For DESTINATION_LOCATION with polarity=EXCLUDE and status=UNRESOLVED, prefer one canonical fallback: timetabledCallEnd.stopPoint.nameLong NOT_CONTAINS_NORMALIZED with rawText.\n");
        section.append("- Do not create any/OR branches across timetabledCallEnd/callEnd/nameLong/nameShort negative fallback alternatives; one negative textual fallback field is enough for this MVP.\n");
        section.append("- Use NOT_EQUALS_NORMALIZED only when the user explicitly requires exact normalized inequality and the catalog supports it.\n");
        section.append("- Never use NOT_EQUAL or NOT_EQUALS on stopPoint.nameLong/nameShort unless that exact operator is listed in the catalog for that exact field.\n");
        section.append("- UNRESOLVED INCLUDE -> fallback to nameLong CONTAINS_NORMALIZED and lower confidence.\n");
        section.append("- UNRESOLVED EXCLUDE -> fallback to nameLong/nameShort NOT_CONTAINS_NORMALIZED and lower confidence when catalog-supported.\n");
        section.append("- NO LOCATION -> do not add any location filter.\n");
        section.append("- If a location has one resolved candidate, use stopPoint.id with EQUALS.\n");
        section.append("- If a location has multiple selected candidates, use stopPoint.id with IN.\n");
        section.append("- Do not use stopPoint.nameLong/nameShort for resolved locations.\n");
        section.append("- If an INCLUDE location is unresolved, use nameLong CONTAINS_NORMALIZED as fallback and lower confidence.\n");
        section.append("- Never invent stopPoint ids.\n");
        section.append("- Never use stopPoint ids not listed in the resolved candidates section.\n");
        section.append("- If the user did not mention a location, do not add any location condition.\n");
    }

    private static void appendFewShotExamples(StringBuilder section) {
        section.append("\nMinimal examples:\n");
        section.append("- Positive resolved single candidate:\n");
        section.append("  User prompt: \"Avvertimi quando una corsa parte da Rho Fieramilano\"\n");
        section.append("  Resolved location: Rho Fieramilano -> TNPNTS00000000005467\n");
        section.append("  Expected condition leaves:\n");
        section.append("    payload.ongroundServiceEvent.eventsType CONTAINS DEPARTED\n");
        section.append("    payload.ongroundServiceEvent.stopPoint.id EQUALS TNPNTS00000000005467\n");
        section.append("  requirementCoverage: location constraint mappedBy [payload.ongroundServiceEvent.stopPoint.id]\n");
        section.append("- Positive resolved multiple candidates:\n");
        section.append("  User prompt: \"Avvertimi quando una corsa parte da Malpensa\"\n");
        section.append("  Resolved locations: Malpensa -> TNPNTS00000000000028, TNPNTS00000000000029\n");
        section.append("  Expected condition leaf:\n");
        section.append("    payload.ongroundServiceEvent.stopPoint.id IN [TNPNTS00000000000028, TNPNTS00000000000029]\n");
        section.append("  requirementCoverage: location constraint mappedBy [payload.ongroundServiceEvent.stopPoint.id]\n");
        section.append("- Fallback unresolved location:\n");
        section.append("  User prompt: \"Avvertimi quando un treno passa da Genova Nervi\"\n");
        section.append("  Location unresolved.\n");
        section.append("  Expected condition may use nameLong CONTAINS_NORMALIZED \"Genova Nervi\".\n");
        section.append("  Expected warning and low confidence; requirementCoverage must mention fallback text matching.\n");
        section.append("- Negative resolved location:\n");
        section.append("  If a location was resolved, do not emit payload.ongroundServiceEvent.stopPoint.nameLong CONTAINS_NORMALIZED \"Rho Fieramilano\".\n");
        section.append("- Negative unresolved excluded destination:\n");
        section.append("  User prompt: \"La corsa non deve avere come destinazione Bologna\"\n");
        section.append("  Location Bologna has polarity=EXCLUDE and status=UNRESOLVED.\n");
        section.append("  Do not generate timetabledCallEnd.stopPoint.nameLong NOT_EQUAL \"Bologna\" unless NOT_EQUAL is explicitly allowed by the catalog.\n");
        section.append("  Expected fallback when catalog-supported: timetabledCallEnd.stopPoint.nameLong NOT_CONTAINS_NORMALIZED \"Bologna\".\n");
        section.append("  Do not generate any/OR between timetabledCallEnd.stopPoint.nameLong, timetabledCallEnd.stopPoint.nameShort, callEnd.stopPoint.nameLong and callEnd.stopPoint.nameShort.\n");
        section.append("  Expected warning and lower confidence; requirementCoverage must mention negative fallback text matching.\n");
        section.append("- Positive current departure plus future route location:\n");
        section.append("  User prompt: \"Dimmi quando una corsa parte da Garibaldi e passera da Venezia\"\n");
        section.append("  LocationContext: Garibaldi MAIN_EVENT_LOCATION; Venezia ROUTE_OR_NEXT_CALL_LOCATION.\n");
        section.append("  Expected condition leaves:\n");
        section.append("    payload.ongroundServiceEvent.eventsType CONTAINS DEPARTED\n");
        section.append("    payload.stopPointJourney.stopPoint.id or payload.ongroundServiceEvent.stopPoint.id for Garibaldi\n");
        section.append("    nextCalls[].stopPoint.id for Venezia inside stopPointsJourneyDetails[] anyElement.\n");
        section.append("  Do not put Garibaldi on callStart.stopPoint.id or timetabledCallStart.stopPoint.id.\n");
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record LocationResolution(
            String rawText,
            String normalizedText,
            String semanticRole,
            String relationToMainEvent,
            boolean requiredCoverage,
            String polarity,
            String logicalGroup,
            double understandingConfidence,
            String status,
            List<LocationCandidate> candidates,
            List<String> selectedPointIds,
            boolean fallbackToNameLong,
            boolean fallbackAllowed,
            double confidenceImpact,
            String warningReason,
            List<String> targetFieldHints) {

        public LocationResolution {
            rawText = nullToEmpty(rawText);
            normalizedText = nullToEmpty(normalizedText);
            semanticRole = nullToEmpty(semanticRole);
            relationToMainEvent = nullToEmpty(relationToMainEvent);
            polarity = nullToEmpty(polarity);
            logicalGroup = nullToEmpty(logicalGroup);
            status = nullToEmpty(status);
            candidates = candidates == null ? List.of() : List.copyOf(candidates);
            selectedPointIds = selectedPointIds == null ? List.of() : List.copyOf(selectedPointIds);
            warningReason = nullToEmpty(warningReason);
            targetFieldHints = targetFieldHints == null ? List.of() : List.copyOf(targetFieldHints);
        }

        public LocationResolution(
                String rawText,
                String semanticRole,
                String status,
                List<LocationCandidate> candidates,
                boolean fallbackToNameLong,
                double confidenceImpact) {
            this(
                    rawText,
                    "",
                    semanticRole,
                    "",
                    true,
                    "INCLUDE",
                    "",
                    0.0,
                    status,
                    candidates,
                    List.of(),
                    fallbackToNameLong,
                    fallbackToNameLong,
                    confidenceImpact,
                    "",
                    List.of());
        }
    }

    public record LocationCandidate(
            String id,
            String nameLong,
            String nameShort,
            String transportMode,
            double score,
            String matchType,
            boolean selected) {
    }

    public record NonLocationConstraint(
            String type,
            String rawText) {

        public NonLocationConstraint {
            type = nullToEmpty(type);
            rawText = nullToEmpty(rawText);
        }
    }
}
