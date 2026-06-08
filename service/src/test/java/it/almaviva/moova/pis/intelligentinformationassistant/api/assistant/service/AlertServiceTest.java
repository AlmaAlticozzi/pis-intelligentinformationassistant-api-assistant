package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AgentGenerationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AgentGenerationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteAccessMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteIntentKind;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteOutputMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteUnderstandingService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationMention;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationPolarity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationRelation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationRole;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationUnderstandingService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertVerificationService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertMonitoringScope;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertRuntimeMetadata;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertTechnicalSpecificationResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertTechnicalSpecificationUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationMockEngine;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationResolverService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataApiQueryContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationResolutionService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationResolutionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataResolvedLocation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.SimpleAlertLocationMentionExtractor;
import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class AlertServiceTest {

    @Test
    void verifyRouteRejectedPersistsRejectedBeforeTechnicalVerification() {
        AlertService service = verificationService(false);
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.locationUnderstandingEnabled = true;
        AlertVerificationRequest request = new AlertVerificationRequest();
        when(service.alertRouteUnderstandingService.understand(any()))
                .thenReturn(AlertRouteUnderstandingResult.rejected("Weather alerts are outside supported domains."));

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().rejectedReason()).contains("Weather alerts");
        assertThat(outcome.getValue().technicalSpecification()).isNull();
        assertThat(outcome.getValue().agentBlueprintPreview()).isNull();
        verify(service.alertVerificationPromptBuilder, never()).build(any());
        verify(service.llmGateway, never()).get();
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyScheduledRouteResolvesLocationsThenPersistsNotImplementedRejectedWithoutCallingAlertVerify() {
        AlertService service = verificationService(false);
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        service.locationUnderstandingEnabled = true;
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                scheduledMention("Garibaldi FS", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true));
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                List.of(resolvedScheduledLocation("Garibaldi FS", ScheduledAlertLocationRole.MONITORED_STOP_POINT, List.of("GARIBALDI_ID"))),
                List.of(),
                List.of("GARIBALDI_ID"),
                false,
                false,
                List.of(),
                List.of());
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().summary()).contains("monitored stop points were resolved");
        assertThat(outcome.getValue().rejectedReason()).isEqualTo(
                "The alert was recognized as a SERVICE_DATA scheduled snapshot alert, but SCHEDULED_INTERPRETER technical verification is not implemented yet.");
        assertThat(outcome.getValue().confidence()).isEqualTo(0.95);
        assertThat(outcome.getValue().requiredSources()).containsExactly("SERVICE_DATA");
        assertThat(outcome.getValue().interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(outcome.getValue().technicalSpecification()).isNull();
        assertThat(outcome.getValue().agentBlueprintPreview()).isNull();
        assertThat(outcome.getValue().warnings()).contains(
                "ROUTE_INTERPRETER_TYPE=SCHEDULED_INTERPRETER",
                "ROUTE_DATA_DOMAINS=SERVICE_DATA",
                "ROUTE_ACCESS_MODE=SERVICE_DATA_API_SNAPSHOT",
                "ROUTE_INTENT_KIND=SNAPSHOT_CONDITION",
                "ROUTE_OUTPUT_MODE=ON_MATCH",
                "SCHEDULED_MONITORING_SCOPE=EXPLICIT_STOP_POINTS",
                "SCHEDULED_SERVICE_DATA_API_STOP_POINTS=[GARIBALDI_ID]",
                "SCHEDULED_TECHNICAL_VERIFICATION_NOT_IMPLEMENTED");
        assertThat(outcome.getValue().safetyChecks()).contains("SCHEDULED_TECHNICAL_VERIFICATION_NOT_IMPLEMENTED");
        verify(service.alertVerificationPromptBuilder, never()).build(any());
        verify(service.llmGateway, never()).get();
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
        verify(service.scheduledAlertLocationUnderstandingService).understandLocations("Prompt", "ALRT1");
        verify(service.scheduledServiceDataLocationResolutionService).resolve(scheduledUnderstanding);
    }

    @Test
    void verifyScheduledRouteRejectsWhenMonitoredStopPointCannotBeResolved() {
        AlertService service = verificationService(false);
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                scheduledMention("Fermata Inventata", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true));
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                List.of(unresolvedScheduledLocation("Fermata Inventata", ScheduledAlertLocationRole.MONITORED_STOP_POINT, false)),
                List.of(),
                List.of(),
                false,
                true,
                List.of("Fermata Inventata"),
                List.of("Monitored stop point 'Fermata Inventata' could not be resolved to a ServiceData stop point id."));
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().rejectedReason()).contains("could not be resolved");
        assertThat(outcome.getValue().technicalSpecification()).isNull();
        assertThat(outcome.getValue().agentBlueprintPreview()).isNull();
        verify(service.alertVerificationPromptBuilder, never()).build(any());
        verify(service.llmGateway, never()).get();
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyScheduledRouteRejectsWhenScheduledLocationUnderstandingFails() {
        AlertService service = verificationService(false);
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenThrow(new IllegalStateException("provider returned non-json"));

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().rejectedReason())
                .contains("Scheduled location understanding failed")
                .contains("provider returned non-json");
        assertThat(outcome.getValue().technicalSpecification()).isNull();
        assertThat(outcome.getValue().agentBlueprintPreview()).isNull();
        verify(service.scheduledServiceDataLocationResolutionService, never()).resolve(any());
        verify(service.alertVerificationPromptBuilder, never()).build(any());
        verify(service.llmGateway, never()).get();
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyScheduledRouteRejectsWhenNoMonitoredStopPointAndUnspecifiedScope() {
        AlertService service = verificationService(false);
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.UNSPECIFIED);
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.UNSPECIFIED,
                List.of(),
                List.of(),
                List.of(),
                false,
                false,
                List.of(),
                List.of());
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().rejectedReason())
                .contains("requires at least one monitored stop point or an explicit all-locations scope");
        assertThat(outcome.getValue().technicalSpecification()).isNull();
        assertThat(outcome.getValue().agentBlueprintPreview()).isNull();
        verify(service.alertVerificationPromptBuilder, never()).build(any());
        verify(service.llmGateway, never()).get();
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyScheduledRouteAllKnownStopPointsContinuesToNotImplementedRejected() {
        AlertService service = verificationService(false);
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS);
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS,
                List.of(),
                List.of(),
                List.of(),
                true,
                false,
                List.of(),
                List.of("All known stop points scope requested; ids will be materialized by runtime or later verification phase."));
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().rejectedReason()).contains("SCHEDULED_INTERPRETER technical verification is not implemented yet");
        assertThat(outcome.getValue().warnings()).contains(
                "SCHEDULED_MONITORING_SCOPE=ALL_KNOWN_STOP_POINTS",
                "SCHEDULED_SERVICE_DATA_API_STOP_POINTS=ALL_KNOWN_STOP_POINTS",
                "All known stop points scope requested; scheduled technical verification is not implemented yet.");
        verify(service.alertVerificationPromptBuilder, never()).build(any());
        verify(service.llmGateway, never()).get();
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyScheduledRouteWithFeatureEnabledPersistsScheduledVerifiedOutcome() {
        AlertService service = verificationService(false);
        service.scheduledVerifyEnabled = true;
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        service.scheduledAlertVerificationService = mock(ScheduledAlertVerificationService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                scheduledMention("Garibaldi FS", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true));
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                List.of(resolvedScheduledLocation("Garibaldi FS", ScheduledAlertLocationRole.MONITORED_STOP_POINT, List.of("GARIBALDI_ID"))),
                List.of(),
                List.of("GARIBALDI_ID"),
                false,
                false,
                List.of(),
                List.of());
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);
        when(service.scheduledAlertVerificationService.verify(any(), any(), any(), any(), any(), any()))
                .thenReturn(scheduledVerifiedOutcome());

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.getValue().technicalSpecification()).isNotNull();
        assertThat(outcome.getValue().agentBlueprintPreview()).isNotNull();
        assertThat(outcome.getValue().interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(outcome.getValue().rejectedReason()).isNull();
        verify(service.scheduledAlertVerificationService).verify("ALRT1", "Alert", null, "Prompt", scheduledRoute(), scheduledContext);
        verify(service.alertVerificationPromptBuilder, never()).build(any());
        verify(service.llmGateway, never()).get();
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyScheduledRouteWithFeatureEnabledPersistsScheduledRejectedOutcome() {
        AlertService service = verificationService(false);
        service.scheduledVerifyEnabled = true;
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        service.scheduledAlertVerificationService = mock(ScheduledAlertVerificationService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                scheduledMention("Garibaldi FS", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true));
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                List.of(resolvedScheduledLocation("Garibaldi FS", ScheduledAlertLocationRole.MONITORED_STOP_POINT, List.of("GARIBALDI_ID"))),
                List.of(),
                List.of("GARIBALDI_ID"),
                false,
                false,
                List.of(),
                List.of());
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);
        when(service.scheduledAlertVerificationService.verify(any(), any(), any(), any(), any(), any()))
                .thenReturn(scheduledRejectedOutcome("Scheduled validator rejected the generated specification."));

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().rejectedReason()).isEqualTo("Scheduled validator rejected the generated specification.");
        assertThat(outcome.getValue().technicalSpecification()).isNull();
        assertThat(outcome.getValue().agentBlueprintPreview()).isNull();
        verify(service.scheduledAlertVerificationService).verify("ALRT1", "Alert", null, "Prompt", scheduledRoute(), scheduledContext);
        verify(service.alertVerificationPromptBuilder, never()).build(any());
        verify(service.llmGateway, never()).get();
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyScheduledRouteWithFeatureDisabledDoesNotCallScheduledVerificationService() {
        AlertService service = verificationService(false);
        service.scheduledVerifyEnabled = false;
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        service.scheduledAlertVerificationService = mock(ScheduledAlertVerificationService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                scheduledMention("Garibaldi FS", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true));
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                List.of(resolvedScheduledLocation("Garibaldi FS", ScheduledAlertLocationRole.MONITORED_STOP_POINT, List.of("GARIBALDI_ID"))),
                List.of(),
                List.of("GARIBALDI_ID"),
                false,
                false,
                List.of(),
                List.of());
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().rejectedReason()).contains("SCHEDULED_INTERPRETER technical verification is not implemented yet");
        verify(service.scheduledAlertVerificationService, never()).verify(any(), any(), any(), any(), any(), any());
    }

    @Test
    void verifyScheduledUnresolvedMonitoredLocationDoesNotCallScheduledVerificationServiceWhenEnabled() {
        AlertService service = verificationService(false);
        service.scheduledVerifyEnabled = true;
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        service.scheduledAlertVerificationService = mock(ScheduledAlertVerificationService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                scheduledMention("Fermata Inventata", ScheduledAlertLocationRole.MONITORED_STOP_POINT, true));
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                List.of(unresolvedScheduledLocation("Fermata Inventata", ScheduledAlertLocationRole.MONITORED_STOP_POINT, false)),
                List.of(),
                List.of(),
                false,
                true,
                List.of("Fermata Inventata"),
                List.of());
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().rejectedReason()).contains("could not be resolved");
        verify(service.scheduledAlertVerificationService, never()).verify(any(), any(), any(), any(), any(), any());
    }

    @Test
    void verifyScheduledAllKnownStopPointsCallsScheduledVerificationServiceWhenEnabled() {
        AlertService service = verificationService(false);
        service.scheduledVerifyEnabled = true;
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        service.scheduledAlertVerificationService = mock(ScheduledAlertVerificationService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS);
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS,
                List.of(),
                List.of(),
                List.of(),
                true,
                false,
                List.of(),
                List.of());
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);
        when(service.scheduledAlertVerificationService.verify(any(), any(), any(), any(), any(), any()))
                .thenReturn(scheduledVerifiedOutcome());

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        verify(service.scheduledAlertVerificationService).verify("ALRT1", "Alert", null, "Prompt", scheduledRoute(), scheduledContext);
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyAllLocationsScheduledReportReachesScheduledLocationUnderstanding() {
        String prompt = "Fammi sapere ogni ora quanti treni sono in ritardo in tutte le località";
        AlertService service = verificationService(false);
        when(service.alertRepository.getAlertVerificationPromptData("ALRT1"))
                .thenReturn(java.util.Optional.of(new AlertVerificationPromptData("ALRT1", "Alert", null, prompt)));
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.alertLocationUnderstandingService = mock(AlertLocationUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        service.scheduledServiceDataLocationResolutionService = mock(ScheduledServiceDataLocationResolutionService.class);
        AlertVerificationRequest request = new AlertVerificationRequest();
        ScheduledAlertLocationUnderstandingResult scheduledUnderstanding = scheduledUnderstanding(
                ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS);
        ScheduledServiceDataLocationContext scheduledContext = scheduledContext(
                ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS,
                List.of(),
                List.of(),
                List.of(),
                true,
                false,
                List.of(),
                List.of("All known stop points scope requested; ids will be materialized by runtime or later verification phase."));
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(scheduledRoute());
        when(service.scheduledAlertLocationUnderstandingService.understandLocations(any(), any()))
                .thenReturn(scheduledUnderstanding);
        when(service.scheduledServiceDataLocationResolutionService.resolve(scheduledUnderstanding))
                .thenReturn(scheduledContext);

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.getValue().rejectedReason()).contains("SCHEDULED_INTERPRETER technical verification is not implemented yet");
        assertThat(outcome.getValue().rejectedReason()).doesNotContain("Weather");
        assertThat(outcome.getValue().warnings()).contains(
                "SCHEDULED_MONITORING_SCOPE=ALL_KNOWN_STOP_POINTS",
                "SCHEDULED_SERVICE_DATA_API_STOP_POINTS=ALL_KNOWN_STOP_POINTS");
        verify(service.scheduledAlertLocationUnderstandingService).understandLocations(prompt, "ALRT1");
        verify(service.scheduledServiceDataLocationResolutionService).resolve(scheduledUnderstanding);
        verify(service.alertVerificationPromptBuilder, never()).build(any());
        verify(service.llmGateway, never()).get();
        verify(service.alertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyEventRouteContinuesIntoExistingAlertVerifyFlow() {
        AlertService service = verificationService(false);
        service.alertRouteUnderstandingService = mock(AlertRouteUnderstandingService.class);
        service.scheduledAlertLocationUnderstandingService = mock(ScheduledAlertLocationUnderstandingService.class);
        when(service.alertRouteUnderstandingService.understand(any())).thenReturn(eventRoute());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse("""
                {
                  "decision":"REJECTED",
                  "summary":"Unsupported.",
                  "rejectedReason":"Unsupported.",
                  "confidence":0.0,
                  "warnings":[],
                  "safetyChecks":[]
                }
                """, "OPENAI", "gpt-4.1-mini", null, null, null));

        service.verifyAlert("ALRT1", new AlertVerificationRequest());

        ArgumentCaptor<LlmRequest> llmRequest = ArgumentCaptor.forClass(LlmRequest.class);
        verify(service.alertVerificationPromptBuilder).build(any());
        verify(service.llmGateway.get()).generateText(llmRequest.capture());
        assertThat(llmRequest.getValue().useCase()).isEqualTo(AiUseCase.ALERT_VERIFY);
        verify(service.scheduledAlertLocationUnderstandingService, never()).understandLocations(any(), any());
    }

    @Test
    void verifyParserFailureDoesNotUseMockWhenFallbackIsDisabled() {
        AlertService service = verificationService(false);
        AlertVerificationRequest request = new AlertVerificationRequest();
        when(service.llmGateway.get().generateText(any())).thenReturn(
                new LlmResponse("{\"decision\":\"VERIFIED\"", "OPENAI", "gpt-4.1-mini", null, null, null));

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.ERROR);
        assertThat(outcome.getValue().warnings().getFirst()).contains("looksTruncated=true");
        verify(service.alertVerificationMockEngine, never()).verify(any(), any());
    }

    @Test
    void verifyParserFailureUsesMockOnlyWhenFallbackIsEnabled() {
        AlertService service = verificationService(true);
        AlertVerificationRequest request = new AlertVerificationRequest();
        when(service.llmGateway.get().generateText(any())).thenReturn(
                new LlmResponse("{\"decision\":\"VERIFIED\"", "OPENAI", "gpt-4.1-mini", null, null, null));
        when(service.alertVerificationMockEngine.verify("ALRT1", "Prompt"))
                .thenReturn(rejectedVerificationOutcome("MOCK"));

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        verify(service.alertVerificationMockEngine).verify("ALRT1", "Prompt");
        assertThat(outcome.getValue().warnings()).contains(
                "LLM response was empty, invalid or rejected. Deterministic mock fallback was used because fallback-on-invalid-llm is enabled.");
    }

    @Test
    void verifyTenantFailureIsNotReportedAsAiProviderFailure() {
        AlertService service = verificationService(false);
        AlertVerificationRequest request = new AlertVerificationRequest();
        when(service.llmGateway.get().generateText(any())).thenThrow(
                new RuntimeException("SessionFactory configured for multi-tenancy, but no tenant identifier specified"));

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.ERROR);
        assertThat(outcome.getValue().summary()).contains("tenant context").doesNotContain("AI provider");
    }

    @Test
    void verifyFlowDoesNotFailForNoLocationPrompt() {
        AlertService service = verificationService(false);
        AlertVerificationRequest request = new AlertVerificationRequest();
        when(service.alertRepository.getAlertVerificationPromptData("ALRT1"))
                .thenReturn(java.util.Optional.of(new AlertVerificationPromptData(
                        "ALRT1",
                        "No location",
                        null,
                        "Dimmi quando una metro è in ritardo di oltre 10 min")));
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse("""
                {
                  "decision":"REJECTED",
                  "summary":"Unsupported.",
                  "rejectedReason":"Unsupported.",
                  "confidence":0.0,
                  "warnings":[],
                  "safetyChecks":[]
                }
                """, "OPENAI", "gpt-4.1-mini", null, null, null));

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationPromptData> promptData =
                ArgumentCaptor.forClass(AlertVerificationPromptData.class);
        verify(service.alertVerificationPromptBuilder).build(promptData.capture());
        AlertVerificationLocationContext context = promptData.getValue().locationResolutionContext();
        assertThat(context.hasLocationMentions()).isFalse();
        assertThat(context.resolutions()).isEmpty();
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), any());
    }

    @Test
    void createWithVerifyImmediatelyFalseKeepsDraftDisabled() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertCreateRequest request = createRequest(false, true);
        AlertDetail draft = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.DRAFT)
                .enabled(false);
        doReturn(draft).when(service).createDraftAlertInNewTransaction(request);

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.DRAFT);
        assertThat(result.getEnabled()).isFalse();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void createWithVerifyImmediatelyTrueReturnsVerifyingAndSchedulesAsyncVerification() {
        AlertService service = spy(new AlertService());
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail verifying = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFYING).enabled(false);
        doReturn(verifying).when(service).createVerifyingAlertInNewTransaction(request);

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.VERIFYING);
        assertThat(result.getEnabled()).isFalse();
        verify(service.alertAsyncVerificationService).scheduleCreatedAlertVerification("ALRT1", true, null);
    }

    @Test
    void createWithVerifyImmediatelyTruePropagatesResolvedTenantToAsyncVerification() {
        AlertService service = spy(new AlertService());
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        service.tenantContext = mock(TenantContext.class);
        service.tenantResolver = mock(Instance.class);
        TenantResolver resolver = mock(TenantResolver.class);
        when(service.tenantResolver.isUnsatisfied()).thenReturn(false);
        when(service.tenantResolver.isAmbiguous()).thenReturn(false);
        when(service.tenantResolver.get()).thenReturn(resolver);
        when(resolver.resolveTenantId()).thenReturn("tenant-a");
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail verifying = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFYING).enabled(false);
        doReturn(verifying).when(service).createVerifyingAlertInNewTransaction(request);

        service.createAlert(request);

        verify(service.alertAsyncVerificationService).scheduleCreatedAlertVerification("ALRT1", true, "tenant-a");
    }

    @Test
    void createWithVerifyImmediatelyTrueFallsBackToDefaultSchemaForAsyncVerification() {
        AlertService service = spy(new AlertService());
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        service.defaultSchema = "pis_intelligentinformationassistant";
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail verifying = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFYING).enabled(false);
        doReturn(verifying).when(service).createVerifyingAlertInNewTransaction(request);

        service.createAlert(request);

        verify(service.alertAsyncVerificationService).scheduleCreatedAlertVerification(
                "ALRT1", true, "pis_intelligentinformationassistant");
    }

    @Test
    void createWithVerifyImmediatelyTrueDoesNotBlockForRejectedResult() {
        AlertService service = spy(new AlertService());
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail verifying = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFYING).enabled(false);
        doReturn(verifying).when(service).createVerifyingAlertInNewTransaction(request);

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.VERIFYING);
        assertThat(result.getEnabled()).isFalse();
        verify(service.alertAsyncVerificationService).scheduleCreatedAlertVerification("ALRT1", true, null);
    }

    @Test
    void createWithVerifyImmediatelyTrueDoesNotBlockForProviderError() {
        AlertService service = spy(new AlertService());
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail verifying = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFYING).enabled(false);
        doReturn(verifying).when(service).createVerifyingAlertInNewTransaction(request);

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.VERIFYING);
        assertThat(result.getEnabled()).isFalse();
        verify(service.alertAsyncVerificationService).scheduleCreatedAlertVerification("ALRT1", true, null);
    }

    @Test
    void updateWithUnchangedPromptUpdatesOnlyMetadata() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("  Create a suggestion when a journey is cancelled.  ")
                .enabled(true)
                .version(3);
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is cancelled.");
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt(current.getPrompt())
                .enabled(true)
                .version(3);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertMetadataWithoutPromptChangeInNewTransaction("ALRT1", request);

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service).updateAlertMetadataWithoutPromptChangeInNewTransaction("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyFalseResetsToDraft() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.");
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(false)
                .enableAfterVerification(true);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.DRAFT)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertDraftAfterPromptChangeInNewTransaction("ALRT1", request);

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service).updateAlertDraftAfterPromptChangeInNewTransaction("ALRT1", request);
        verify(repository, never()).updateAlertMetadataWithoutPromptChange("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyNullResetsToDraft() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.");
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(null)
                .enableAfterVerification(true);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.DRAFT)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertDraftAfterPromptChangeInNewTransaction("ALRT1", request);

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service).updateAlertDraftAfterPromptChangeInNewTransaction("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyTrueSavesVerifyingAndSchedulesAsyncVerification() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.")
                .enabled(true);
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(true)
                .enableAfterVerification(true);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFYING)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertVerifyingAfterPromptChangeInNewTransaction("ALRT1", request);

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service).updateAlertVerifyingAfterPromptChangeInNewTransaction("ALRT1", request);
        verify(service.alertAsyncVerificationService).scheduleCreatedAlertVerification("ALRT1", true, null);
        verify(repository, never()).updateAlertMetadataWithoutPromptChange("ALRT1", request);
        verify(repository, never()).updateAlertDraftAfterPromptChange("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyTrueDoesNotEnablePreviouslyDisabledAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.")
                .enabled(false);
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(true)
                .enableAfterVerification(true);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFYING)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertVerifyingAfterPromptChangeInNewTransaction("ALRT1", request);

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service.alertAsyncVerificationService).scheduleCreatedAlertVerification("ALRT1", false, null);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyTrueDoesNotEnableWhenRequestDisablesAfterVerification() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.")
                .enabled(true);
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(true)
                .enableAfterVerification(false);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFYING)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertVerifyingAfterPromptChangeInNewTransaction("ALRT1", request);

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service.alertAsyncVerificationService).scheduleCreatedAlertVerification("ALRT1", false, null);
    }

    @Test
    void enableVerifiedOperationalAlertUpdatesOnlyEnabledFlag() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(false);
        AlertDetail enabled = verifiedAlert(true);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        when(repository.hasOperationalVerificationMetadata("ALRT1")).thenReturn(true);
        when(repository.updateAlertEnabled("ALRT1", true)).thenReturn(java.util.Optional.of(enabled));

        java.util.Optional<AlertDetail> result = service.enableAlert("ALRT1");

        assertThat(result).contains(enabled);
        assertThat(result.orElseThrow().getStatus()).isEqualTo(AlertStatus.VERIFIED);
        assertThat(result.orElseThrow().getVerification().getStatus()).isEqualTo(AlertVerificationStatus.VERIFIED);
        verify(repository).updateAlertEnabled("ALRT1", true);
    }

    @Test
    void enableRequiresVerifiedLifecycleStatus() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(false).status(AlertStatus.DRAFT);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));

        assertThatThrownBy(() -> service.enableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.STATUS_NOT_VERIFIED);

        verify(repository, never()).updateAlertEnabled("ALRT1", true);
    }

    @Test
    void enableRequiresVerifiedVerificationStatus() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(false)
                .verification(new AlertVerificationResult().status(AlertVerificationStatus.PENDING));
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));

        assertThatThrownBy(() -> service.enableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.VERIFICATION_NOT_VERIFIED);
    }

    @Test
    void enableRejectsAlreadyEnabledAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(true)));

        assertThatThrownBy(() -> service.enableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.ALREADY_ENABLED);
    }

    @Test
    void enableRequiresOperationalInterpreterMetadata() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(false)));
        when(repository.hasOperationalVerificationMetadata("ALRT1")).thenReturn(false);

        assertThatThrownBy(() -> service.enableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.MISSING_OPERATIONAL_METADATA);
    }

    @Test
    void disableEnabledAlertUpdatesOnlyEnabledFlag() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(true);
        AlertDetail disabled = verifiedAlert(false);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        when(repository.updateAlertEnabled("ALRT1", false)).thenReturn(java.util.Optional.of(disabled));

        java.util.Optional<AlertDetail> result = service.disableAlert("ALRT1");

        assertThat(result).contains(disabled);
        assertThat(result.orElseThrow().getStatus()).isEqualTo(AlertStatus.VERIFIED);
        assertThat(result.orElseThrow().getVerification().getStatus()).isEqualTo(AlertVerificationStatus.VERIFIED);
        verify(repository).updateAlertEnabled("ALRT1", false);
    }

    @Test
    void disableRejectsVerifyingAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(true).status(AlertStatus.VERIFYING);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));

        assertThatThrownBy(() -> service.disableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.VERIFYING);
    }

    @Test
    void disableRejectsAlreadyDisabledAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(false)));

        assertThatThrownBy(() -> service.disableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.ALREADY_DISABLED);
    }

    @Test
    void deleteSoftDeletesAllowedLifecycleState() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(true)));
        when(repository.softDeleteAlert("ALRT1")).thenReturn(true);

        boolean result = service.deleteAlert("ALRT1");

        assertThat(result).isTrue();
        verify(repository).softDeleteAlert("ALRT1");
    }

    @Test
    void deleteReturnsFalseWhenAlertDoesNotExist() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.empty());

        boolean result = service.deleteAlert("ALRT1");

        assertThat(result).isFalse();
        verify(repository, never()).softDeleteAlert("ALRT1");
    }

    @Test
    void deleteRejectsAlreadyDeletedAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.existsDeletedAlert("ALRT1")).thenReturn(true);

        assertThatThrownBy(() -> service.deleteAlert("ALRT1"))
                .isInstanceOf(AlertDeleteRejectedException.class)
                .extracting(ex -> ((AlertDeleteRejectedException) ex).reason())
                .isEqualTo(AlertDeleteRejectedException.Reason.DELETED);

        verify(repository, never()).softDeleteAlert("ALRT1");
    }

    @Test
    void deleteRejectsVerifyingAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(false).status(AlertStatus.VERIFYING)));

        assertThatThrownBy(() -> service.deleteAlert("ALRT1"))
                .isInstanceOf(AlertDeleteRejectedException.class)
                .extracting(ex -> ((AlertDeleteRejectedException) ex).reason())
                .isEqualTo(AlertDeleteRejectedException.Reason.VERIFYING);

        verify(repository, never()).softDeleteAlert("ALRT1");
    }

    @Test
    void deleteRejectsDeployingAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail deploying = verifiedAlert(false)
                .runtime(new AlertRuntimeMetadata().deploymentStatus(AlertRuntimeMetadata.DeploymentStatusEnum.DEPLOYING));
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(deploying));

        assertThatThrownBy(() -> service.deleteAlert("ALRT1"))
                .isInstanceOf(AlertDeleteRejectedException.class)
                .extracting(ex -> ((AlertDeleteRejectedException) ex).reason())
                .isEqualTo(AlertDeleteRejectedException.Reason.DEPLOYING);

        verify(repository, never()).softDeleteAlert("ALRT1");
    }

    @Test
    void previewVerifiedAlertBuildsReadOnlyResponse() {
        AlertRepository repository = mock(AlertRepository.class);
        AgentGenerationPreviewMapper mapper = mock(AgentGenerationPreviewMapper.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        service.agentGenerationPreviewMapper = mapper;
        AlertAgentGenerationPreviewData data = previewData("VERIFIED", "VERIFIED", Map.of("source", "SERVICE_DATA"), Map.of("agentName", "Agent"));
        AgentGenerationPreviewResponse response = new AgentGenerationPreviewResponse();
        when(repository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(mapper.toResponse(data, null)).thenReturn(response);

        java.util.Optional<AgentGenerationPreviewResponse> result = service.previewAgentGenerationForAlert("ALRT1", null);

        assertThat(result).contains(response);
        verify(mapper).toResponse(data, null);
    }

    @Test
    void previewMissingAlertReturnsEmpty() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.empty());

        java.util.Optional<AgentGenerationPreviewResponse> result = service.previewAgentGenerationForAlert("ALRT1", null);

        assertThat(result).isEmpty();
    }

    @Test
    void previewDraftAlertIsRejected() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlertAgentGenerationPreviewData("ALRT1"))
                .thenReturn(java.util.Optional.of(previewData("DRAFT", "PENDING", Map.of(), Map.of())));

        assertThatThrownBy(() -> service.previewAgentGenerationForAlert("ALRT1", null))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class)
                .extracting(ex -> ((AlertAgentGenerationPreviewRejectedException) ex).reason())
                .isEqualTo(AlertAgentGenerationPreviewRejectedException.Reason.NOT_VERIFIED);
    }

    @Test
    void previewVerifiedAlertWithoutArtifactsIsUnprocessable() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlertAgentGenerationPreviewData("ALRT1"))
                .thenReturn(java.util.Optional.of(previewData("VERIFIED", "VERIFIED", null, Map.of("agentName", "Agent"))));

        assertThatThrownBy(() -> service.previewAgentGenerationForAlert("ALRT1", null))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class)
                .extracting(ex -> ((AlertAgentGenerationPreviewRejectedException) ex).reason())
                .isEqualTo(AlertAgentGenerationPreviewRejectedException.Reason.MISSING_TECHNICAL_ARTIFACTS);
    }

    @Test
    void getAlertTechnicalSpecificationReturnsPersistedSpecification() {
        AlertService service = verificationService(false);
        Alert alert = verifiedTechnicalSpecificationAlert(Map.of("source", "SERVICE_DATA", "schemaVersion", "v1"));
        alert.setFlgTechnicalspecificationedited(true);
        when(service.alertRepository.findAlertForTechnicalSpecification("ALRT1"))
                .thenReturn(java.util.Optional.of(alert));

        AlertTechnicalSpecificationResponse response = service.getAlertTechnicalSpecification("ALRT1").orElseThrow();

        assertThat(response.getAlert().getId()).isEqualTo("ALRT1");
        assertThat(response.getAlert().getName()).isEqualTo("Verified alert");
        assertThat(response.getStatus()).isEqualTo(AlertStatus.VERIFIED);
        assertThat(response.getVerificationStatus()).isEqualTo(AlertVerificationStatus.VERIFIED);
        assertThat(response.getInterpreterType())
                .isEqualTo(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType.EVENT_INTERPRETER);
        assertThat(response.getInputModel()).isEqualTo("ServiceDataV2");
        assertThat(response.getOutputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(response.getTechnicalSpecificationEdited()).isTrue();
        assertThat(response.getTechnicalSpecification()).containsEntry("source", "SERVICE_DATA");
        assertThat(response.getWarnings()).isEmpty();
        assertThat(response.getAgentBlueprintPreviewRegenerated()).isNull();
    }

    @Test
    void getAlertTechnicalSpecificationRejectsBlankSpecification() {
        AlertService service = verificationService(false);
        Alert alert = verifiedTechnicalSpecificationAlert(" ");
        when(service.alertRepository.findAlertForTechnicalSpecification("ALRT1"))
                .thenReturn(java.util.Optional.of(alert));

        assertThatThrownBy(() -> service.getAlertTechnicalSpecification("ALRT1"))
                .isInstanceOf(AlertTechnicalSpecificationRejectedException.class)
                .extracting(ex -> ((AlertTechnicalSpecificationRejectedException) ex).reason())
                .isEqualTo(AlertTechnicalSpecificationRejectedException.Reason.INVALID_TECHNICAL_SPECIFICATION);
    }

    @Test
    void getAlertTechnicalSpecificationRejectsDeletedAlert() {
        AlertService service = verificationService(false);
        Alert alert = verifiedTechnicalSpecificationAlert(Map.of("source", "SERVICE_DATA"));
        alert.setDtDeletedat(java.time.OffsetDateTime.parse("2026-06-08T10:00:00Z"));
        when(service.alertRepository.findAlertForTechnicalSpecification("ALRT1"))
                .thenReturn(java.util.Optional.of(alert));

        assertThatThrownBy(() -> service.getAlertTechnicalSpecification("ALRT1"))
                .isInstanceOf(AlertTechnicalSpecificationRejectedException.class)
                .extracting(ex -> ((AlertTechnicalSpecificationRejectedException) ex).reason())
                .isEqualTo(AlertTechnicalSpecificationRejectedException.Reason.DELETED);
    }

    @Test
    void updateAlertTechnicalSpecificationPersistsManualReplacementAndDisablesAlert() {
        AlertService service = verificationService(false);
        service.alertTechnicalSpecificationManualValidator = mock(AlertTechnicalSpecificationManualValidator.class);
        Map<String, Object> replacement = Map.of(
                "source", "SERVICE_DATA",
                "interpreterType", "EVENT_INTERPRETER",
                "triggerType", "EVENT",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "condition", Map.of("type", "SERVICE_DATA_FIELD_MATCH", "field", "payload.ongroundServiceEvent.eventsType", "operator", "EQUALS", "value", "ARRIVING"));
        Alert alert = verifiedTechnicalSpecificationAlert(Map.of("source", "SERVICE_DATA"));
        alert.setFlgEnabled(true);
        Alert updated = verifiedTechnicalSpecificationAlert(replacement);
        updated.setFlgTechnicalspecificationedited(true);
        updated.setFlgEnabled(false);
        updated.setNumVersion(8);
        when(service.alertRepository.findAlertForTechnicalSpecification("ALRT1"))
                .thenReturn(java.util.Optional.of(alert));
        when(service.alertTechnicalSpecificationManualValidator.validate(replacement))
                .thenReturn(AlertTechnicalSpecificationManualValidator.ValidationResult.valid("EVENT_INTERPRETER"));
        when(service.alertRepository.replaceTechnicalSpecificationManually("ALRT1", replacement))
                .thenReturn(java.util.Optional.of(updated));
        AlertTechnicalSpecificationUpdateRequest request = new AlertTechnicalSpecificationUpdateRequest()
                .technicalSpecification(replacement);

        AlertTechnicalSpecificationResponse response = service.updateAlertTechnicalSpecification("ALRT1", request).orElseThrow();

        assertThat(response.getTechnicalSpecificationEdited()).isTrue();
        assertThat(response.getTechnicalSpecification()).isEqualTo(replacement);
        assertThat(response.getAgentBlueprintPreviewRegenerated()).isFalse();
        assertThat(response.getWarnings()).contains(
                "Technical specification was replaced manually; Agent Blueprint preview realignment will be handled by the dedicated generation/preview flow.",
                "Alert was disabled after manual technical specification replacement and must be explicitly enabled again.");
        verify(service.alertRepository).replaceTechnicalSpecificationManually("ALRT1", replacement);
    }

    @Test
    void updateAlertTechnicalSpecificationRejectsInvalidSpecificationWithoutPersisting() {
        AlertService service = verificationService(false);
        service.alertTechnicalSpecificationManualValidator = mock(AlertTechnicalSpecificationManualValidator.class);
        Map<String, Object> replacement = Map.of("source", "SERVICE_DATA");
        Alert alert = verifiedTechnicalSpecificationAlert(Map.of("source", "SERVICE_DATA"));
        when(service.alertRepository.findAlertForTechnicalSpecification("ALRT1"))
                .thenReturn(java.util.Optional.of(alert));
        when(service.alertTechnicalSpecificationManualValidator.validate(replacement))
                .thenReturn(AlertTechnicalSpecificationManualValidator.ValidationResult.invalid("condition is missing"));
        AlertTechnicalSpecificationUpdateRequest request = new AlertTechnicalSpecificationUpdateRequest()
                .technicalSpecification(replacement);

        assertThatThrownBy(() -> service.updateAlertTechnicalSpecification("ALRT1", request))
                .isInstanceOf(AlertTechnicalSpecificationRejectedException.class)
                .extracting(ex -> ((AlertTechnicalSpecificationRejectedException) ex).reason())
                .isEqualTo(AlertTechnicalSpecificationRejectedException.Reason.INVALID_TECHNICAL_SPECIFICATION);
        verify(service.alertRepository, never()).replaceTechnicalSpecificationManually(any(), any());
    }

    @Test
    void previewWithLlmDisabledKeepsDeterministicPath() {
        AlertRepository repository = mock(AlertRepository.class);
        AgentGenerationPreviewMapper mapper = mock(AgentGenerationPreviewMapper.class);
        LlmGateway gateway = mock(LlmGateway.class);
        Instance<LlmGateway> gatewayInstance = mock(Instance.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        service.agentGenerationPreviewMapper = mapper;
        service.llmGateway = gatewayInstance;
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(repository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(mapper.toResponse(data, null)).thenReturn(new AgentGenerationPreviewResponse());

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(mapper).toResponse(data, null);
        verify(gateway, never()).generateText(any());
        verify(repository, never()).persistValidatedAgentBlueprintPreview(any(), any());
    }

    @Test
    void previewWithValidLlmCandidatePassesCandidateToValidatedMapperPath() {
        AlertService service = llmPreviewService();
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "{\"canGenerate\":true,\"blueprint\":{\"agentName\":\"GeneratedAgent\"},\"warnings\":[]}",
                "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any()))
                .thenReturn(new AgentGenerationPreviewResponse());

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(service.agentGenerationPreviewMapper).toResponse(
                data,
                null,
                Map.of("agentName", "GeneratedAgent"),
                List.of("Agent Blueprint preview generated by LLM and validated by backend; no Agent Definition has been created."));
        verify(service.alertRepository, never()).persistValidatedAgentBlueprintPreview(any(), any());
    }

    @Test
    void previewWithInvalidLlmResponseFallsBackToDeterministicArtifacts() {
        AlertService service = llmPreviewService();
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "invalid-json", "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any()))
                .thenReturn(new AgentGenerationPreviewResponse());

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(service.agentGenerationPreviewMapper).toResponse(
                data,
                null,
                null,
                List.of("LLM Agent Blueprint generation failed or was rejected by backend validation; deterministic verified Alert artifacts were used instead."));
        verify(service.alertRepository, never()).persistValidatedAgentBlueprintPreview(any(), any());
    }

    @Test
    void previewWithValidatedLlmPersistsFinalBlueprintWhenEnabled() {
        AlertService service = llmPreviewService();
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        AgentBlueprint finalBlueprint = new AgentBlueprint()
                .agentName("GeneratedAgent")
                .parameters(Map.of(
                        "generationContext", Map.of("previewSource", "LLM_VALIDATED"),
                        "runtimeContract", Map.of("source", "SERVICE_DATA"),
                        "generationReadiness", Map.of("readyForAgentDefinition", true)));
        AgentGenerationPreviewResponse response = new AgentGenerationPreviewResponse()
                .blueprint(finalBlueprint)
                .warnings(List.of(
                        AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING,
                        AgentGenerationPreviewMapper.DSL_DIAGNOSTIC_WARNING));
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "{\"canGenerate\":true,\"blueprint\":{\"agentName\":\"GeneratedAgent\"},\"warnings\":[]}",
                "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any())).thenReturn(response);
        when(service.alertRepository.persistValidatedAgentBlueprintPreview(any(), any())).thenReturn(true);

        AgentGenerationPreviewResponse result = service.previewAgentGenerationForAlert("ALRT1", null).orElseThrow();

        verify(service.alertRepository).persistValidatedAgentBlueprintPreview(
                org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.argThat(blueprint -> "GeneratedAgent".equals(blueprint.get("agentName"))
                        && ((Map<?, ?>) blueprint.get("parameters")).containsKey("generationContext")
                        && ((Map<?, ?>) blueprint.get("parameters")).containsKey("runtimeContract")
                        && ((Map<?, ?>) blueprint.get("parameters")).containsKey("generationReadiness")));
        assertThat(result.getWarnings()).containsExactly(
                AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING,
                AgentGenerationPreviewMapper.PERSISTED_LLM_PREVIEW_WARNING,
                AgentGenerationPreviewMapper.DSL_DIAGNOSTIC_WARNING);
    }

    @Test
    void previewDoesNotPersistAfterLlmFallbackEvenWhenEnabled() {
        AlertService service = llmPreviewService();
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "invalid-json", "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any()))
                .thenReturn(new AgentGenerationPreviewResponse());

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(service.alertRepository, never()).persistValidatedAgentBlueprintPreview(any(), any());
    }

    @Test
    void previewDoesNotPersistWhenParsedLlmBlueprintIsRejectedByValidation() {
        AlertService service = llmPreviewService();
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "{\"canGenerate\":true,\"blueprint\":{\"requiredSources\":[\"EXTERNAL_HTTP\"]},\"warnings\":[]}",
                "FAKE", "fake-model", null, null, null));
        org.mockito.Mockito.doThrow(new AlertAgentGenerationPreviewRejectedException(
                        AlertAgentGenerationPreviewRejectedException.Reason.INVALID_BLUEPRINT))
                .when(service.agentGenerationPreviewMapper).toResponse(
                org.mockito.ArgumentMatchers.eq(data),
                org.mockito.ArgumentMatchers.isNull(),
                any(),
                any());
        org.mockito.Mockito.doReturn(new AgentGenerationPreviewResponse())
                .when(service.agentGenerationPreviewMapper).toResponse(data, null, null, List.of(
                        "LLM Agent Blueprint generation failed or was rejected by backend validation; deterministic verified Alert artifacts were used instead."));

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(service.alertRepository, never()).persistValidatedAgentBlueprintPreview(any(), any());
        verify(service.agentGenerationPreviewMapper).toResponse(data, null, null, List.of(
                "LLM Agent Blueprint generation failed or was rejected by backend validation; deterministic verified Alert artifacts were used instead."));
    }

    @Test
    void previewPersistenceRejectsAlertThatIsNoLongerVerifiedWithoutFallback() {
        AlertService service = llmPreviewService();
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "{\"canGenerate\":true,\"blueprint\":{\"agentName\":\"GeneratedAgent\"},\"warnings\":[]}",
                "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any()))
                .thenReturn(new AgentGenerationPreviewResponse()
                        .blueprint(new AgentBlueprint().agentName("GeneratedAgent"))
                        .warnings(List.of(AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING)));
        when(service.alertRepository.persistValidatedAgentBlueprintPreview(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.previewAgentGenerationForAlert("ALRT1", null))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class)
                .extracting(ex -> ((AlertAgentGenerationPreviewRejectedException) ex).reason())
                .isEqualTo(AlertAgentGenerationPreviewRejectedException.Reason.NOT_VERIFIED);
    }

    @SuppressWarnings("unchecked")
    private AlertService llmPreviewService() {
        AlertService service = new AlertService();
        service.alertRepository = mock(AlertRepository.class);
        service.agentGenerationPreviewMapper = mock(AgentGenerationPreviewMapper.class);
        service.agentGenerationPromptBuilder = mock(AgentGenerationPromptBuilder.class);
        service.agentGenerationLlmResponseParser = new AgentGenerationLlmResponseParser();
        service.llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.get()).thenReturn(gateway);
        service.agentGenerationPreviewUseLlm = true;
        service.agentGenerationPreviewFallbackToDeterministicOnLlmError = true;
        return service;
    }

    @SuppressWarnings("unchecked")
    private AlertService verificationService(boolean fallbackEnabled) {
        AlertService service = new AlertService();
        service.alertRepository = mock(AlertRepository.class);
        when(service.alertRepository.getAlertVerificationPromptData("ALRT1"))
                .thenReturn(java.util.Optional.of(new AlertVerificationPromptData("ALRT1", "Alert", null, "Prompt")));
        when(service.alertRepository.verifyAlert(any(), any(), any())).thenReturn(java.util.Optional.empty());
        service.alertVerificationPromptBuilder = mock(AlertVerificationPromptBuilder.class);
        when(service.alertVerificationPromptBuilder.build(any())).thenReturn(
                new LlmRequest(AiUseCase.ALERT_VERIFY, "system", "user", "gpt-4.1-mini", 0.1, 5000, "ALRT1"));
        service.alertVerificationLlmResponseParser = new AlertVerificationLlmResponseParser();
        service.alertVerificationOutcomeValidator = mock(AlertVerificationOutcomeValidator.class);
        when(service.alertVerificationOutcomeValidator.validate(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service.alertVerificationMockEngine = mock(AlertVerificationMockEngine.class);
        service.alertLocationMentionExtractor = new SimpleAlertLocationMentionExtractor();
        service.alertLocationResolverService = new AlertLocationResolverService();
        service.llmGateway = mock(Instance.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(mock(LlmGateway.class));
        service.aiConfiguration = mock(AiConfiguration.class);
        when(service.aiConfiguration.provider()).thenReturn("openai");
        AiConfiguration.AlertVerify alertVerify = mock(AiConfiguration.AlertVerify.class);
        when(alertVerify.simulateProviderTimeout()).thenReturn(false);
        when(service.aiConfiguration.alertVerify()).thenReturn(alertVerify);
        service.fallbackOnInvalidLlm = fallbackEnabled;
        service.scheduledVerifyEnabled = false;
        return service;
    }

    private Alert verifiedTechnicalSpecificationAlert(Object technicalSpecification) {
        Alert alert = new Alert();
        alert.setCodAlert("ALRT1");
        alert.setDscName("Verified alert");
        alert.setJsnTechnicalspecification(technicalSpecification);
        alert.setDscInputmodel("ServiceDataV2");
        alert.setDscOutputmodel("AgentOutput.CANDIDATE_SUGGESTION");

        it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus status =
                new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus();
        status.setSglStatus("VERIFIED");
        alert.setSglStatus(status);

        it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVerificationStatus verificationStatus =
                new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVerificationStatus();
        verificationStatus.setSglVerificationstatus("VERIFIED");
        alert.setSglVerificationstatus(verificationStatus);

        it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType interpreterType =
                new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType();
        interpreterType.setSglInterpretertype("EVENT_INTERPRETER");
        alert.setSglInterpretertype(interpreterType);

        return alert;
    }

    private AlertVerificationOutcome rejectedVerificationOutcome(String provider) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED, "Rejected.", "Rejected.", 0.0, provider, "mock-model",
                "alert-verify-mvp-v1", List.of(), null, null, null, null, null, List.of(), List.of(),
                null, null, null, List.of(), List.of());
    }

    private AlertVerificationOutcome scheduledVerifiedOutcome() {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED,
                "Scheduled verified.",
                null,
                0.91,
                "provider",
                "scheduled-model",
                "alert-scheduled-verify-mvp-v1",
                List.of("SERVICE_DATA"),
                "SCHEDULED_INTERPRETER",
                "ServiceDataStopPointJourneysV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "SCHEDULE",
                "SCHEDULED_SNAPSHOT_MATCH",
                List.of(),
                List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                Map.of(
                        "schemaVersion", "iia.alert.technical-specification/v2",
                        "source", "SERVICE_DATA",
                        "interpreterType", "SCHEDULED_INTERPRETER",
                        "accessMode", "SERVICE_DATA_API_SNAPSHOT"),
                Map.of(
                        "schemaVersion", "iia.agent.blueprint/v1",
                        "agentName", "ScheduledServiceDataSnapshotAlertAgent"),
                Map.of(
                        "requirements", List.of(),
                        "allRequiredRequirementsMapped", true),
                List.of(),
                List.of("No executable code generated."));
    }

    private AlertVerificationOutcome scheduledRejectedOutcome(String reason) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                "Scheduled rejected.",
                reason,
                0.0,
                "provider",
                "scheduled-model",
                "alert-scheduled-verify-mvp-v1",
                List.of("SERVICE_DATA"),
                "SCHEDULED_INTERPRETER",
                "ServiceDataStopPointJourneysV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "SCHEDULE",
                "SCHEDULED_SNAPSHOT_MATCH",
                List.of(),
                List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                null,
                null,
                Map.of(
                        "requirements", List.of(),
                        "allRequiredRequirementsMapped", false),
                List.of(),
                List.of("No executable code generated."));
    }

    private AlertRouteUnderstandingResult eventRoute() {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.EVENT_INTERPRETER,
                AlertRouteAccessMode.KAFKA_EVENT,
                AlertRouteIntentKind.EVENT_OCCURRENCE,
                AlertRouteOutputMode.ON_MATCH,
                false,
                false,
                true,
                false,
                false,
                false,
                0.86,
                "Event ServiceData route.",
                null,
                List.of());
    }

    private AlertRouteUnderstandingResult scheduledRoute() {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.SCHEDULED_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                AlertRouteIntentKind.SNAPSHOT_CONDITION,
                AlertRouteOutputMode.ON_MATCH,
                true,
                true,
                false,
                true,
                true,
                false,
                0.95,
                "Scheduled ServiceData snapshot route.",
                null,
                List.of());
    }

    private ScheduledAlertLocationUnderstandingResult scheduledUnderstanding(
            ScheduledAlertMonitoringScope scope,
            ScheduledAlertLocationMention... mentions) {
        return new ScheduledAlertLocationUnderstandingResult(
                mentions.length > 0,
                "it",
                scope,
                List.of(mentions),
                List.of(),
                List.of());
    }

    private ScheduledAlertLocationMention scheduledMention(
            String rawText,
            ScheduledAlertLocationRole role,
            boolean requiredForApiQuery) {
        return new ScheduledAlertLocationMention(
                rawText,
                rawText,
                role,
                requiredForApiQuery
                        ? ScheduledAlertLocationRelation.SERVICE_DATA_API_QUERY_STOP_POINT
                        : ScheduledAlertLocationRelation.UNKNOWN,
                requiredForApiQuery,
                true,
                ScheduledAlertLocationPolarity.INCLUDE,
                "G1",
                0.95);
    }

    private ScheduledServiceDataLocationContext scheduledContext(
            ScheduledAlertMonitoringScope scope,
            List<ScheduledServiceDataResolvedLocation> monitoredLocations,
            List<ScheduledServiceDataResolvedLocation> filterLocations,
            List<String> serviceDataApiStopPoints,
            boolean requiresAllKnownStopPoints,
            boolean hasUnresolvedRequiredMonitoredLocations,
            List<String> unresolvedRequiredMonitoredLocationTexts,
            List<String> warnings) {
        return new ScheduledServiceDataLocationContext(
                scope,
                monitoredLocations,
                filterLocations,
                List.of(),
                serviceDataApiStopPoints,
                requiresAllKnownStopPoints,
                hasUnresolvedRequiredMonitoredLocations,
                unresolvedRequiredMonitoredLocationTexts,
                warnings,
                new ScheduledServiceDataApiQueryContext(scope, serviceDataApiStopPoints, requiresAllKnownStopPoints));
    }

    private ScheduledServiceDataResolvedLocation resolvedScheduledLocation(
            String rawText,
            ScheduledAlertLocationRole role,
            List<String> selectedPointIds) {
        return new ScheduledServiceDataResolvedLocation(
                rawText,
                rawText,
                role,
                ScheduledAlertLocationPolarity.INCLUDE,
                role == ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                selectedPointIds,
                List.of(),
                false,
                false,
                role == ScheduledAlertLocationRole.MONITORED_STOP_POINT ? List.of("body.stopPoints[]") : List.of(),
                "");
    }

    private ScheduledServiceDataResolvedLocation unresolvedScheduledLocation(
            String rawText,
            ScheduledAlertLocationRole role,
            boolean fallbackAllowed) {
        return new ScheduledServiceDataResolvedLocation(
                rawText,
                rawText,
                role,
                ScheduledAlertLocationPolarity.INCLUDE,
                role == ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                true,
                ScheduledServiceDataLocationResolutionStatus.UNRESOLVED,
                List.of(),
                List.of(),
                fallbackAllowed,
                fallbackAllowed,
                role == ScheduledAlertLocationRole.MONITORED_STOP_POINT ? List.of("body.stopPoints[]") : List.of(),
                "");
    }

    private LlmRequest llmRequest() {
        return new LlmRequest(AiUseCase.AGENT_BLUEPRINT_GENERATE, "system", "user", "fake-model", 0.1, 2500, "ALRT1");
    }

    private AlertAgentGenerationPreviewData validPreviewData() {
        return previewData(
                "VERIFIED",
                "VERIFIED",
                Map.of("source", "SERVICE_DATA"),
                Map.of("agentName", "Agent"));
    }

    private AlertCreateRequest createRequest(boolean verifyImmediately, boolean enableAfterVerification) {
        return new AlertCreateRequest()
                .name("Alert")
                .prompt("Create a suggestion when a journey is cancelled at Milano Malpensa T1.")
                .verifyImmediately(verifyImmediately)
                .enableAfterVerification(enableAfterVerification);
    }

    private AlertUpdateRequest updateRequest(String prompt) {
        return new AlertUpdateRequest()
                .name("Updated alert")
                .description("Updated description")
                .prompt(prompt)
                .verifyImmediately(true)
                .enableAfterVerification(true);
    }

    private AlertDetail verifiedAlert(boolean enabled) {
        return new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .enabled(enabled)
                .verification(new AlertVerificationResult().status(AlertVerificationStatus.VERIFIED));
    }

    private AlertAgentGenerationPreviewData previewData(
            String status,
            String verificationStatus,
            Map<String, Object> technicalSpecification,
            Map<String, Object> blueprint) {
        return new AlertAgentGenerationPreviewData(
                "ALRT1",
                "Alert",
                status,
                verificationStatus,
                false,
                null,
                1,
                "Create a suggestion when a journey is cancelled.",
                "Verified.",
                null,
                null,
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification,
                blueprint,
                List.of("JOURNEY_CANCELLED"),
                List.of(),
                List.of());
    }
}
