package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationExample;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationPlan;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;

import java.util.ArrayList;
import java.util.List;

class AgentValidationPlanBuilder {

    AgentValidationPlan build(
            AlertAgentGenerationPreviewData data,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        if (conditionSummary.cancellation()
                && conditionSummary.location() != null
                && !conditionSummary.location().isBlank()
                && !conditionSummary.cancellationLeaves().isEmpty()) {
            List<AgentValidationExample> positiveExamples = new ArrayList<>();
            conditionSummary.cancellationLeaves().stream()
                    .limit(2)
                    .forEach(leaf -> positiveExamples.add(example(
                            "ServiceData event with " + statusLabel(leaf.field()) + " status " + leaf.value()
                                    + " at " + conditionSummary.location() + ".",
                            AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)));
            return new AgentValidationPlan()
                    .positiveExamples(positiveExamples)
                    .negativeExamples(List.of(
                            example("ServiceData event for " + conditionSummary.location()
                                            + " without cancellation statuses.",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                            example("ServiceData cancellation event for a different stop point.",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                    .edgeCases(List.of(
                            "ServiceData event without journeyId or vehicleJourneyName is rejected safely or produces a guarded output.",
                            "ServiceData event without stopPoint.nameLong does not match the location condition.",
                            "ServiceData event with empty arrivalStatuses and departureStatuses does not produce output."));
        }

        String eventName = data.interpretedEventNames() == null || data.interpretedEventNames().isEmpty()
                ? "a matching interpreted event"
                : data.interpretedEventNames().getFirst();
        return new AgentValidationPlan()
                .positiveExamples(List.of(example(
                        "ServiceData event with eventName " + eventName + ".",
                        AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                .negativeExamples(List.of(example(
                        "ServiceData event with an eventName outside the interpreted event set.",
                        AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                .edgeCases(List.of("ServiceData event without journeyId or with an incomplete target is rejected safely."));
    }

    private String statusLabel(String field) {
        if (field != null && field.toLowerCase().contains("departure")) {
            return "departure";
        }
        return "arrival";
    }

    private AgentValidationExample example(
            String description,
            AgentValidationExample.ExpectedOutputEnum expectedOutput) {
        return new AgentValidationExample().description(description).expectedOutput(expectedOutput);
    }
}
