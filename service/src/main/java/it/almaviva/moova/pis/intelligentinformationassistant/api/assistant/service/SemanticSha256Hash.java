package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SemanticSha256Hash {
    private static final Pattern VALUE = Pattern.compile("^(?:(sha-?256):)?([0-9a-f]{64})$", Pattern.CASE_INSENSITIVE);

    private SemanticSha256Hash() { }

    static String digest(String value) {
        if (value == null) throw invalid();
        Matcher matcher = VALUE.matcher(value.trim());
        if (!matcher.matches()) throw invalid();
        if (matcher.group(1) != null && !matcher.group(1).replace("-", "").equalsIgnoreCase("sha256")) throw invalid();
        return matcher.group(2).toLowerCase(Locale.ROOT);
    }

    static boolean equal(String left, String right) {
        return digest(left).equals(digest(right));
    }

    private static AgentRuntimePackageBuildException invalid() {
        return new AgentRuntimePackageBuildException("SHA-256 hash is structurally invalid or uses an unsupported algorithm.");
    }
}
