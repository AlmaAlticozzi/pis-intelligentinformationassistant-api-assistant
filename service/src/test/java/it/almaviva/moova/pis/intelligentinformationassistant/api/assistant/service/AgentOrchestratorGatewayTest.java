package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class AgentOrchestratorGatewayTest {

    private static final String AGENT_DEFINITION_ID = "AGDF123456789";
    private static final String CANONICAL_PACKAGE_HASH =
            "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final Instant REQUESTED_AT = Instant.parse("2026-06-17T10:15:30Z");

    private final UnavailableAgentOrchestratorGateway gateway = new UnavailableAgentOrchestratorGateway();

    @Test
    void activateLogsSafeDiagnosticsAndThrowsUnavailableException() {
        AgentOrchestratorActivationRequest request = new AgentOrchestratorActivationRequest(
                AGENT_DEFINITION_ID,
                submission(AGENT_DEFINITION_ID, "operator note must not be logged"),
                CANONICAL_PACKAGE_HASH);

        Captured captured = captureSystemOut(() -> gateway.activate(request));

        assertThat(captured.thrown()).isInstanceOf(AgentOrchestratorUnavailableException.class);
        AgentOrchestratorUnavailableException exception =
                (AgentOrchestratorUnavailableException) captured.thrown();
        assertThat(exception.operation()).isEqualTo(AgentOrchestratorOperation.ACTIVATE);
        assertThat(exception.agentDefinitionId()).isEqualTo(AGENT_DEFINITION_ID);
        assertThat(exception.targetMethod()).isEqualTo("PUT");
        assertThat(exception.targetPath()).isEqualTo("/v1/runtime-agent-definitions/" + AGENT_DEFINITION_ID);
        assertThat(exception.httpCallExecuted()).isFalse();

        assertThat(captured.output())
                .contains("[IIA][AGENT_ORCHESTRATOR][INTENTIONALLY_UNAVAILABLE]")
                .contains("operation=ACTIVATE")
                .contains("agentDefinitionId=" + AGENT_DEFINITION_ID)
                .contains("targetMethod=PUT")
                .contains("submissionId=ACTIVATE:" + AGENT_DEFINITION_ID + ":1:0123456789abcdef")
                .contains("packageVersion=1")
                .contains("desiredStatus=ACTIVE")
                .contains("packageHashPrefix=0123456789abcdef")
                .contains("runtimePackagePrepared=true")
                .contains("httpCallExecuted=false")
                .contains("outcome=ORCHESTRATOR_UNAVAILABLE");
        assertThat(captured.output())
                .doesNotContain("operator note must not be logged")
                .doesNotContain("dslSecretMarker")
                .doesNotContain("runtimeSecretMarker")
                .doesNotContain(CANONICAL_PACKAGE_HASH);
    }

    @Test
    void activateRejectsProgrammingErrorsBeforeUnavailableException() {
        assertThatThrownBy(() -> gateway.activate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("request");

        assertThatThrownBy(() -> new AgentOrchestratorActivationRequest(
                AGENT_DEFINITION_ID,
                submission("DIFFERENT", null),
                CANONICAL_PACKAGE_HASH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("submission.agentDefinition.id");
    }

    @Test
    void disableLogsSafeDiagnosticsAndThrowsUnavailableException() {
        AgentOrchestratorDisableRequest request = new AgentOrchestratorDisableRequest(
                AGENT_DEFINITION_ID,
                "  maintenance reason must not be logged  ",
                true,
                60,
                REQUESTED_AT);

        Captured captured = captureSystemOut(() -> gateway.disable(request));

        assertThat(captured.thrown()).isInstanceOf(AgentOrchestratorUnavailableException.class);
        AgentOrchestratorUnavailableException exception =
                (AgentOrchestratorUnavailableException) captured.thrown();
        assertThat(exception.operation()).isEqualTo(AgentOrchestratorOperation.DISABLE);
        assertThat(exception.agentDefinitionId()).isEqualTo(AGENT_DEFINITION_ID);
        assertThat(exception.targetMethod()).isEqualTo("POST");
        assertThat(exception.targetPath()).isEqualTo("/v1/runtime-agent-definitions/" + AGENT_DEFINITION_ID + "/disable");
        assertThat(exception.httpCallExecuted()).isFalse();

        assertThat(captured.output())
                .contains("[IIA][AGENT_ORCHESTRATOR][INTENTIONALLY_UNAVAILABLE]")
                .contains("operation=DISABLE")
                .contains("agentDefinitionId=" + AGENT_DEFINITION_ID)
                .contains("targetMethod=POST")
                .contains("targetPath=/v1/runtime-agent-definitions/" + AGENT_DEFINITION_ID + "/disable")
                .contains("stopRunningAgents=true")
                .contains("gracePeriodSeconds=60")
                .contains("requestedAt=" + REQUESTED_AT)
                .contains("httpCallExecuted=false")
                .contains("outcome=ORCHESTRATOR_UNAVAILABLE")
                .doesNotContain("maintenance reason must not be logged");
    }

    @Test
    void disableRequestNormalizesReasonAndValidatesGracePeriod() {
        AgentOrchestratorDisableRequest request = new AgentOrchestratorDisableRequest(
                " " + AGENT_DEFINITION_ID + " ",
                "   ",
                false,
                0,
                REQUESTED_AT);

        assertThat(request.agentDefinitionId()).isEqualTo(AGENT_DEFINITION_ID);
        assertThat(request.reason()).isNull();
        assertThat(request.stopRunningAgents()).isFalse();
        assertThat(request.gracePeriodSeconds()).isZero();

        assertThatThrownBy(() -> new AgentOrchestratorDisableRequest(
                AGENT_DEFINITION_ID, null, true, -1, REQUESTED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gracePeriodSeconds");
        assertThatThrownBy(() -> new AgentOrchestratorDisableRequest(
                AGENT_DEFINITION_ID, null, true, 601, REQUESTED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gracePeriodSeconds");
    }

    @Test
    void resultWarningsAreImmutable() {
        AgentOrchestratorRuntimeAgentResult result = new AgentOrchestratorRuntimeAgentResult(
                AGENT_DEFINITION_ID,
                "ACTIVE",
                "ACCEPTED",
                1,
                "submission",
                REQUESTED_AT,
                REQUESTED_AT,
                "runtime-assignment",
                List.of("warning"));

        assertThat(result.warnings()).containsExactly("warning");
        assertThatThrownBy(() -> result.warnings().add("another"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void gatewayContractDoesNotExposeRepositoriesHttpTypesOrGeneratedDtos() {
        assertThat(UnavailableAgentOrchestratorGateway.class.getDeclaredFields())
                .allSatisfy(field -> assertThat(Modifier.isStatic(field.getModifiers())).isTrue());
        assertThat(AgentOrchestratorGateway.class.getDeclaredMethods())
                .extracting(Method::getName)
                .containsExactlyInAnyOrder("activate", "disable");

        assertMethodHasInternalTypesOnly("activate", AgentOrchestratorActivationRequest.class);
        assertMethodHasInternalTypesOnly("disable", AgentOrchestratorDisableRequest.class);
        assertThat(AgentOrchestratorActivationRequest.class.getRecordComponents())
                .extracting(component -> component.getType().getName())
                .doesNotContain("it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationRequest");
        assertThat(AgentOrchestratorDisableRequest.class.getRecordComponents())
                .extracting(component -> component.getType().getName())
                .doesNotContain("it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDisableRequest");
    }

    private void assertMethodHasInternalTypesOnly(String methodName, Class<?> requestType) {
        Method method;
        try {
            method = AgentOrchestratorGateway.class.getMethod(methodName, requestType);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        assertThat(method.getReturnType()).isEqualTo(AgentOrchestratorRuntimeAgentResult.class);
        assertThat(method.getParameterTypes()).containsExactly(requestType);
        assertThat(method.getReturnType().getName()).doesNotContain("jakarta.ws", "javax.ws", "resteasy", "repository", ".model.assistant");
        assertThat(method.getParameterTypes()[0].getName()).doesNotContain("jakarta.ws", "javax.ws", "resteasy", "repository", ".model.assistant");
    }

    private Captured captureSystemOut(Runnable runnable) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Throwable thrown = catchThrowable(runnable::run);
            return new Captured(output.toString(StandardCharsets.UTF_8), thrown);
        } finally {
            System.setOut(original);
        }
    }

    private AgentRuntimeSubmission submission(String agentDefinitionId, String note) {
        return new AgentRuntimeSubmission(
                "ACTIVATE:" + agentDefinitionId + ":1:0123456789abcdef",
                "ACTIVE",
                1,
                REQUESTED_AT,
                "operator",
                true,
                note,
                new AgentRuntimeSubmission.AgentRuntimeDefinitionPackage(
                        agentDefinitionId,
                        "Runtime Agent",
                        "Runtime package for tests",
                        new AgentRuntimeSubmission.AgentRuntimeSourceReference(
                                "pis-intelligentinformationassistant-api-assistant",
                                "ALRT1",
                                "Alert",
                                7,
                                "AGCP1",
                                null),
                        new AgentRuntimeSubmission.AgentRuntimeProfileSnapshot(
                                "MEDIUM",
                                "Medium",
                                true,
                                250,
                                500,
                                512,
                                1024,
                                "TOOL_GATEWAY_ONLY",
                                "STANDARD_DSL_RUNTIME",
                                2),
                        new AgentRuntimeSubmission.AgentRuntimeActivationPolicy(
                                "CONTINUOUS",
                                "Europe/Rome",
                                OffsetDateTime.parse("2026-06-17T10:00:00Z"),
                                OffsetDateTime.parse("2026-07-17T10:00:00Z"),
                                null,
                                null,
                                null,
                                null,
                                null),
                        "EVENT_INTERPRETER",
                        "EVENT",
                        "ServiceDataV2",
                        "AgentOutput.CANDIDATE_SUGGESTION",
                        new AgentRuntimeSubmission.AgentRuntimeContractPackage(
                                "runtime-image",
                                "iia.agent.dsl/v1",
                                "0.0.3",
                                "KAFKA_EVENT",
                                "EVENT_INTERPRETER",
                                "EVENT",
                                "ServiceDataV2",
                                "AgentOutput.CANDIDATE_SUGGESTION",
                                "STATELESS_EVENT_MATCH",
                                List.of("equals"),
                                List.of(),
                                "TOOL_GATEWAY_ONLY",
                                List.of("runtimeSecretMarker"),
                                Map.of("runtimeClass", "STANDARD_DSL_RUNTIME"),
                                List.of("EVENT_STREAM"),
                                List.of("KAFKA"),
                                List.of("servicedata-realtime-v2")),
                        new AgentRuntimeSubmission.AgentRuntimeArtifact(
                                "DSL",
                                "INLINE",
                                "application/json",
                                "iia.agent.dsl/v1",
                                Map.of("dslSecretMarker", "SHOULD_NOT_LOG"),
                                "SHA-256",
                                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                                "JACKSON_SORT_PROPERTIES_AND_MAP_ENTRIES",
                                new AgentRuntimeSubmission.AgentRuntimeArtifactSignature(
                                        "LOGICAL_MVP",
                                        "SHA256_WITH_CONTROL_PLANE_ATTESTATION",
                                        OffsetDateTime.parse("2026-06-17T09:00:00Z"),
                                        "pis-intelligentinformationassistant-api-assistant",
                                        null,
                                        null),
                                OffsetDateTime.parse("2026-06-17T09:00:00Z"),
                                128),
                        Map.of("implementationSummary", "compiled"),
                        OffsetDateTime.parse("2026-06-17T09:30:00Z"),
                        "SERVICE_DATA",
                        List.of(new AgentRuntimeSubmission.AgentRuntimeDataSourceBinding(
                                "primaryInput",
                                "SERVICE_DATA",
                                "EVENT_STREAM",
                                "KAFKA",
                                "servicedata-realtime-v2",
                                "ServiceDataV2",
                                "v2",
                                "iia.runtime.data-source-binding/v1",
                                null,
                                true,
                                Map.of("subscriptionProfile", "SERVICEDATA_EVENTS")))));
    }

    private record Captured(String output, Throwable thrown) {
    }
}
