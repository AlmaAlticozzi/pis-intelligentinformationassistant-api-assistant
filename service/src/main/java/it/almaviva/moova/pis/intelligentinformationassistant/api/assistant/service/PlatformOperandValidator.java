package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.PlatformNormalizer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public final class PlatformOperandValidator {

    private final PlatformNormalizer platformNormalizer;

    public PlatformOperandValidator() {
        this(new PlatformNormalizer());
    }

    PlatformOperandValidator(PlatformNormalizer platformNormalizer) {
        this.platformNormalizer = platformNormalizer;
    }

    public boolean isHumanPlatformValue(Object value) {
        return value instanceof String text
                && !text.isBlank()
                && platformNormalizer.normalize(text).hasNumber();
    }

    public boolean isHumanPlatformValuesArray(Object values) {
        return values instanceof List<?> platforms
                && !platforms.isEmpty()
                && platforms.stream().allMatch(this::isHumanPlatformValue);
    }

    public Optional<Integer> exactInt(Object value) {
        if (!(value instanceof Number number)) {
            return Optional.empty();
        }
        try {
            if (number instanceof Integer integer) {
                return Optional.of(integer);
            }
            if (number instanceof Short || number instanceof Byte) {
                return Optional.of(number.intValue());
            }
            if (number instanceof Long longValue) {
                return Optional.of(Math.toIntExact(longValue));
            }
            if (number instanceof BigInteger bigInteger) {
                return Optional.of(bigInteger.intValueExact());
            }
            if (number instanceof BigDecimal bigDecimal) {
                return Optional.of(bigDecimal.intValueExact());
            }
            if (number instanceof Double doubleValue && !Double.isFinite(doubleValue)) {
                return Optional.empty();
            }
            if (number instanceof Float floatValue && !Float.isFinite(floatValue)) {
                return Optional.empty();
            }
            return Optional.of(new BigDecimal(number.toString()).intValueExact());
        } catch (ArithmeticException | NumberFormatException exception) {
            return Optional.empty();
        }
    }

    public boolean isPositiveExactInt(Object value) {
        return exactInt(value).filter(integer -> integer > 0).isPresent();
    }
}
