package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentLifecycleTransitionValidatorTest {

    private final AgentLifecycleTransitionValidator validator = new AgentLifecycleTransitionValidator();

    @Test
    void activateAllowsReadyWithOrchestratorExecution() {
        AgentLifecycleTransitionDecision decision = validator.validate("READY", AgentLifecycleAction.ACTIVATE);

        assertAllowed(decision, AgentLifecycleAction.ACTIVATE, "READY", false,
                AgentLifecycleExecutionMode.ORCHESTRATOR_REQUIRED, "ACTIVE");
    }

    @Test
    void activateAllowsDisabledWithOrchestratorExecution() {
        AgentLifecycleTransitionDecision decision = validator.validate("DISABLED", AgentLifecycleAction.ACTIVATE);

        assertAllowed(decision, AgentLifecycleAction.ACTIVATE, "DISABLED", false,
                AgentLifecycleExecutionMode.ORCHESTRATOR_REQUIRED, "ACTIVE");
    }

    @Test
    void activateRejectsActiveAsFutureConflict() {
        AgentLifecycleTransitionDecision decision = validator.validate("ACTIVE", AgentLifecycleAction.ACTIVATE);

        assertRejected(decision, AgentLifecycleAction.ACTIVATE, "ACTIVE", "Agent Definition is already ACTIVE");
    }

    @Test
    void activateRejectsNonActivableStatuses() {
        assertRejected(validator.validate("DRAFT", AgentLifecycleAction.ACTIVATE),
                AgentLifecycleAction.ACTIVATE, "DRAFT", "successfully compiled");
        assertRejected(validator.validate("COMPILATION_PENDING", AgentLifecycleAction.ACTIVATE),
                AgentLifecycleAction.ACTIVATE, "COMPILATION_PENDING", "not completed");
        assertRejected(validator.validate("COMPILING", AgentLifecycleAction.ACTIVATE),
                AgentLifecycleAction.ACTIVATE, "COMPILING", "in progress");
        assertRejected(validator.validate("REJECTED", AgentLifecycleAction.ACTIVATE),
                AgentLifecycleAction.ACTIVATE, "REJECTED", "rejected");
        assertRejected(validator.validate("SUPERSEDED", AgentLifecycleAction.ACTIVATE),
                AgentLifecycleAction.ACTIVATE, "SUPERSEDED", "superseded");
        assertRejected(validator.validate("ARCHIVED", AgentLifecycleAction.ACTIVATE),
                AgentLifecycleAction.ACTIVATE, "ARCHIVED", "archived");
    }

    @Test
    void activateRejectsMissingOrUnknownStatus() {
        assertRejected(validator.validate(null, AgentLifecycleAction.ACTIVATE),
                AgentLifecycleAction.ACTIVATE, null, "status is required");
        assertRejected(validator.validate("   ", AgentLifecycleAction.ACTIVATE),
                AgentLifecycleAction.ACTIVATE, null, "status is required");
        assertRejected(validator.validate("UNKNOWN_STATUS", AgentLifecycleAction.ACTIVATE),
                AgentLifecycleAction.ACTIVATE, "UNKNOWN_STATUS", "Unknown Agent Definition status");
    }

    @Test
    void activateNormalizesLowercaseAndWhitespaceStatus() {
        AgentLifecycleTransitionDecision decision = validator.validate(" ready ", AgentLifecycleAction.ACTIVATE);

        assertAllowed(decision, AgentLifecycleAction.ACTIVATE, "READY", false,
                AgentLifecycleExecutionMode.ORCHESTRATOR_REQUIRED, "ACTIVE");
    }

    @Test
    void disableReadyIsLocalStateChange() {
        AgentLifecycleTransitionDecision decision = validator.validate("READY", AgentLifecycleAction.DISABLE);

        assertAllowed(decision, AgentLifecycleAction.DISABLE, "READY", false,
                AgentLifecycleExecutionMode.LOCAL_STATE_CHANGE, "DISABLED");
    }

    @Test
    void disableDisabledIsIdempotentNoOperation() {
        AgentLifecycleTransitionDecision decision = validator.validate("DISABLED", AgentLifecycleAction.DISABLE);

        assertAllowed(decision, AgentLifecycleAction.DISABLE, "DISABLED", true,
                AgentLifecycleExecutionMode.NO_OPERATION, "DISABLED");
    }

    @Test
    void disableActiveRequiresOrchestratorExecution() {
        AgentLifecycleTransitionDecision decision = validator.validate("ACTIVE", AgentLifecycleAction.DISABLE);

        assertAllowed(decision, AgentLifecycleAction.DISABLE, "ACTIVE", false,
                AgentLifecycleExecutionMode.ORCHESTRATOR_REQUIRED, "DISABLED");
    }

    @Test
    void disableRejectsNonDisableableStatuses() {
        assertRejected(validator.validate("DRAFT", AgentLifecycleAction.DISABLE),
                AgentLifecycleAction.DISABLE, "DRAFT", "DRAFT");
        assertRejected(validator.validate("COMPILATION_PENDING", AgentLifecycleAction.DISABLE),
                AgentLifecycleAction.DISABLE, "COMPILATION_PENDING", "not completed");
        assertRejected(validator.validate("COMPILING", AgentLifecycleAction.DISABLE),
                AgentLifecycleAction.DISABLE, "COMPILING", "in progress");
        assertRejected(validator.validate("REJECTED", AgentLifecycleAction.DISABLE),
                AgentLifecycleAction.DISABLE, "REJECTED", "rejected");
        assertRejected(validator.validate("SUPERSEDED", AgentLifecycleAction.DISABLE),
                AgentLifecycleAction.DISABLE, "SUPERSEDED", "superseded");
        assertRejected(validator.validate("ARCHIVED", AgentLifecycleAction.DISABLE),
                AgentLifecycleAction.DISABLE, "ARCHIVED", "archived");
    }

    @Test
    void disableRejectsMissingOrUnknownStatus() {
        assertRejected(validator.validate(null, AgentLifecycleAction.DISABLE),
                AgentLifecycleAction.DISABLE, null, "status is required");
        assertRejected(validator.validate("   ", AgentLifecycleAction.DISABLE),
                AgentLifecycleAction.DISABLE, null, "status is required");
        assertRejected(validator.validate("UNKNOWN_STATUS", AgentLifecycleAction.DISABLE),
                AgentLifecycleAction.DISABLE, "UNKNOWN_STATUS", "Unknown Agent Definition status");
    }

    @Test
    void disableNormalizesLowercaseAndWhitespaceStatus() {
        AgentLifecycleTransitionDecision decision = validator.validate(" active ", AgentLifecycleAction.DISABLE);

        assertAllowed(decision, AgentLifecycleAction.DISABLE, "ACTIVE", false,
                AgentLifecycleExecutionMode.ORCHESTRATOR_REQUIRED, "DISABLED");
    }

    @Test
    void validatorIsStatelessAndIndependentFromPersistenceRestAndSnapshotModels() {
        assertThat(AgentLifecycleTransitionValidator.class.getDeclaredFields()).isEmpty();
        assertThat(AgentLifecycleTransitionValidator.class.getDeclaredConstructors()).hasSize(1);
        assertThat(AgentLifecycleTransitionDecision.class.getRecordComponents())
                .extracting(component -> component.getType().getName())
                .doesNotContain(
                        "it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentActivationSnapshot",
                        "it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository");
    }

    private void assertAllowed(
            AgentLifecycleTransitionDecision decision,
            AgentLifecycleAction action,
            String currentStatus,
            boolean idempotent,
            AgentLifecycleExecutionMode executionMode,
            String targetStatus) {
        assertThat(decision.action()).isEqualTo(action);
        assertThat(decision.currentStatus()).isEqualTo(currentStatus);
        assertThat(decision.allowed()).isTrue();
        assertThat(decision.idempotent()).isEqualTo(idempotent);
        assertThat(decision.executionMode()).isEqualTo(executionMode);
        assertThat(decision.targetStatus()).isEqualTo(targetStatus);
        assertThat(decision.reason()).isNull();
    }

    private void assertRejected(
            AgentLifecycleTransitionDecision decision,
            AgentLifecycleAction action,
            String currentStatus,
            String reasonFragment) {
        assertThat(decision.action()).isEqualTo(action);
        assertThat(decision.currentStatus()).isEqualTo(currentStatus);
        assertThat(decision.allowed()).isFalse();
        assertThat(decision.idempotent()).isFalse();
        assertThat(decision.executionMode()).isNull();
        assertThat(decision.targetStatus()).isNull();
        assertThat(decision.reason()).contains(reasonFragment);
    }
}
