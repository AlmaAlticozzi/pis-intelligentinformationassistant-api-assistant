package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class PointRegistry {

    private static final String POINTS_RESOURCE = "points.json";

    private final PointNormalizer normalizer;
    private final List<NetworkPoint> points;
    private final Set<String> pointIds;

    public PointRegistry() {
        this(new PointNormalizer(), new ObjectMapper());
    }

    PointRegistry(PointNormalizer normalizer, ObjectMapper objectMapper) {
        this.normalizer = normalizer;
        this.points = Collections.unmodifiableList(loadPoints(objectMapper));
        this.pointIds = points.stream()
                .map(NetworkPoint::id)
                .collect(Collectors.toUnmodifiableSet());
    }

    List<NetworkPoint> points() {
        return points;
    }

    public int size() {
        return points.size();
    }

    public boolean containsId(String id) {
        return id != null && pointIds.contains(id);
    }

    private List<NetworkPoint> loadPoints(ObjectMapper objectMapper) {
        try (InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(POINTS_RESOURCE)) {
            if (stream != null) {
                return parsePoints(objectMapper.readTree(stream));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load classpath resource " + POINTS_RESOURCE, e);
        }

        return loadFromRepositoryLayout(objectMapper);
    }

    private List<NetworkPoint> loadFromRepositoryLayout(ObjectMapper objectMapper) {
        List<Path> candidates = List.of(
                Path.of("api", "src", "main", "resources", POINTS_RESOURCE),
                Path.of("..", "api", "src", "main", "resources", POINTS_RESOURCE),
                Path.of("src", "main", "resources", POINTS_RESOURCE));

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                try (InputStream stream = Files.newInputStream(candidate)) {
                    return parsePoints(objectMapper.readTree(stream));
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to load points file " + candidate.toAbsolutePath(), e);
                }
            }
        }

        throw new IllegalStateException("Unable to find " + POINTS_RESOURCE + " on classpath or repository resources");
    }

    private List<NetworkPoint> parsePoints(JsonNode root) {
        if (!root.isArray()) {
            return List.of();
        }

        List<NetworkPoint> loadedPoints = new ArrayList<>();
        for (JsonNode node : root) {
            String id = text(node, "id");
            if (id.isBlank()) {
                continue;
            }

            String nameLong = text(node, "nameLong");
            String nameShort = text(node, "nameShort");
            String normalizedNameLong = normalizer.normalize(nameLong);
            String normalizedNameShort = normalizer.normalize(nameShort);
            loadedPoints.add(new NetworkPoint(
                    id,
                    nameLong,
                    nameShort,
                    text(node, "transportMode"),
                    node.path("isStopPoint").asBoolean(false),
                    normalizedNameLong,
                    normalizedNameShort,
                    normalizer.tokens(normalizedNameLong),
                    normalizer.tokens(normalizedNameShort)));
        }
        return loadedPoints;
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            return "";
        }
        return field.asText("").trim();
    }
}
