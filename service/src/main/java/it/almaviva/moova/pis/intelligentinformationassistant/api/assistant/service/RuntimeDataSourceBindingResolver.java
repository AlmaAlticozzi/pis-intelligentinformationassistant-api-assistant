package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.List;

public interface RuntimeDataSourceBindingResolver {

    boolean supports(AgentActivationSnapshot snapshot);

    List<AgentRuntimeSubmission.AgentRuntimeDataSourceBinding> resolve(AgentActivationSnapshot snapshot);
}
