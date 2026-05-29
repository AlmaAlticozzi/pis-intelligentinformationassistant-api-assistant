package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class PointResolverTest {

    private static final String RHO_FIERAMILANO_ID = "TNPNTS00000000005467";
    private static final String MALPENSA_T1_ID = "TNPNTS00000000000028";
    private static final String MALPENSA_T2_ID = "TNPNTS00000000000029";

    private final PointRegistry registry = new PointRegistry();
    private final PointResolver resolver = new PointResolver(registry, new PointNormalizer());

    @Test
    void exactRhoFieramilanoResolvesToExpectedId() {
        PointResolutionResult result = resolver.resolve("Rho Fieramilano");

        assertThat(result.status()).isEqualTo(PointResolutionStatus.RESOLVED);
        assertThat(result.bestScore()).isEqualTo(1.0);
        assertThat(result.candidates()).first()
                .extracting(PointCandidate::id, PointCandidate::matchType, PointCandidate::selected)
                .containsExactly(RHO_FIERAMILANO_ID, PointMatchType.EXACT_NORMALIZED, true);
    }

    @Test
    void tokenOrderFieramilanoRhoResolvesToExpectedId() {
        PointResolutionResult result = resolver.resolve("Fieramilano Rho");

        assertThat(result.status()).isEqualTo(PointResolutionStatus.RESOLVED);
        assertThat(result.candidates()).first()
                .extracting(PointCandidate::id, PointCandidate::matchType)
                .containsExactly(RHO_FIERAMILANO_ID, PointMatchType.TOKEN_EXACT_ANY_ORDER);
        assertThat(result.bestScore()).isGreaterThanOrEqualTo(0.90);
    }

    @Test
    void typoRohFieramlianoResolvesToExpectedId() {
        PointResolutionResult result = resolver.resolve("Roh Fieramliano");

        assertThat(result.status()).isEqualTo(PointResolutionStatus.RESOLVED);
        assertThat(result.candidates()).first()
                .extracting(PointCandidate::id, PointCandidate::matchType)
                .containsExactly(RHO_FIERAMILANO_ID, PointMatchType.FUZZY_TOKEN);
        assertThat(result.bestScore()).isGreaterThan(0.80);
    }

    @Test
    void malpensaReturnsTerminalOneAndTwo() {
        PointResolutionResult result = resolver.resolve("Malpensa");

        List<String> candidateIds = result.candidates().stream()
                .map(PointCandidate::id)
                .toList();

        assertThat(result.status()).isIn(PointResolutionStatus.RESOLVED_AMBIGUOUS, PointResolutionStatus.RESOLVED);
        assertThat(result.candidates()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(candidateIds).contains(MALPENSA_T1_ID, MALPENSA_T2_ID);
    }

    @Test
    void genovaNerviIsUnresolved() {
        PointResolutionResult result = resolver.resolve("Genova Nervi");

        assertThat(result.status()).isEqualTo(PointResolutionStatus.UNRESOLVED);
        assertThat(result.candidates()).isEmpty();
    }

    @Test
    void blankInputIsUnresolved() {
        PointResolutionResult result = resolver.resolve("   ");

        assertThat(result.status()).isEqualTo(PointResolutionStatus.UNRESOLVED);
        assertThat(result.candidates()).isEmpty();
        assertThat(result.fallbackReason()).isEqualTo("blank input");
    }

    @Test
    void resolverDoesNotThrowWhenPointsJsonIsLoaded() {
        assertThatCode(() -> resolver.resolve("Rho Fieramilano"))
                .doesNotThrowAnyException();
        assertThat(registry.size()).isGreaterThan(0);
    }
}
