package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.List;

public record ScheduledConditionNode(
        String type,
        List<ScheduledConditionNode> all,
        List<ScheduledConditionNode> any,
        ScheduledConditionNode anyElement,
        String path,
        String field,
        String operator,
        Object value,
        List<Object> values,
        String otherField) {
}
