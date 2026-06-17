package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Locale;
import java.util.Objects;

public class AgentLifecycleTransitionValidator {

    public AgentLifecycleTransitionDecision validate(String currentStatus, AgentLifecycleAction action) {
        Objects.requireNonNull(action, "action is required");
        String normalizedStatus = normalizeStatus(currentStatus);
        AgentLifecycleTransitionDecision decision = switch (action) {
            case ACTIVATE -> validateActivate(normalizedStatus);
            case DISABLE -> validateDisable(normalizedStatus);
        };
        log(decision);
        return decision;
    }

    private AgentLifecycleTransitionDecision validateActivate(String currentStatus) {
        if (currentStatus == null) {
            return reject(AgentLifecycleAction.ACTIVATE, null, "Agent Definition status is required");
        }
        return switch (currentStatus) {
            case "READY", "DISABLED" -> AgentLifecycleTransitionDecision.allowed(
                    AgentLifecycleAction.ACTIVATE,
                    currentStatus,
                    false,
                    AgentLifecycleExecutionMode.ORCHESTRATOR_REQUIRED,
                    "ACTIVE");
            case "ACTIVE" -> reject(
                    AgentLifecycleAction.ACTIVATE,
                    currentStatus,
                    "Agent Definition is already ACTIVE");
            case "DRAFT" -> reject(
                    AgentLifecycleAction.ACTIVATE,
                    currentStatus,
                    "Agent Definition must be successfully compiled before activation");
            case "COMPILATION_PENDING" -> reject(
                    AgentLifecycleAction.ACTIVATE,
                    currentStatus,
                    "Agent Definition compilation is not completed");
            case "COMPILING" -> reject(
                    AgentLifecycleAction.ACTIVATE,
                    currentStatus,
                    "Agent Definition compilation is in progress");
            case "REJECTED" -> reject(
                    AgentLifecycleAction.ACTIVATE,
                    currentStatus,
                    "Agent Definition or compilation has been rejected");
            case "SUPERSEDED" -> reject(
                    AgentLifecycleAction.ACTIVATE,
                    currentStatus,
                    "Agent Definition is superseded");
            case "ARCHIVED" -> reject(
                    AgentLifecycleAction.ACTIVATE,
                    currentStatus,
                    "Agent Definition is archived");
            default -> reject(
                    AgentLifecycleAction.ACTIVATE,
                    currentStatus,
                    "Unknown Agent Definition status: " + currentStatus);
        };
    }

    private AgentLifecycleTransitionDecision validateDisable(String currentStatus) {
        if (currentStatus == null) {
            return reject(AgentLifecycleAction.DISABLE, null, "Agent Definition status is required");
        }
        return switch (currentStatus) {
            case "READY" -> AgentLifecycleTransitionDecision.allowed(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    false,
                    AgentLifecycleExecutionMode.LOCAL_STATE_CHANGE,
                    "DISABLED");
            case "DISABLED" -> AgentLifecycleTransitionDecision.allowed(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    true,
                    AgentLifecycleExecutionMode.NO_OPERATION,
                    "DISABLED");
            case "ACTIVE" -> AgentLifecycleTransitionDecision.allowed(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    false,
                    AgentLifecycleExecutionMode.ORCHESTRATOR_REQUIRED,
                    "DISABLED");
            case "DRAFT" -> reject(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    "Agent Definition cannot be disabled from DRAFT status");
            case "COMPILATION_PENDING" -> reject(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    "Agent Definition compilation is not completed");
            case "COMPILING" -> reject(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    "Agent Definition compilation is in progress");
            case "REJECTED" -> reject(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    "Agent Definition or compilation has been rejected");
            case "SUPERSEDED" -> reject(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    "Agent Definition is superseded");
            case "ARCHIVED" -> reject(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    "Agent Definition is archived");
            default -> reject(
                    AgentLifecycleAction.DISABLE,
                    currentStatus,
                    "Unknown Agent Definition status: " + currentStatus);
        };
    }

    private AgentLifecycleTransitionDecision reject(
            AgentLifecycleAction action,
            String currentStatus,
            String reason) {
        return AgentLifecycleTransitionDecision.rejected(action, currentStatus, reason);
    }

    private String normalizeStatus(String currentStatus) {
        if (currentStatus == null) {
            return null;
        }
        String trimmed = currentStatus.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private void log(AgentLifecycleTransitionDecision decision) {
        StringBuilder message = new StringBuilder("[IIA][AGENT_LIFECYCLE][TRANSITION]")
                .append(" action=").append(decision.action())
                .append(" currentStatus=").append(decision.currentStatus())
                .append(" allowed=").append(decision.allowed());
        if (decision.allowed()) {
            message.append(" idempotent=").append(decision.idempotent())
                    .append(" executionMode=").append(decision.executionMode())
                    .append(" targetStatus=").append(decision.targetStatus());
        } else {
            message.append(" reason=").append(decision.reason());
        }
        System.out.println(message);
    }
}
