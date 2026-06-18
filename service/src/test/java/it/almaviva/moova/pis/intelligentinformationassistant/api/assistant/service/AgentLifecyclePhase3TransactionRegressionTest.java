package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AgentLifecyclePhase3TransactionRegressionTest {

    @Test
    void lifecycleUseCaseServicesDoNotHoldTransactionsAcrossRuntimeBoundaries() {
        assertThat(AgentActivationService.class.isAnnotationPresent(Transactional.class)).isFalse();
        assertThat(AgentDisableService.class.isAnnotationPresent(Transactional.class)).isFalse();
    }

    @Test
    void lifecycleDatabaseBoundariesRemainExplicitAndShort() throws Exception {
        Method snapshotLoad = AgentActivationSnapshotLoader.class.getMethod("load", String.class);
        Method lifecycleLoad = AgentDefinitionLifecycleStateLoader.class.getMethod("load", String.class);
        Method lifecycleTransition = AgentDefinitionLifecycleStateWriter.class.getMethod(
                "transition",
                String.class,
                String.class,
                String.class,
                Instant.class);

        assertThat(snapshotLoad.isAnnotationPresent(Transactional.class)).isTrue();
        assertThat(lifecycleLoad.isAnnotationPresent(Transactional.class)).isTrue();
        assertThat(lifecycleTransition.isAnnotationPresent(Transactional.class)).isTrue();
    }
}
