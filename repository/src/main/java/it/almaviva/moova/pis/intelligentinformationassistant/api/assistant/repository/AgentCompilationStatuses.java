package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import java.util.Set;

public final class AgentCompilationStatuses {

    public static final String PENDING = "PENDING";
    public static final String GENERATING_BLUEPRINT = "GENERATING_BLUEPRINT";
    public static final String VALIDATING_BLUEPRINT = "VALIDATING_BLUEPRINT";
    public static final String GENERATING_ARTIFACT = "GENERATING_ARTIFACT";
    public static final String STATIC_ANALYSIS = "STATIC_ANALYSIS";
    public static final String COMPILING = "COMPILING";
    public static final String TESTING = "TESTING";
    public static final String SIMULATING = "SIMULATING";
    public static final String SIGNING = "SIGNING";

    public static final String READY = "READY";
    public static final String FAILED = "FAILED";
    public static final String REJECTED = "REJECTED";

    public static final Set<String> RUNNING = Set.of(
            PENDING,
            GENERATING_BLUEPRINT,
            VALIDATING_BLUEPRINT,
            GENERATING_ARTIFACT,
            STATIC_ANALYSIS,
            COMPILING,
            TESTING,
            SIMULATING,
            SIGNING);

    public static final Set<String> TERMINAL = Set.of(READY, FAILED, REJECTED);

    private AgentCompilationStatuses() {
    }
}
