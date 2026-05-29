package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PointResolver {

    private static final double MIN_SCORE = 0.65;
    private static final double AMBIGUOUS_DELTA = 0.08;
    private static final int MAX_CANDIDATES = 10;

    private final PointRegistry registry;
    private final PointNormalizer normalizer;

    public PointResolver() {
        this(new PointRegistry(), new PointNormalizer());
    }

    public PointResolver(PointRegistry registry, PointNormalizer normalizer) {
        this.registry = registry;
        this.normalizer = normalizer;
    }

    public PointResolutionResult resolve(String rawText) {
        String normalizedText = normalizer.normalize(rawText);
        System.out.println("[IIA][POINT_RESOLVER] raw=" + rawText);

        if (normalizedText.isBlank()) {
            PointResolutionResult result = new PointResolutionResult(
                    rawText,
                    normalizedText,
                    PointResolutionStatus.UNRESOLVED,
                    List.of(),
                    0.0,
                    "blank input");
            logResult(result);
            return result;
        }

        List<String> inputTokens = normalizer.tokens(normalizedText);
        List<ScoredPoint> scoredPoints = registry.points().stream()
                .map(point -> score(point, normalizedText, inputTokens))
                .filter(scoredPoint -> scoredPoint.score() >= MIN_SCORE)
                .sorted(Comparator
                        .comparingDouble(ScoredPoint::score).reversed()
                        .thenComparing(ScoredPoint::stopPoint, Comparator.reverseOrder())
                        .thenComparing(scoredPoint -> scoredPoint.point().nameLong())
                        .thenComparing(scoredPoint -> scoredPoint.point().id()))
                .limit(MAX_CANDIDATES)
                .toList();

        if (scoredPoints.isEmpty()) {
            PointResolutionResult result = new PointResolutionResult(
                    rawText,
                    normalizedText,
                    PointResolutionStatus.UNRESOLVED,
                    List.of(),
                    0.0,
                    "no candidate above threshold");
            logResult(result);
            return result;
        }

        PointResolutionStatus status = resolveStatus(scoredPoints);
        ScoredPoint bestPoint = scoredPoints.get(0);
        List<PointCandidate> candidates = scoredPoints.stream()
                .map(scoredPoint -> toCandidate(scoredPoint, status == PointResolutionStatus.RESOLVED
                        && scoredPoint == bestPoint))
                .toList();

        PointResolutionResult result = new PointResolutionResult(
                rawText,
                normalizedText,
                status,
                candidates,
                bestPoint.score(),
                null);
        logResult(result);
        return result;
    }

    private PointResolutionStatus resolveStatus(List<ScoredPoint> scoredPoints) {
        if (scoredPoints.size() == 1) {
            return PointResolutionStatus.RESOLVED;
        }
        double bestScore = scoredPoints.get(0).score();
        double secondScore = scoredPoints.get(1).score();
        if (secondScore >= bestScore - AMBIGUOUS_DELTA) {
            return PointResolutionStatus.RESOLVED_AMBIGUOUS;
        }
        return PointResolutionStatus.RESOLVED;
    }

    private PointCandidate toCandidate(ScoredPoint scoredPoint, boolean selected) {
        NetworkPoint point = scoredPoint.point();
        return new PointCandidate(
                point.id(),
                point.nameLong(),
                point.nameShort(),
                point.transportMode(),
                scoredPoint.score(),
                scoredPoint.matchType(),
                selected);
    }

    private ScoredPoint score(NetworkPoint point, String normalizedText, List<String> inputTokens) {
        Match nameLongMatch = scoreName(normalizedText, inputTokens, point.normalizedNameLong(), point.nameLongTokens());
        Match nameShortMatch = scoreName(normalizedText, inputTokens, point.normalizedNameShort(), point.nameShortTokens());
        Match bestMatch = nameLongMatch.score() >= nameShortMatch.score() ? nameLongMatch : nameShortMatch;
        return new ScoredPoint(point, bestMatch.score(), bestMatch.matchType());
    }

    private Match scoreName(String normalizedText, List<String> inputTokens, String normalizedPointName, List<String> pointTokens) {
        if (normalizedPointName.isBlank()) {
            return Match.none();
        }
        if (normalizedText.equals(normalizedPointName)) {
            return new Match(1.0, PointMatchType.EXACT_NORMALIZED);
        }
        if (sameTokenSet(inputTokens, pointTokens)) {
            return new Match(0.95, PointMatchType.TOKEN_EXACT_ANY_ORDER);
        }
        if (!inputTokens.isEmpty() && pointTokens.containsAll(inputTokens)) {
            return new Match(0.85, PointMatchType.PARTIAL_TOKEN);
        }
        if (normalizedPointName.contains(normalizedText)) {
            return new Match(0.80, PointMatchType.PARTIAL_TOKEN);
        }

        double phraseSimilarity = levenshteinSimilarity(normalizedText, normalizedPointName);
        if (phraseSimilarity >= 0.80) {
            return new Match(Math.min(0.92, phraseSimilarity), PointMatchType.FUZZY_TOKEN);
        }

        return Match.none();
    }

    private boolean sameTokenSet(List<String> first, List<String> second) {
        if (first.isEmpty() || second.isEmpty()) {
            return false;
        }
        Set<String> firstSet = new LinkedHashSet<>(first);
        Set<String> secondSet = new LinkedHashSet<>(second);
        return firstSet.equals(secondSet);
    }

    private double levenshteinSimilarity(String first, String second) {
        if (first.equals(second)) {
            return 1.0;
        }
        int maxLength = Math.max(first.length(), second.length());
        if (maxLength == 0) {
            return 1.0;
        }
        int distance = damerauLevenshteinDistance(first, second);
        return Math.max(0.0, 1.0 - ((double) distance / maxLength));
    }

    private int damerauLevenshteinDistance(String first, String second) {
        int[][] distances = new int[first.length() + 1][second.length() + 1];
        for (int i = 0; i <= first.length(); i++) {
            distances[i][0] = i;
        }
        for (int j = 0; j <= second.length(); j++) {
            distances[0][j] = j;
        }
        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                int substitutionCost = first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1;
                int deletion = distances[i - 1][j] + 1;
                int insertion = distances[i][j - 1] + 1;
                int substitution = distances[i - 1][j - 1] + substitutionCost;
                int best = Math.min(Math.min(deletion, insertion), substitution);
                if (i > 1
                        && j > 1
                        && first.charAt(i - 1) == second.charAt(j - 2)
                        && first.charAt(i - 2) == second.charAt(j - 1)) {
                    best = Math.min(best, distances[i - 2][j - 2] + 1);
                }
                distances[i][j] = best;
            }
        }
        return distances[first.length()][second.length()];
    }

    private void logResult(PointResolutionResult result) {
        System.out.println("[IIA][POINT_RESOLVER] status="
                + result.status()
                + " candidates="
                + result.candidates().size());
    }

    private record Match(double score, PointMatchType matchType) {
        static Match none() {
            return new Match(0.0, PointMatchType.NONE);
        }
    }

    private record ScoredPoint(NetworkPoint point, double score, PointMatchType matchType) {
        boolean stopPoint() {
            return point.stopPoint();
        }
    }
}
