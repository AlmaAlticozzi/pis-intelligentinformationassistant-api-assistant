package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDisableRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentLifecycleCommandFactoryTest {

    private final AgentLifecycleCommandFactory factory = new AgentLifecycleCommandFactory();

    @Test
    void activationRequestNullUsesDefaults() {
        AgentActivationCommand command = factory.createActivationCommand("AGDF1", null);

        assertThat(command.agentDefinitionId()).isEqualTo("AGDF1");
        assertThat(command.note()).isNull();
        assertThat(command.startImmediatelyIfAllowed()).isTrue();
    }

    @Test
    void activationNullFieldsUseDefaults() {
        AgentActivationRequest request = new AgentActivationRequest();
        request.setNote(null);
        request.setStartImmediatelyIfAllowed(null);

        AgentActivationCommand command = factory.createActivationCommand("AGDF1", request);

        assertThat(command.note()).isNull();
        assertThat(command.startImmediatelyIfAllowed()).isTrue();
    }

    @Test
    void activationNoteIsTrimmed() {
        AgentActivationRequest request = new AgentActivationRequest().note("  activate now  ");

        AgentActivationCommand command = factory.createActivationCommand(" AGDF1 ", request);

        assertThat(command.agentDefinitionId()).isEqualTo("AGDF1");
        assertThat(command.note()).isEqualTo("activate now");
    }

    @Test
    void activationBlankNoteBecomesNull() {
        AgentActivationRequest request = new AgentActivationRequest().note("   ");

        AgentActivationCommand command = factory.createActivationCommand("AGDF1", request);

        assertThat(command.note()).isNull();
    }

    @Test
    void activationBlankAgentDefinitionIdIsRejected() {
        assertThatThrownBy(() -> factory.createActivationCommand(" ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("agentDefinitionId");
    }

    @Test
    void activationExplicitFalseIsPreserved() {
        AgentActivationRequest request = new AgentActivationRequest().startImmediatelyIfAllowed(false);

        AgentActivationCommand command = factory.createActivationCommand("AGDF1", request);

        assertThat(command.startImmediatelyIfAllowed()).isFalse();
    }

    @Test
    void disableRequestNullUsesDefaults() {
        AgentDisableCommand command = factory.createDisableCommand("AGDF1", null);

        assertThat(command.agentDefinitionId()).isEqualTo("AGDF1");
        assertThat(command.reason()).isNull();
        assertThat(command.stopRunningAgents()).isTrue();
        assertThat(command.gracePeriodSeconds()).isEqualTo(60);
    }

    @Test
    void disableNullFieldsUseDefaults() {
        AgentDisableRequest request = new AgentDisableRequest();
        request.setReason(null);
        request.setStopRunningAgents(null);
        request.setGracePeriodSeconds(null);

        AgentDisableCommand command = factory.createDisableCommand("AGDF1", request);

        assertThat(command.reason()).isNull();
        assertThat(command.stopRunningAgents()).isTrue();
        assertThat(command.gracePeriodSeconds()).isEqualTo(60);
    }

    @Test
    void disableReasonIsTrimmed() {
        AgentDisableRequest request = new AgentDisableRequest().reason("  maintenance  ");

        AgentDisableCommand command = factory.createDisableCommand(" AGDF1 ", request);

        assertThat(command.agentDefinitionId()).isEqualTo("AGDF1");
        assertThat(command.reason()).isEqualTo("maintenance");
    }

    @Test
    void disableBlankReasonBecomesNull() {
        AgentDisableRequest request = new AgentDisableRequest().reason("   ");

        AgentDisableCommand command = factory.createDisableCommand("AGDF1", request);

        assertThat(command.reason()).isNull();
    }

    @Test
    void disableExplicitFalseIsPreserved() {
        AgentDisableRequest request = new AgentDisableRequest().stopRunningAgents(false);

        AgentDisableCommand command = factory.createDisableCommand("AGDF1", request);

        assertThat(command.stopRunningAgents()).isFalse();
    }

    @Test
    void disableGracePeriodZeroIsAccepted() {
        AgentDisableRequest request = new AgentDisableRequest().gracePeriodSeconds(0);

        AgentDisableCommand command = factory.createDisableCommand("AGDF1", request);

        assertThat(command.gracePeriodSeconds()).isZero();
    }

    @Test
    void disableNegativeGracePeriodIsRejected() {
        AgentDisableRequest request = new AgentDisableRequest().gracePeriodSeconds(-1);

        assertThatThrownBy(() -> factory.createDisableCommand("AGDF1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gracePeriodSeconds");
    }

    @Test
    void disableGracePeriodOverSwaggerMaxIsRejected() {
        AgentDisableRequest request = new AgentDisableRequest().gracePeriodSeconds(601);

        assertThatThrownBy(() -> factory.createDisableCommand("AGDF1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("600");
    }

    @Test
    void disableBlankAgentDefinitionIdIsRejected() {
        assertThatThrownBy(() -> factory.createDisableCommand("\t", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("agentDefinitionId");
    }

    @Test
    void factoryDoesNotMutateOpenApiDtosAndDoesNotHoldStatefulDependencies() {
        AgentActivationRequest activationRequest = new AgentActivationRequest()
                .note("  keep spaces  ")
                .startImmediatelyIfAllowed(false);
        AgentDisableRequest disableRequest = new AgentDisableRequest()
                .reason("  keep reason  ")
                .stopRunningAgents(false)
                .gracePeriodSeconds(10);

        factory.createActivationCommand("AGDF1", activationRequest);
        factory.createDisableCommand("AGDF1", disableRequest);

        assertThat(activationRequest.getNote()).isEqualTo("  keep spaces  ");
        assertThat(disableRequest.getReason()).isEqualTo("  keep reason  ");
        assertThat(AgentLifecycleCommandFactory.class.getDeclaredFields())
                .allMatch(field -> Modifier.isStatic(field.getModifiers()));
    }
}
