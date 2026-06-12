package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentContinuousActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDailyWindowActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DayOfWeek;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentProfileRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentActivationType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactSignatureStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVerificationStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentDefinitionServiceTest {

    private static final String ALERT_ID = "ALRT1";
    private static final String PROFILE_ID = "MEDIUM";
    private static final String COMPILE_NOT_IMPLEMENTED =
            "Agent compilation pipeline is not implemented yet. Create the Agent Definition with compileImmediately=false.";

    @Test
    void createsDraftEventAgentDefinitionWhenCompileImmediatelyFalse() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentProfileRepository profileRepository = mock(AgentProfileRepository.class);
        AgentDefinitionService service = service(definitionRepository, profileRepository);
        stubReferences(definitionRepository);
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(eventTechnicalSpecification())));
        when(profileRepository.findByProfileId(PROFILE_ID)).thenReturn(Optional.of(profile(true)));
        when(definitionRepository.create(any(), anyList(), anyList(), anyList()))
                .thenAnswer(invocation -> created(invocation.getArgument(0)));

        var detail = service.createAgentDefinition(baseRequest(false, continuousPolicy()));

        ArgumentCaptor<AgentDefinition> definitionCaptor = ArgumentCaptor.forClass(AgentDefinition.class);
        verify(definitionRepository).create(
                definitionCaptor.capture(),
                eq(List.of("SERVICE_DATA")),
                eq(List.of()),
                eq(List.of()));
        AgentDefinition definition = definitionCaptor.getValue();
        assertPersistedDraft(definition, "ServiceDataV2");
        assertThat(detail.getId()).isEqualTo("AGDF1");
        assertThat(detail.getStatus().toString()).isEqualTo("DRAFT");
        assertThat(detail.getInterpreterType().toString()).isEqualTo("EVENT_INTERPRETER");
        assertThat(detail.getTriggerType()).isEqualTo("EVENT");
        assertThat(detail.getInputModel()).isEqualTo("ServiceDataV2");
        assertThat(detail.getOutputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(detail.getRuntimeContract()).isNotNull();
        assertThat(detail.getActivationPolicy()).isNotNull();
        assertThat(detail.getProfile().getId()).isEqualTo(PROFILE_ID);
        assertThat(detail.getAlertVersion()).isEqualTo(3);
    }

    @Test
    void getsPersistedEventAgentDefinitionDetail() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        when(definitionRepository.findByDefinitionId("AGDF1"))
                .thenReturn(Optional.of(persistedDefinition(eventTechnicalSpecification(), "ServiceDataV2", List.of())));

        var detail = service.getAgentDefinition(" AGDF1 ");

        assertThat(detail.getId()).isEqualTo("AGDF1");
        assertThat(detail.getStatus().toString()).isEqualTo("DRAFT");
        assertThat(detail.getAlert().getId()).isEqualTo(ALERT_ID);
        assertThat(detail.getAlert().getName()).isEqualTo("Verified alert");
        assertThat(detail.getProfile().getId()).isEqualTo(PROFILE_ID);
        assertThat(detail.getInterpreterType().toString()).isEqualTo("EVENT_INTERPRETER");
        assertThat(detail.getTriggerType()).isEqualTo("EVENT");
        assertThat(detail.getInputModel()).isEqualTo("ServiceDataV2");
        assertThat(detail.getOutputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(detail.getRuntimeContract().getAllowedTools()).isEmpty();
        assertThat(detail.getCompilation()).isNull();
        assertThat(detail.getLatestRun()).isNull();
    }

    @Test
    void getsPersistedScheduledAgentDefinitionDetail() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        when(definitionRepository.findByDefinitionId("AGDF1"))
                .thenReturn(Optional.of(persistedDefinition(
                        scheduledTechnicalSpecification(600, 1),
                        "ServiceDataStopPointJourneysV2",
                        List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys"))));

        var detail = service.getAgentDefinition("AGDF1");

        assertThat(detail.getInterpreterType().toString()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(detail.getTriggerType()).isEqualTo("SCHEDULE");
        assertThat(detail.getInputModel()).isEqualTo("ServiceDataStopPointJourneysV2");
        assertThat(detail.getRuntimeContract().getAllowedTools())
                .extracting(tool -> tool.getToolName())
                .containsExactly("SERVICE_DATA_API.POST_/v2/stoppointjourneys");
    }

    @Test
    void rejectsBlankAgentDefinitionIdOnGet() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));

        assertThatThrownBy(() -> service.getAgentDefinition(" "))
                .isInstanceOf(AgentDefinitionInvalidRequestException.class)
                .extracting(ex -> ((AgentDefinitionInvalidRequestException) ex).source())
                .isEqualTo("agentDefinitionId");
        verify(definitionRepository, never()).findByDefinitionId(any());
    }

    @Test
    void rejectsMissingAgentDefinitionOnGet() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        when(definitionRepository.findByDefinitionId("AGDF404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAgentDefinition("AGDF404"))
                .isInstanceOf(AgentDefinitionNotFoundException.class)
                .extracting(ex -> ((AgentDefinitionNotFoundException) ex).source())
                .isEqualTo("agentDefinitionId");
    }

    @Test
    void rejectsCompileImmediatelyTrueBeforeLoadingAlert() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));

        assertThatThrownBy(() -> service.createAgentDefinition(baseRequest(true, continuousPolicy())))
                .isInstanceOf(AgentDefinitionCreateRejectedException.class)
                .hasMessage(COMPILE_NOT_IMPLEMENTED)
                .extracting(ex -> ((AgentDefinitionCreateRejectedException) ex).reason())
                .isEqualTo(AgentDefinitionCreateRejectedException.Reason.COMPILATION_NOT_IMPLEMENTED);
        verify(definitionRepository, never()).findAlert(any());
    }

    @Test
    void rejectsNullCompileImmediatelyAsContractDefaultTrue() {
        AgentDefinitionService service = service(mock(AgentDefinitionRepository.class), mock(AgentProfileRepository.class));
        AgentDefinitionCreateRequest request = baseRequest(false, continuousPolicy());
        request.setCompileImmediately(null);

        assertThatThrownBy(() -> service.createAgentDefinition(request))
                .isInstanceOf(AgentDefinitionCreateRejectedException.class)
                .hasMessage(COMPILE_NOT_IMPLEMENTED);
    }

    @Test
    void rejectsMissingAlert() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAgentDefinition(baseRequest(false, continuousPolicy())))
                .isInstanceOf(AgentDefinitionNotFoundException.class)
                .extracting(ex -> ((AgentDefinitionNotFoundException) ex).source())
                .isEqualTo("alertId");
    }

    @Test
    void rejectsMissingAgentProfile() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentProfileRepository profileRepository = mock(AgentProfileRepository.class);
        AgentDefinitionService service = service(definitionRepository, profileRepository);
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(eventTechnicalSpecification())));
        when(profileRepository.findByProfileId(PROFILE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAgentDefinition(baseRequest(false, continuousPolicy())))
                .isInstanceOf(AgentDefinitionNotFoundException.class)
                .extracting(ex -> ((AgentDefinitionNotFoundException) ex).source())
                .isEqualTo("agentProfileId");
    }

    @Test
    void rejectsAlertNotVerified() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        Alert alert = verifiedAlert(eventTechnicalSpecification());
        alert.setSglStatus(status("VERIFYING"));
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(alert));

        assertRejected(service, AgentDefinitionCreateRejectedException.Reason.ALERT_NOT_VERIFIED);
    }

    @Test
    void rejectsDeletedAlertStatusOrDeletedAt() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        Alert alert = verifiedAlert(eventTechnicalSpecification());
        alert.setDtDeletedat(OffsetDateTime.parse("2026-06-12T10:00:00Z"));
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(alert));

        assertRejected(service, AgentDefinitionCreateRejectedException.Reason.ALERT_DELETED);
    }

    @Test
    void rejectsAlertVersionMismatch() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        AgentDefinitionCreateRequest request = baseRequest(false, continuousPolicy());
        request.setAlertVersion(2);
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(eventTechnicalSpecification())));

        assertThatThrownBy(() -> service.createAgentDefinition(request))
                .isInstanceOf(AgentDefinitionCreateRejectedException.class)
                .extracting(ex -> ((AgentDefinitionCreateRejectedException) ex).reason())
                .isEqualTo(AgentDefinitionCreateRejectedException.Reason.ALERT_VERSION_MISMATCH);
    }

    @Test
    void rejectsMissingOrEmptyTechnicalSpecification() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(Map.of())));

        assertRejected(service, AgentDefinitionCreateRejectedException.Reason.MISSING_TECHNICAL_SPECIFICATION);
    }

    @Test
    void rejectsTechnicalSpecificationWithoutRuntimeContractFields() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(Map.of("source", "SERVICE_DATA"))));

        assertRejected(service, AgentDefinitionCreateRejectedException.Reason.UNSUPPORTED_TECHNICAL_SPECIFICATION);
    }

    @Test
    void rejectsDisabledProfile() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentProfileRepository profileRepository = mock(AgentProfileRepository.class);
        AgentDefinitionService service = service(definitionRepository, profileRepository);
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(eventTechnicalSpecification())));
        when(profileRepository.findByProfileId(PROFILE_ID)).thenReturn(Optional.of(profile(false)));

        assertRejected(service, AgentDefinitionCreateRejectedException.Reason.PROFILE_DISABLED);
    }

    @Test
    void rejectsJavaTemplateGenerationMode() {
        AgentDefinitionService service = service(mock(AgentDefinitionRepository.class), mock(AgentProfileRepository.class));
        AgentDefinitionCreateRequest request = baseRequest(false, continuousPolicy());
        request.setGenerationMode(AgentGenerationMode.JAVA_TEMPLATE);

        assertThatThrownBy(() -> service.createAgentDefinition(request))
                .isInstanceOf(AgentDefinitionCreateRejectedException.class)
                .extracting(ex -> ((AgentDefinitionCreateRejectedException) ex).reason())
                .isEqualTo(AgentDefinitionCreateRejectedException.Reason.UNSUPPORTED_GENERATION_MODE);
    }

    @Test
    void rejectsContinuousPolicyWithInvalidTimezone() {
        AgentDefinitionService service = service(mock(AgentDefinitionRepository.class), mock(AgentProfileRepository.class));
        AgentContinuousActivationPolicy policy = continuousPolicy();
        policy.setTimezone("Mars/Base");

        assertThatThrownBy(() -> service.createAgentDefinition(baseRequest(false, policy)))
                .isInstanceOf(AgentDefinitionInvalidRequestException.class)
                .extracting(ex -> ((AgentDefinitionInvalidRequestException) ex).source())
                .isEqualTo("activationPolicy.timezone");
    }

    @Test
    void rejectsContinuousPolicyWithInvalidDateRange() {
        AgentDefinitionService service = service(mock(AgentDefinitionRepository.class), mock(AgentProfileRepository.class));
        AgentContinuousActivationPolicy policy = continuousPolicy();
        policy.setValidTo(policy.getValidFrom());

        assertThatThrownBy(() -> service.createAgentDefinition(baseRequest(false, policy)))
                .isInstanceOf(AgentDefinitionInvalidRequestException.class)
                .hasMessageContaining("CONTINUOUS activation policy requires validFrom, validTo and validTo > validFrom.");
    }

    @Test
    void rejectsDailyWindowPolicyWithInvalidTimeRange() {
        AgentDefinitionService service = service(mock(AgentDefinitionRepository.class), mock(AgentProfileRepository.class));
        AgentDailyWindowActivationPolicy policy = dailyPolicy();
        policy.setDailyEndTime("07:00:00");

        assertThatThrownBy(() -> service.createAgentDefinition(baseRequest(false, policy)))
                .isInstanceOf(AgentDefinitionInvalidRequestException.class)
                .hasMessageContaining("DAILY_WINDOW activation policy requires dailyEndTime > dailyStartTime.");
    }

    @Test
    void createsDailyWindowDefinitionAndPassesDaysOfWeekToRepository() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentProfileRepository profileRepository = mock(AgentProfileRepository.class);
        AgentDefinitionService service = service(definitionRepository, profileRepository);
        stubReferences(definitionRepository);
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(eventTechnicalSpecification())));
        when(profileRepository.findByProfileId(PROFILE_ID)).thenReturn(Optional.of(profile(true)));
        when(definitionRepository.create(any(), anyList(), anyList(), anyList()))
                .thenAnswer(invocation -> created(invocation.getArgument(0)));

        service.createAgentDefinition(baseRequest(false, dailyPolicy()));

        verify(definitionRepository).create(any(), eq(List.of("SERVICE_DATA")), eq(List.of("MONDAY", "TUESDAY")), eq(List.of()));
    }

    @Test
    void createsScheduledDefinitionWhenLimitsAreWithinMvpBounds() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentProfileRepository profileRepository = mock(AgentProfileRepository.class);
        AgentDefinitionService service = service(definitionRepository, profileRepository);
        stubReferences(definitionRepository);
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(scheduledTechnicalSpecification(300, 50))));
        when(profileRepository.findByProfileId(PROFILE_ID)).thenReturn(Optional.of(profile(true)));
        when(definitionRepository.create(any(), anyList(), anyList(), anyList()))
                .thenAnswer(invocation -> created(invocation.getArgument(0)));

        service.createAgentDefinition(baseRequest(false, continuousPolicy()));

        ArgumentCaptor<AgentDefinition> definitionCaptor = ArgumentCaptor.forClass(AgentDefinition.class);
        verify(definitionRepository).create(
                definitionCaptor.capture(),
                eq(List.of("SERVICE_DATA")),
                eq(List.of()),
                eq(List.of("SERVICE_DATA_API.POST_/v2/stoppointjourneys")));
        assertPersistedDraft(definitionCaptor.getValue(), "ServiceDataStopPointJourneysV2");
        assertThat(definitionCaptor.getValue().getJsnAllowedtools())
                .containsExactly("SERVICE_DATA_API.POST_/v2/stoppointjourneys");
    }

    @Test
    void rejectsScheduledFrequencyBelowMvpMinimum() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(scheduledTechnicalSpecification(299, 1))));

        assertRejected(service, AgentDefinitionCreateRejectedException.Reason.SCHEDULE_TOO_AGGRESSIVE);
    }

    @Test
    void rejectsScheduledStopPointsAboveMvpMaximum() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentDefinitionService service = service(definitionRepository, mock(AgentProfileRepository.class));
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(verifiedAlert(scheduledTechnicalSpecification(300, 51))));

        assertRejected(service, AgentDefinitionCreateRejectedException.Reason.TOO_MANY_STOP_POINTS);
    }

    @Test
    void addsWarningWhenTechnicalSpecificationWasEdited() {
        AgentDefinitionRepository definitionRepository = mock(AgentDefinitionRepository.class);
        AgentProfileRepository profileRepository = mock(AgentProfileRepository.class);
        AgentDefinitionService service = service(definitionRepository, profileRepository);
        stubReferences(definitionRepository);
        Alert alert = verifiedAlert(eventTechnicalSpecification());
        alert.setFlgTechnicalspecificationedited(true);
        when(definitionRepository.findAlert(ALERT_ID)).thenReturn(Optional.of(alert));
        when(profileRepository.findByProfileId(PROFILE_ID)).thenReturn(Optional.of(profile(true)));
        when(definitionRepository.create(any(), anyList(), anyList(), anyList()))
                .thenAnswer(invocation -> created(invocation.getArgument(0)));

        service.createAgentDefinition(baseRequest(false, continuousPolicy()));

        ArgumentCaptor<AgentDefinition> definitionCaptor = ArgumentCaptor.forClass(AgentDefinition.class);
        verify(definitionRepository).create(definitionCaptor.capture(), anyList(), anyList(), anyList());
        assertThat(definitionCaptor.getValue().getJsnGenerationwarnings())
                .contains("Technical specification was manually edited; Agent Definition was created from current technicalSpecification and existing blueprint may be derived.");
    }

    private void assertRejected(AgentDefinitionService service, AgentDefinitionCreateRejectedException.Reason reason) {
        assertThatThrownBy(() -> service.createAgentDefinition(baseRequest(false, continuousPolicy())))
                .isInstanceOf(AgentDefinitionCreateRejectedException.class)
                .extracting(ex -> ((AgentDefinitionCreateRejectedException) ex).reason())
                .isEqualTo(reason);
    }

    private void assertPersistedDraft(AgentDefinition definition, String inputModel) {
        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("DRAFT");
        assertThat(definition.getSglArtifacttype().getSglArtifacttype()).isEqualTo("NONE");
        assertThat(definition.getCodLatestcompilation()).isNull();
        assertThat(definition.getSglLatestcompilationstatus()).isNull();
        assertThat(definition.getJsnRuntimecontract()).isNotEmpty();
        assertThat(definition.getDscInputmodel()).isEqualTo(inputModel);
        assertThat(definition.getDscOutputmodel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(definition.getJsnRequiredsources()).contains("SERVICE_DATA");
    }

    private AgentDefinitionService service(AgentDefinitionRepository definitionRepository, AgentProfileRepository profileRepository) {
        AgentDefinitionService service = new AgentDefinitionService();
        service.agentDefinitionRepository = definitionRepository;
        service.agentProfileRepository = profileRepository;
        service.agentProfileMapper = new AgentProfileMapper();
        AgentDefinitionMapper mapper = new AgentDefinitionMapper();
        mapper.agentProfileMapper = new AgentProfileMapper();
        service.agentDefinitionMapper = mapper;
        return service;
    }

    private void stubReferences(AgentDefinitionRepository repository) {
        when(repository.statusReference("DRAFT")).thenReturn(statusRef("DRAFT"));
        when(repository.generationModeReference("AUTO")).thenReturn(generationModeRef("AUTO"));
        when(repository.activationTypeReference(any())).thenAnswer(invocation -> activationRef(invocation.getArgument(0)));
        when(repository.artifactTypeReference("NONE")).thenReturn(artifactTypeRef("NONE"));
        when(repository.signatureStatusReference("NOT_SIGNED")).thenReturn(signatureRef("NOT_SIGNED"));
        when(repository.complexityReference(any())).thenAnswer(invocation -> complexityRef(invocation.getArgument(0)));
    }

    private AgentDefinition created(AgentDefinition definition) {
        definition.setCodAgentdefinition("AGDF1");
        return definition;
    }

    private AgentDefinition persistedDefinition(
            Map<String, Object> technicalSpecification,
            String inputModel,
            List<String> allowedTools) {
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition("AGDF1");
        definition.setDscName("Agent Definition Test");
        definition.setDscDescription("Persisted Agent Definition");
        definition.setCodAlert(verifiedAlert(technicalSpecification));
        definition.setNumAlertversion(3);
        definition.setCodAgentprofile(profile(true));
        definition.setSglStatus(statusRef("DRAFT"));
        definition.setSglGenerationmode(generationModeRef("AUTO"));
        definition.setJsnActivationpolicy(Map.of(
                "type", "CONTINUOUS",
                "timezone", "Europe/Rome",
                "validFrom", "2026-06-12T10:00:00+02:00",
                "validTo", "2026-12-31T23:59:59+01:00"));
        definition.setSglActivationtype(activationRef("CONTINUOUS"));
        definition.setJsnBlueprint(Map.of("schemaVersion", "iia.agent.blueprint/v1", "agentName", "Agent Definition Test"));
        definition.setSglArtifacttype(artifactTypeRef("NONE"));
        definition.setSglSignaturestatus(signatureRef("NOT_SIGNED"));
        definition.setDscInputmodel(inputModel);
        definition.setDscOutputmodel("AgentOutput.CANDIDATE_SUGGESTION");
        definition.setJsnAllowedtools(allowedTools.stream().map(Object.class::cast).toList());
        definition.setDscNetworkpolicy("TOOL_GATEWAY_ONLY");
        definition.setJsnRuntimecontract(runtimeContract(
                stringValue(technicalSpecification.get("interpreterType")),
                stringValue(technicalSpecification.get("triggerType")),
                inputModel,
                stringValue(technicalSpecification.get("evaluationMode")),
                allowedTools));
        definition.setDtCreatedat(OffsetDateTime.parse("2026-06-12T10:00:00Z"));
        definition.setDtUpdatedat(OffsetDateTime.parse("2026-06-12T10:01:00Z"));
        return definition;
    }

    private Map<String, Object> runtimeContract(
            String interpreterType,
            String triggerType,
            String inputModel,
            String evaluationMode,
            List<String> allowedTools) {
        return Map.of(
                "runtimeExecutionModel", "STANDARD_DSL_EVALUATOR",
                "interpreterType", interpreterType,
                "triggerType", triggerType,
                "inputModel", inputModel,
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", evaluationMode,
                "allowedTools", allowedTools.stream()
                        .map(tool -> Map.of("toolName", tool, "operations", List.of(tool)))
                        .toList(),
                "networkPolicy", "TOOL_GATEWAY_ONLY",
                "forbiddenCapabilities", List.of(
                        "ARBITRARY_CODE_EXECUTION",
                        "EXTERNAL_HTTP",
                        "DB_QUERY",
                        "FILESYSTEM",
                        "SHELL"),
                "orchestratorCompatibility", Map.of(
                        "minimumRuntimeVersion", "1.0.0",
                        "runtimeClass", "STANDARD_DSL_RUNTIME"));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private AgentDefinitionCreateRequest baseRequest(boolean compileImmediately, AgentActivationPolicy policy) {
        return new AgentDefinitionCreateRequest()
                .alertId(ALERT_ID)
                .agentProfileId(PROFILE_ID)
                .name("Agent Definition Test")
                .generationMode(AgentGenerationMode.AUTO)
                .compileImmediately(compileImmediately)
                .activationPolicy(policy);
    }

    private AgentContinuousActivationPolicy continuousPolicy() {
        return new AgentContinuousActivationPolicy()
                .type(AgentActivationPolicy.TypeEnum.CONTINUOUS)
                .timezone("Europe/Rome")
                .validFrom(OffsetDateTime.parse("2026-06-12T10:00:00+02:00"))
                .validTo(OffsetDateTime.parse("2026-12-31T23:59:59+01:00"));
    }

    private AgentDailyWindowActivationPolicy dailyPolicy() {
        return new AgentDailyWindowActivationPolicy()
                .type(AgentActivationPolicy.TypeEnum.DAILY_WINDOW)
                .timezone("Europe/Rome")
                .validFromDate(LocalDate.parse("2026-06-12"))
                .validToDate(LocalDate.parse("2026-12-31"))
                .dailyStartTime("07:00:00")
                .dailyEndTime("10:30:00")
                .daysOfWeek(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
    }

    private Alert verifiedAlert(Map<String, Object> technicalSpecification) {
        Alert alert = new Alert();
        alert.setCodAlert(ALERT_ID);
        alert.setDscName("Verified alert");
        alert.setDscDescription("Verified alert description");
        alert.setNumVersion(3);
        alert.setSglStatus(status("VERIFIED"));
        alert.setSglVerificationstatus(verificationStatus("VERIFIED"));
        alert.setFlgTechnicalspecificationedited(null);
        alert.setJsnTechnicalspecification(technicalSpecification);
        alert.setJsnAgentblueprintpreview(null);
        return alert;
    }

    private Map<String, Object> eventTechnicalSpecification() {
        return Map.of(
                "source", "SERVICE_DATA",
                "interpreterType", "EVENT_INTERPRETER",
                "triggerType", "EVENT",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "STATELESS_EVENT_MATCH");
    }

    private Map<String, Object> scheduledTechnicalSpecification(int frequencySeconds, int stopPointCount) {
        List<String> stopPoints = java.util.stream.IntStream.range(0, stopPointCount)
                .mapToObj(index -> "STOP" + index)
                .toList();
        return Map.of(
                "source", "SERVICE_DATA",
                "interpreterType", "SCHEDULED_INTERPRETER",
                "triggerType", "SCHEDULE",
                "inputModel", "ServiceDataStopPointJourneysV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "schedule", Map.of("frequencySeconds", frequencySeconds),
                "serviceDataQuery", Map.of("stopPoints", stopPoints));
    }

    private AgentProfile profile(boolean enabled) {
        AgentProfile profile = new AgentProfile();
        profile.setCodAgentprofile(PROFILE_ID);
        profile.setDscName("Medium Agent");
        profile.setFlgEnabled(enabled);
        profile.setJsnRecommendedfor(List.of("Agent definitions"));
        profile.setNumCpurequestmillicores(250);
        profile.setNumCpulimitmillicores(700);
        profile.setNumMemoryrequestmib(256);
        profile.setNumMemorylimitmib(768);
        profile.setDscNetworkpolicy("TOOL_GATEWAY_ONLY");
        profile.setNumMaxruntimeconcurrency(1);
        return profile;
    }

    private AlertStatus status(String value) {
        AlertStatus status = new AlertStatus();
        status.setSglStatus(value);
        return status;
    }

    private AlertVerificationStatus verificationStatus(String value) {
        AlertVerificationStatus status = new AlertVerificationStatus();
        status.setSglVerificationstatus(value);
        return status;
    }

    private AgentDefinitionStatus statusRef(String value) {
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus(value);
        return status;
    }

    private it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode generationModeRef(String value) {
        it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode mode =
                new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode();
        mode.setSglGenerationmode(value);
        return mode;
    }

    private AgentActivationType activationRef(String value) {
        AgentActivationType activationType = new AgentActivationType();
        activationType.setSglActivationtype(value);
        return activationType;
    }

    private AgentArtifactType artifactTypeRef(String value) {
        AgentArtifactType artifactType = new AgentArtifactType();
        artifactType.setSglArtifacttype(value);
        return artifactType;
    }

    private AgentArtifactSignatureStatus signatureRef(String value) {
        AgentArtifactSignatureStatus signatureStatus = new AgentArtifactSignatureStatus();
        signatureStatus.setSglSignaturestatus(value);
        return signatureStatus;
    }

    private AgentComplexity complexityRef(String value) {
        AgentComplexity complexity = new AgentComplexity();
        complexity.setSglComplexity(value);
        return complexity;
    }
}
