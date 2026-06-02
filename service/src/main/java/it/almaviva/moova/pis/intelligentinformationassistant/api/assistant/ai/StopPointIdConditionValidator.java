package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.PointRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class StopPointIdConditionValidator {

    private static final Pattern STOP_POINT_ID_PATTERN = Pattern.compile("^TNPNTS\\d{14}$");

    private final PointRegistry pointRegistry;

    StopPointIdConditionValidator() {
        this(new PointRegistry());
    }

    StopPointIdConditionValidator(PointRegistry pointRegistry) {
        this.pointRegistry = pointRegistry;
    }

    static boolean isStopPointIdField(String field) {
        return field != null && (field.endsWith(".stopPoint.id") || field.endsWith(".stopPointId.id"));
    }

    Result validate(String field, String operator, Map<?, ?> leaf) {
        List<String> ids = extractIds(operator, leaf);
        System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][POINT_ID] field=" + field
                + " operator=" + operator + " ids=" + ids);

        if (!"EQUALS".equals(operator) && !"IN".equals(operator) && !"NOT_IN".equals(operator)) {
            return Result.invalid("operator is not allowed for stopPoint id field.");
        }
        if ("EQUALS".equals(operator)) {
            Object value = leaf.get("value");
            if (!(value instanceof String id) || id.isBlank()) {
                return Result.invalid("stopPoint id EQUALS requires a non-empty string value.");
            }
            return validateIds(List.of(id.trim()));
        }
        Object values = leaf.get("values");
        if (!(values instanceof List<?> valueList) || valueList.isEmpty()) {
            return Result.invalid("stopPoint id " + operator + " requires a non-empty values array.");
        }
        List<String> stringIds = new ArrayList<>();
        for (Object value : valueList) {
            if (!(value instanceof String id) || id.isBlank()) {
                return Result.invalid("stopPoint id " + operator + " requires only non-empty string values.");
            }
            stringIds.add(id.trim());
        }
        return validateIds(stringIds);
    }

    private List<String> extractIds(String operator, Map<?, ?> leaf) {
        if (List.of("IN", "NOT_IN").contains(operator) && leaf.get("values") instanceof List<?> values) {
            return values.stream()
                    .map(String::valueOf)
                    .toList();
        }
        Object value = leaf.get("value");
        return value == null ? List.of() : List.of(String.valueOf(value));
    }

    private Result validateIds(List<String> ids) {
        for (String id : ids) {
            if (!STOP_POINT_ID_PATTERN.matcher(id).matches() || !pointRegistry.containsId(id)) {
                System.out.println("[IIA][ALERT_VERIFY][VALIDATOR][POINT_ID] unknownId=" + id);
                return Result.invalid("Unsupported or unknown stopPoint id in technicalSpecification: " + id);
            }
        }
        return Result.ok();
    }

    record Result(boolean valid, String reason) {
        static Result ok() {
            return new Result(true, null);
        }

        static Result invalid(String reason) {
            return new Result(false, reason);
        }
    }
}
