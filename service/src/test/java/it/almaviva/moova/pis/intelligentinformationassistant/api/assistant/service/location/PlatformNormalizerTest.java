package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformNormalizerTest {

    private final PlatformNormalizer normalizer = new PlatformNormalizer();

    @Test
    void samePlatformRecognizesEquivalentDescriptions() {
        assertThat(normalizer.samePlatform("1", "Platform 1")).isTrue();
        assertThat(normalizer.samePlatform("1", "Binario 1")).isTrue();
        assertThat(normalizer.samePlatform("1", "PL1")).isTrue();
        assertThat(normalizer.samePlatform("01", "Platform 1")).isTrue();
        assertThat(normalizer.samePlatform("3A", "Platform 3A")).isTrue();
        assertThat(normalizer.samePlatform("3 a", "Binario 3A")).isTrue();
    }

    @Test
    void samePlatformDoesNotUsePartialNumberMatches() {
        assertThat(normalizer.samePlatform("1", "Platform 11")).isFalse();
        assertThat(normalizer.samePlatform("1", "Platform 12")).isFalse();
    }

    @Test
    void samePlatformTreatsLetterSuffixAsDiscriminating() {
        assertThat(normalizer.samePlatform("3A", "Platform 3B")).isFalse();
        assertThat(normalizer.samePlatform("3", "Platform 3A")).isFalse();
    }

    @Test
    void samePlatformRejectsBlankAndNullValues() {
        assertThat(normalizer.samePlatform("", "Platform 1")).isFalse();
        assertThat(normalizer.samePlatform("1", " ")).isFalse();
        assertThat(normalizer.samePlatform(null, null)).isFalse();
        assertThat(normalizer.samePlatform(null, "Platform 1")).isFalse();
    }

    @Test
    void extractsPlatformNumber() {
        assertThat(normalizer.extractPlatformNumber("Platform 12")).contains(12);
        assertThat(normalizer.extractPlatformNumber("binario 5A")).contains(5);
        assertThat(normalizer.extractPlatformNumber("foo")).isEmpty();
    }

    @Test
    void evaluatesAdvancedNumericPlatformPropertiesWithoutLosingSuffixSemantics() {
        assertThat(normalizer.isDoubleDigit("Platform 12")).isTrue();
        assertThat(normalizer.extractPlatformNumber("binario 5A")).contains(5);
        assertThat(normalizer.hasLetterSuffix("binario 5A")).isTrue();
        assertThat(normalizer.isEven("Platform 6")).isTrue();
        assertThat(normalizer.isOdd("Platform 7")).isTrue();
        assertThat(normalizer.isMultipleOf("Platform 9", 3)).isTrue();
        assertThat(normalizer.isBetween("Platform 10", 5, 12)).isTrue();
        assertThat(normalizer.isEven("foo")).isFalse();
        assertThat(normalizer.isOdd("foo")).isFalse();
        assertThat(normalizer.isDoubleDigit("foo")).isFalse();
        assertThat(normalizer.isMultipleOf("foo", 3)).isFalse();
        assertThat(normalizer.isBetween("foo", 1, 9)).isFalse();

        assertThat(normalizer.extractPlatformNumber("Platform 3A")).contains(3);
        assertThat(normalizer.isBetween("Platform 3A", 2, 4)).isTrue();
        assertThat(normalizer.samePlatform("3A", "3")).isFalse();
    }

    @Test
    void checksPlatformMembershipUsingSemanticComparison() {
        assertThat(normalizer.inPlatforms("Platform 4", List.of("1", "4"))).isTrue();
        assertThat(normalizer.inPlatforms("Platform 14", List.of("1", "4"))).isFalse();
        assertThat(normalizer.notInPlatforms("Platform 14", List.of("1", "4"))).isTrue();
    }

    @Test
    void supportsSynonymsPrepositionsPunctuationAndOrdinalDescriptions() {
        assertThat(normalizer.samePlatform("1", "sul binario 1")).isTrue();
        assertThat(normalizer.samePlatform("1", "dal binario 1")).isTrue();
        assertThat(normalizer.samePlatform("1", "Track 01")).isTrue();
        assertThat(normalizer.samePlatform("1", "1° binario")).isTrue();
        assertThat(normalizer.samePlatform("3A", "Platform 3-A")).isTrue();
        assertThat(normalizer.samePlatform("2", "banchina 2")).isTrue();
        assertThat(normalizer.samePlatform("2", "banchìna 2")).isTrue();
        assertThat(normalizer.samePlatform("2", "marciapiede 2")).isTrue();
        assertThat(normalizer.samePlatform("2", "quay 2")).isTrue();
        assertThat(normalizer.samePlatform("2", "plat 2")).isTrue();
        assertThat(normalizer.samePlatform("2", "bin. 2")).isTrue();
    }

    @Test
    void exposesNormalizedPlatformDetails() {
        NormalizedPlatform platform = normalizer.normalize("Platform 03-a");

        assertThat(platform.rawValue()).isEqualTo("Platform 03-a");
        assertThat(platform.normalizedText()).isEqualTo("3A");
        assertThat(platform.number()).isEqualTo(3);
        assertThat(platform.suffix()).isEqualTo("A");
        assertThat(platform.hasNumber()).isTrue();
        assertThat(platform.hasLetterSuffix()).isTrue();
        assertThat(platform.malformed()).isFalse();
        assertThat(platform.unknown()).isFalse();
    }

    @Test
    void marksRecognizedPrefixWithoutNumberAsMalformed() {
        NormalizedPlatform platform = normalizer.normalize("Platform foo");

        assertThat(platform.hasNumber()).isFalse();
        assertThat(platform.malformed()).isTrue();
        assertThat(platform.unknown()).isTrue();
    }

    @Test
    void treatsOutOfRangeNumberAsUnknownInsteadOfThrowing() {
        NormalizedPlatform platform = normalizer.normalize("Platform 999999999999999999999999");

        assertThat(platform.hasNumber()).isFalse();
        assertThat(platform.malformed()).isTrue();
        assertThat(platform.unknown()).isTrue();
    }
}
