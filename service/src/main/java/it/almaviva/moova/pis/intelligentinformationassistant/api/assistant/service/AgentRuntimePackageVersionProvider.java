package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public interface AgentRuntimePackageVersionProvider {

    long resolvePackageVersion(AgentActivationSnapshot snapshot);
}
