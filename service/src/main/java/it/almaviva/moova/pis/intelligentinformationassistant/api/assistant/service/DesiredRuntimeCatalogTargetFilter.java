package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;

final class DesiredRuntimeCatalogTargetFilter {
    private DesiredRuntimeCatalogTargetFilter() { }

    static List<String> sorted(Collection<String> ids) {
        return ids.stream().sorted().toList();
    }

    static String fingerprint(List<String> sortedIds) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String id : sortedIds) {
                byte[] value = id.getBytes(StandardCharsets.UTF_8);
                digest.update(Integer.toString(value.length).getBytes(StandardCharsets.US_ASCII));
                digest.update((byte) ':');
                digest.update(value);
                digest.update((byte) '|');
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception ex) {
            throw new IllegalStateException("TARGETED filter fingerprint calculation failed.", ex);
        }
    }
}
