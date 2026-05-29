package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AlertLocationResolverService {

    private final PointResolver pointResolver;

    public AlertLocationResolverService() {
        this(new PointResolver());
    }

    public AlertLocationResolverService(PointResolver pointResolver) {
        this.pointResolver = pointResolver;
    }

    public List<AlertLocationResolution> resolve(List<AlertLocationMention> mentions) {
        if (mentions == null || mentions.isEmpty()) {
            return List.of();
        }
        return mentions.stream()
                .map(this::resolve)
                .toList();
    }

    private AlertLocationResolution resolve(AlertLocationMention mention) {
        PointResolutionResult result = pointResolver.resolve(mention.rawText());
        List<String> selectedPointIds = selectedPointIds(result);
        boolean fallbackToNameLong = result.status() == PointResolutionStatus.UNRESOLVED;
        double confidenceImpact = fallbackToNameLong ? 0.0 : mention.confidence() * result.bestScore();

        System.out.println("[IIA][LOCATION_RESOLUTION] raw="
                + mention.rawText()
                + " role="
                + mention.semanticRole()
                + " status="
                + result.status()
                + " ids="
                + selectedPointIds);

        return new AlertLocationResolution(
                mention,
                result,
                selectedPointIds,
                fallbackToNameLong,
                confidenceImpact);
    }

    private List<String> selectedPointIds(PointResolutionResult result) {
        if (result.status() == PointResolutionStatus.UNRESOLVED) {
            return List.of();
        }
        if (result.status() == PointResolutionStatus.RESOLVED_AMBIGUOUS) {
            return result.candidates().stream()
                    .map(PointCandidate::id)
                    .toList();
        }
        return result.candidates().stream()
                .filter(PointCandidate::selected)
                .map(PointCandidate::id)
                .toList();
    }
}
