package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Objects;

public record AgentLifecycleTransitionDecision(
        AgentLifecycleAction action,
        String currentStatus,
        boolean allowed,
        boolean idempotent,
        AgentLifecycleExecutionMode executionMode,
        String targetStatus,
        String reason) {

    public AgentLifecycleTransitionDecision {
        Objects.requireNonNull(action, "action is required");
    }

    public static AgentLifecycleTransitionDecision allowed(
            AgentLifecycleAction action,
            String currentStatus,
            boolean idempotent,
            AgentLifecycleExecutionMode executionMode,
            String targetStatus) {
        return new AgentLifecycleTransitionDecision(
                action,
                currentStatus,
                true,
                idempotent,
                Objects.requireNonNull(executionMode, "executionMode is required"),
                Objects.requireNonNull(targetStatus, "targetStatus is required"),
                null);
    }

    public static AgentLifecycleTransitionDecision rejected(
            AgentLifecycleAction action,
            String currentStatus,
            String reason) {
        return new AgentLifecycleTransitionDecision(
                action,
                currentStatus,
                false,
                false,
                null,
                null,
                Objects.requireNonNull(reason, "reason is required"));
    }
}
