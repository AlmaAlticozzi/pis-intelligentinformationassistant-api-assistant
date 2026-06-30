package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;

final class DesiredRuntimeCatalogIncrementalFilter {
    private DesiredRuntimeCatalogIncrementalFilter() { }

    static String checkpoint(DesiredRuntimeCatalogCheckpoint value) {
        return digest("CHECKPOINT", Integer.toString(value.version()), Long.toString(value.changeSequence()),
                value.catalogAsOf().toInstant().toString());
    }

    static String changedAfter(OffsetDateTime value) {
        return digest("CHANGED_AFTER", value.toInstant().toString());
    }

    private static String digest(String... fields) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String field : fields) {
                byte[] bytes = field.getBytes(StandardCharsets.UTF_8);
                digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.US_ASCII));
                digest.update((byte) ':');
                digest.update(bytes);
                digest.update((byte) '|');
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception ex) {
            throw new IllegalStateException("INCREMENTAL filter fingerprint calculation failed.", ex);
        }
    }
}
