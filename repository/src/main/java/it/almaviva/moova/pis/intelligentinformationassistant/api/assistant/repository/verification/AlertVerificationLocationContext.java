package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.List;

public record AlertVerificationLocationContext(
        boolean hasLocationMentions,
        List<LocationResolution> resolutions) {

    public AlertVerificationLocationContext {
        resolutions = resolutions == null ? List.of() : List.copyOf(resolutions);
    }

    public static AlertVerificationLocationContext empty() {
        return new AlertVerificationLocationContext(false, List.of());
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
            return section.toString();
        }

        section.append("Resolved PIS locations:\n");
        for (LocationResolution resolution : resolutions) {
            section.append("- rawText: \"").append(nullToEmpty(resolution.rawText())).append("\"\n");
            section.append("  semanticRole: ").append(nullToEmpty(resolution.semanticRole())).append("\n");
            section.append("  status: ").append(nullToEmpty(resolution.status())).append("\n");
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
                section.append("  fallback: use nameLong CONTAINS_NORMALIZED with rawText and lower confidence\n");
            }
        }
        section.append("\nRules:\n");
        appendRules(section);
        return section.toString();
    }

    private static void appendRules(StringBuilder section) {
        section.append("- If a location has one resolved candidate, use stopPoint.id with EQUALS.\n");
        section.append("- If a location has multiple selected candidates, use stopPoint.id with IN.\n");
        section.append("- Do not use stopPoint.nameLong/nameShort for resolved locations.\n");
        section.append("- If a location is unresolved, use nameLong CONTAINS_NORMALIZED as fallback and lower confidence.\n");
        section.append("- Never invent stopPoint ids.\n");
        section.append("- Never use stopPoint ids not listed in the resolved candidates section.\n");
        section.append("- If the user did not mention a location, do not add any location condition.\n");
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record LocationResolution(
            String rawText,
            String semanticRole,
            String status,
            List<LocationCandidate> candidates,
            boolean fallbackToNameLong,
            double confidenceImpact) {

        public LocationResolution {
            candidates = candidates == null ? List.of() : List.copyOf(candidates);
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
}
