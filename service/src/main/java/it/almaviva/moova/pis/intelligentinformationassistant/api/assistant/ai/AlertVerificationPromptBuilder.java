package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AlertVerificationPromptBuilder {

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    TemporalConfiguration temporalConfiguration;

    @ConfigProperty(name = "iia.alert-verification.fallback-on-invalid-llm", defaultValue = "false")
    boolean fallbackOnInvalidLlm;

    public LlmRequest build(AlertVerificationPromptData alert) {
        AiConfiguration.AlertVerify alertVerifyConfiguration = aiConfiguration.alertVerify();
        String defaultTemporalZone = defaultTemporalZone();
        String model = alertVerifyConfiguration.model();
        Double temperature = alertVerifyConfiguration.temperature();
        Integer maxOutputTokens = alertVerifyConfiguration.maxOutputTokens();
        System.out.println("[IIA][ALERT_VERIFY][CONFIG] model="
                + model
                + " temperature="
                + temperature
                + " maxOutputTokens="
                + maxOutputTokens
                + " fallbackOnInvalidLlm="
                + fallbackOnInvalidLlm);
        System.out.println("[IIA][ALERT_VERIFY][TEMPORAL] temporal default zone loaded=" + defaultTemporalZone);
        System.out.println("[IIA][ALERT_VERIFY][PROMPT] includes temporal day-of-week support operators="
                + "LOCAL_DAY_OF_WEEK_IN,LOCAL_DAY_OF_WEEK_NOT_IN");
        System.out.println("[IIA][ALERT_VERIFY][PROMPT][SECTIONS] structured prompt sections built");
        return new LlmRequest(
                AiUseCase.ALERT_VERIFY,
                systemPrompt(defaultTemporalZone),
                userPrompt(alert, defaultTemporalZone),
                model,
                temperature,
                maxOutputTokens,
                alert.alertId());
    }

    private String systemPrompt(String defaultTemporalZone) {
        return String.join("\n\n",
                missionAndSafetySection(),
                mvpContractSection(defaultTemporalZone),
                mentalWorkflowSection(),
                outputJsonContractSection());
    }

    private String userPrompt(AlertVerificationPromptData alert, String defaultTemporalZone) {
        System.out.println("[IIA][ALERT_VERIFY][CATALOG] allowedFields="
                + ServiceDataCapabilityCatalog.allowedFieldCount());
        return String.join("\n\n",
                alertInputSection(alert),
                locationResolutionSection(alert),
                serviceDataCatalogSection(),
                semanticInterpretationSection(defaultTemporalZone),
                dslConstructionRulesSection(defaultTemporalZone),
                temporalRulesSection(defaultTemporalZone),
                arrayCorrelationRulesSection(),
                rejectionPolicySection(),
                requirementCoverageSection(),
                examplesSection(defaultTemporalZone),
                responseSkeletonSection());
    }

    private String missionAndSafetySection() {
        return """
                Mission and safety:
                - You are verifying an Alert for the PIS Intelligent Information Assistant.
                - An Alert is a user intent for operational monitoring.
                - Verification must not generate code.
                - Verification must not create Agent Definition.
                - Verification must not create Agent Run.
                - Verification must not create Suggestion.
                - Verification must not use Agent Profile.
                - Do not access DB, Kafka, HTTP, filesystem, external APIs or external tools.
                - Do not invent operational data.
                - Return only valid raw JSON. Do not use markdown.
                - The backend validator remains the final technical gate. Do not use confidence as a technical acceptance criterion.
                """;
    }

    private String mvpContractSection(String defaultTemporalZone) {
        return """
                MVP contract:
                - The only allowed data source is SERVICE_DATA.
                - The only allowed interpreter is EVENT_INTERPRETER.
                - The allowed triggerType is EVENT.
                - The only allowed input model is ServiceDataV2.
                - The only allowed output model is AgentOutput.CANDIDATE_SUGGESTION.
                - The only allowed evaluation mode is STATELESS_EVENT_MATCH.
                - Internal state is not supported.
                - Temporal conditions are supported only when evaluated statelessly on timestamps inside one ServiceDataV2 event.
                - Local time windows use LOCAL_TIME_BETWEEN and timezone %s unless the user supplies an explicit timezone.
                - Local day predicates use LOCAL_DAY_OF_WEEK_IN or LOCAL_DAY_OF_WEEK_NOT_IN on an allowed ServiceData timestamp.
                - Scheduled, future-existence, absence-of-events, historical observation windows and activation policies are not supported.
                - The MVP accepts any alert expressible as a stateless boolean condition over fields/operators listed in the ServiceData Capability Catalog.
                """.formatted(defaultTemporalZone);
    }

    private String mentalWorkflowSection() {
        return """
                Required reasoning workflow:
                - Extract all user constraints.
                - For each constraint, classify it as domain, source, event, location, journey, route, temporal, negation, output/action, or unsupported.
                - Map each mappable constraint to ServiceData Capability Catalog fields and allowed operators.
                - Choose the minimum condition tree that satisfies all required constraints.
                - Preserve correlation for array elements using anyElement.
                - Reject only if at least one required constraint cannot be mapped to the catalog or requires unsupported state, history, future lookup, absence of events, or external tools.
                - Return only JSON. Do not output this workflow as prose.
                """;
    }

    private String outputJsonContractSection() {
        return """
                If decision is VERIFIED:
                - technicalSpecification is mandatory and must not be empty.
                - agentBlueprintPreview is mandatory and must not be empty.
                - technicalSpecification must contain at least: schemaVersion, source, inputModel, outputModel, triggerType, evaluationMode, condition, deduplicationKeyTemplate.
                - agentBlueprintPreview must contain at least: schemaVersion, agentName, triggerType, requiredSources, evaluationMode, targetTypes, stateRequirements, output.
                - technicalSpecification.condition.type must be SERVICE_DATA_FIELD_MATCH unless using a legacy event name for backward compatibility.
                - Prefer SERVICE_DATA_FIELD_MATCH for all newly generated results.
                - Copy every extracted condition into agentBlueprintPreview.parameters.condition.
                - requiredSources must be only SERVICE_DATA.
                - interpreterType must be EVENT_INTERPRETER.
                - evaluationMode must be STATELESS_EVENT_MATCH.
                - stateRequirements.requiresState must be false.
                """;
    }

    private String alertInputSection(AlertVerificationPromptData alert) {
        return """
                Alert to verify:
                - alertId: %s
                - name: %s
                - description: %s
                - originalPrompt: %s
                """.formatted(
                nullToEmpty(alert.alertId()),
                nullToEmpty(alert.name()),
                nullToEmpty(alert.description()),
                nullToEmpty(alert.prompt()));
    }

    private String locationResolutionSection(AlertVerificationPromptData alert) {
        return alert.locationResolutionContext().compactPromptSection();
    }

    private String serviceDataCatalogSection() {
        return """
                ServiceData Capability Catalog:
                %s
                Platform catalog guidance:
                - For user-facing binario/platform/quay/banchina/marciapiede/tronco values, use only platform description fields ending in .dsc with EQUAL_PLATFORM, NOT_EQUAL_PLATFORM, IN_PLATFORMS or NOT_IN_PLATFORMS.
                - For platform description field-to-field comparisons, use PLATFORM_EQUALS_FIELD or PLATFORM_NOT_EQUALS_FIELD with "otherField".
                - Never use platform technical id fields for user-facing platform matching.
                - Do not invent fields that are absent from the ServiceData Capability Catalog.
                """.formatted(ServiceDataCapabilityCatalog.compactPromptCatalog());
    }

    private String semanticInterpretationSection(String defaultTemporalZone) {
        return """
                Semantic interpretation rules:
                - Interpret natural language semantically before choosing DSL operators.
                - platform is not a location: binario, platform, quay, banchina, marciapiede and tronco constraints are separate from stop-point location constraints.
                - "arriva a Garibaldi sul binario 1" means location Garibaldi plus arrival platform 1. The platform boundary must not become part of the location.
                - Arrival wording such as "arriva", "arrivo" or "in arrivo" selects arrival event/status and arrival platform fields.
                - Departure wording such as "parte", "partenza" or "in partenza" selects departure event/status and departure platform fields.
                - If the user specifies a platform without arrival or departure wording, do not invent one direction: construct an any condition with one arrival branch and one departure branch.
                - "non si ferma" / "passing through" is supported when mapped to passingType = TRANSIT.
                - "weekend" and "fine settimana" mean SATURDAY plus SUNDAY.
                - "non il weekend" and "escluso weekend" mean LOCAL_DAY_OF_WEEK_NOT_IN with days ["SATURDAY","SUNDAY"].
                - "feriali", "giorni feriali", "nei feriali", "durante i feriali", "dal lunedi al venerdi" and "dal lunedì al venerdì" mean LOCAL_DAY_OF_WEEK_NOT_IN with days ["SATURDAY","SUNDAY"].
                - Italian weekdays normalize as: lunedi/lunedì -> MONDAY; martedi/martedì -> TUESDAY; mercoledi/mercoledì -> WEDNESDAY; giovedi/giovedì -> THURSDAY; venerdi/venerdì -> FRIDAY; sabato -> SATURDAY; domenica -> SUNDAY.
                - "parte da <origin> nei feriali" means apply LOCAL_DAY_OF_WEEK_NOT_IN SATURDAY/SUNDAY to the departure timestamp of the origin, normally timetabledCallStart.departureTime when using stopPointsJourneyDetails[].
                - "transitera a <stop>" without a time/day predicate means use anyElement on nextTransitCalls[] with stopPoint.nameLong only. Do not add passingTime.
                - "transitera a <stop> il martedi" means use anyElement on nextTransitCalls[] with stopPoint.nameLong and passingTime LOCAL_DAY_OF_WEEK_IN TUESDAY.
                - "transitera a <stop> tra le 11:00 e le 12:00" means use passingTime LOCAL_TIME_BETWEEN because the temporal predicate refers to the transit time.
                - "parte da <origin> nei feriali e transitera a <stop>" means the weekday predicate applies to timetabledCallStart.departureTime, while the transit stop is represented by nextTransitCalls[].stopPoint.nameLong.
                - "cambia origine", "cambio origine" and "origine cambiata" mean changes CONTAINS CHANGED_ORIGIN.
                - "cambia destinazione", "cambio destinazione" and "destinazione cambiata" mean changes CONTAINS CHANGED_DESTINATION.
                - "binario di arrivo diverso da quello di partenza" means timetabledArrivalPlatform.dsc PLATFORM_NOT_EQUALS_FIELD timetabledDeparturePlatform.dsc unless the user explicitly asks for real or effective platforms.
                - "binario reale di arrivo diverso da quello reale di partenza" means actualArrivalPlatform.platform.dsc PLATFORM_NOT_EQUALS_FIELD actualDeparturePlatform.platform.dsc.
                - "cambio binario in partenza" means payload.ongroundServiceEvent.eventsType CONTAINS DEPARTURE_PLATFORM_CHANGED plus structural evidence timetabledDeparturePlatform.dsc PLATFORM_NOT_EQUALS_FIELD actualDeparturePlatform.platform.dsc inside stopPointsJourneyDetails[] anyElement.
                - "cambio binario in arrivo" means payload.ongroundServiceEvent.eventsType CONTAINS ARRIVAL_PLATFORM_CHANGED plus structural evidence timetabledArrivalPlatform.dsc PLATFORM_NOT_EQUALS_FIELD actualArrivalPlatform.platform.dsc inside stopPointsJourneyDetails[] anyElement.
                - For "cambio binario" without arrival or departure direction, use payload.ongroundServiceEvent.eventsType CONTAINS_ANY ["DEPARTURE_PLATFORM_CHANGED","ARRIVAL_PLATFORM_CHANGED"] and construct a structural any condition with one departure comparison branch and one arrival comparison branch. Do not invent one direction.
                - For "spostato dal binario X al binario Y" use payload.ongroundServiceEvent.eventsType with the matching PLATFORM_CHANGED value, previousDeparturePlatform or previousArrivalPlatform for X, and actualDeparturePlatform or actualArrivalPlatform for Y. If direction is omitted, use CONTAINS_ANY ["DEPARTURE_PLATFORM_CHANGED","ARRIVAL_PLATFORM_CHANGED"] and construct an any condition with departure and arrival structural branches.
                - For platform change and movement prompts, do not use departureStatuses[].status or arrivalStatuses[].status as the principal platform-change signal. Always prefer payload.ongroundServiceEvent.eventsType.
                - "cambia binario", "cambio binario" and "platform changed" are represented by current payload.ongroundServiceEvent.eventsType evidence and, when possible, structural timetabled != actual or previous-to-actual platform evidence.
                - "corsa cancellata", "soppressione" and "cancellazione" mean changes CONTAINS CANCELLATION, or arrival/departure cancellation status fields when those are more specific to the user wording.
                - "corsa parzialmente cancellata" means changes CONTAINS PARTIALLY_CANCELLATION.
                - Do not reject change prompts as stateful comparison when the requested change is directly represented by the ServiceData changes enum. It is a stateless predicate over the single ServiceData event.
                - "corsa sostitutiva" can be represented as isReplacementOf NOT_EMPTY when the current journey is a replacement of another journey.
                - "fermata sostitutiva in partenza" can be represented with replacement.stopPointReplacements[] and replacementType DEPARTURE or ARRIVALDEPARTURE.
                - "nei feriali" on a replacement departure means LOCAL_DAY_OF_WEEK_NOT_IN SATURDAY/SUNDAY on replacement.stopPointReplacements[].departureTime.
                - For replacement stop point replacements, use nested anyElement: outer path payload.stopPointJourney.stopPointsJourneyDetails[] and inner path replacement.stopPointReplacements[].
                - Use timezone %s unless the user explicitly supplies another valid timezone.
                """.formatted(defaultTemporalZone);
    }

    private String dslConstructionRulesSection(String defaultTemporalZone) {
        return """
                DSL construction rules:
                - Use technicalSpecification.condition.type = SERVICE_DATA_FIELD_MATCH for catalog-driven matches.
                - Conditions can contain "all" for AND, "any" for OR, anyElement for arrays, or leaf checks with field/operator/value or field/operator/values.
                - Use only fields/operators from ServiceDataCapabilityCatalog.compactPromptCatalog().
                - Operators must be allowed for that exact field or relative field.
                - Do not infer operators not listed in the catalog.
                - Do not use EXISTS, NOT_NULL or NOT_EMPTY unless the catalog allows that operator for that exact field.
                - Do not use EXISTS on timestamp fields such as passingTime, departureTime, arrivalTime, eventGenerationTime or timetabledCallStart.departureTime.
                - The existence of an array element is represented by anyElement, not by EXISTS on a timestamp field.
                - If the user only asks that a route contains/transits at a stop, use anyElement with stopPoint.nameLong; do not add a timestamp condition unless the user requests a time/day/date predicate.
                - For operator IN, always use "values": [...] and never "value".
                - For operator CONTAINS, use "value".
                - For operator CONTAINS_ANY, use "values": [...].
                - Do not generate IN with empty values.
                - Use EQUAL_PLATFORM or NOT_EQUAL_PLATFORM with a non-empty "value" for one human platform value.
                - Use IN_PLATFORMS or NOT_IN_PLATFORMS with a non-empty "values" array for multiple human platform values.
                - Use PLATFORM_EQUALS_FIELD or PLATFORM_NOT_EQUALS_FIELD with a non-empty "otherField" to compare two whitelisted platform description fields. Inside anyElement, use relative field paths for both "field" and "otherField".
                - For a current platform change event, use payload.ongroundServiceEvent.eventsType with CONTAINS DEPARTURE_PLATFORM_CHANGED, CONTAINS ARRIVAL_PLATFORM_CHANGED or CONTAINS_ANY ["DEPARTURE_PLATFORM_CHANGED","ARRIVAL_PLATFORM_CHANGED"] according to the requested direction.
                - Do not use departureStatuses[].status or arrivalStatuses[].status as the principal signal for a current platform change or movement prompt.
                - If the user says only binario/platform/quay/banchina/marciapiede/tronco plus a human value, use timetabledArrivalPlatform.dsc for arrival and timetabledDeparturePlatform.dsc for departure.
                - Use actualArrivalPlatform.platform.dsc, actualArrivalPlatform.displayPlatform.dsc, actualDeparturePlatform.platform.dsc or actualDeparturePlatform.displayPlatform.dsc only when the user explicitly asks for a real, confirmed, effective, current, monitored or updated platform, or for a platform change or movement.
                - Do not use CONTAINS for platform. Do not simulate human platform matching with CONTAINS, CONTAINS_IGNORE_CASE or CONTAINS_NORMALIZED.
                - Do not compare human platform values with platform technical id fields.
                - If only one enum value is needed, prefer EQUALS unless the catalog or semantics require IN.
                - If multiple enum values are acceptable, use IN with non-empty values.
                - For EXISTS, NOT_NULL, NOT_EMPTY no value is required when the operator is allowed by the catalog.
                - For SIZE_* operators, value must be numeric.
                - For enum checks, values must be one of the enumValues listed in the catalog.
                - Use operator LOCAL_TIME_BETWEEN with value {"start":"HH:mm:ss","end":"HH:mm:ss","timezone":"%s"} for local clock windows.
                - Use LOCAL_DAY_OF_WEEK_IN or LOCAL_DAY_OF_WEEK_NOT_IN with value {"days":["TUESDAY"],"timezone":"%s"} for local day checks.
                - Do not verify only a subset of the request. Do not silently ignore unsupported constraints.
                """.formatted(defaultTemporalZone, defaultTemporalZone);
    }

    private String temporalRulesSection(String defaultTemporalZone) {
        return """
                Temporal rules:
                - Stateless temporal predicates are allowed only on timestamp fields listed in the ServiceData Capability Catalog.
                - Supported temporal operators are LOCAL_TIME_BETWEEN, LOCAL_DAY_OF_WEEK_IN and LOCAL_DAY_OF_WEEK_NOT_IN.
                - Required LOCAL_DAY_OF_WEEK_IN JSON shape:
                  {"field":"<timestamp field>","operator":"LOCAL_DAY_OF_WEEK_IN","value":{"days":["TUESDAY"],"timezone":"%s"}}
                - Required LOCAL_DAY_OF_WEEK_NOT_IN JSON shape:
                  {"field":"<timestamp field>","operator":"LOCAL_DAY_OF_WEEK_NOT_IN","value":{"days":["SATURDAY","SUNDAY"],"timezone":"%s"}}
                - Normalize natural clock expressions: "tra le 2 e le 10" -> start 02:00:00 and end 10:00:00; "tra le 2:00 e le 10" -> start 02:00:00 and end 10:00:00; "tra le 02 e le 10:30" -> start 02:00:00 and end 10:30:00.
                - For current departure/arrival events use payload.ongroundServiceEvent.eventGenerationTime together with payload.ongroundServiceEvent.eventsType.
                - Do not use activation windows, activationPolicy, scheduler or SCHEDULED_INTERPRETER for day-of-week predicates that can be mapped to a ServiceData timestamp.
                - Reject dates relative to evaluation time such as oggi, domani or dopodomani because a persistent Alert cannot turn them into a stateless single-event predicate in this MVP.
                - Reject prediction, absence of events and historical observation windows even if a nearby stop point or clock time appears mappable.
                """.formatted(defaultTemporalZone, defaultTemporalZone);
    }

    private String arrayCorrelationRulesSection() {
        return """
                Array correlation rules:
                - If multiple constraints must match the same ServiceData array element, use anyElement with that array path and relative fields inside conditions.
                - Inside anyElement, fields are relative to the same array item.
                - For current ARRIVED, ARRIVING, DEPARTED or DEPARTING events, the user location is the monitored current stop: prefer payload.stopPointJourney.stopPoint.id or payload.ongroundServiceEvent.stopPoint.id with EQUALS, IN or NOT_IN according to the resolved locations.
                - Do not use timetabledCallStart.stopPoint.id for a current departure stop such as "parte da X".
                - Do not use timetabledCallEnd.stopPoint.id for a current arrival stop such as "arriva a X".
                - Use timetabledCallStart.stopPoint.id and timetabledCallEnd.stopPoint.id only for explicit journey origin or destination constraints such as "origine della corsa", "destinazione della corsa", "corsa con origine X" or "corsa con destinazione Y".
                - Keep platform constraints inside anyElement on payload.stopPointJourney.stopPointsJourneyDetails[]: timetabledArrivalPlatform.dsc for default arrival platform matching and timetabledDeparturePlatform.dsc for default departure platform matching.
                - Keep current platform-change event evidence at top level on payload.ongroundServiceEvent.eventsType. Keep structural platform comparisons inside anyElement on payload.stopPointJourney.stopPointsJourneyDetails[].
                - Do not use departureStatuses[].status or arrivalStatuses[].status as the principal signal for a current platform change or movement prompt.
                - A top-level current stop location and a platform constraint inside stopPointsJourneyDetails[] do not need to be inside the same anyElement.
                - For a future stop described within the received journey payload, use anyElement on nextCalls[] or nextTransitCalls[] as appropriate.
                - When correlating fields of payload.stopPointJourney.stopPointsJourneyDetails[] with child arrays, use nested anyElement.
                - Outer path must be payload.stopPointJourney.stopPointsJourneyDetails[].
                - Inner path must be relative, for example nextTransitCalls[], nextCalls[] or replacement.stopPointReplacements[].
                - Do not generate sibling anyElement nodes where one path is payload.stopPointJourney.stopPointsJourneyDetails[] and another path is payload.stopPointJourney.stopPointsJourneyDetails[].<childArray>[]; that loses same stopPointsJourneyDetails correlation.
                - anyElement on nextTransitCalls[] with stopPoint.nameLong already represents existence of a matching transit call.
                """;
    }

    private String rejectionPolicySection() {
        return """
                Cases to reject:
                - Empty or too short prompt.
                - Encyclopedic or non-operational questions.
                - Weather/meteo requests.
                - Requests requiring audio, video, device, display, broadcast, content, or unsupported sources.
                - Requests requiring internal state, scheduled/future evaluation, historical evaluation, external lookup, or absence of events.
                - Requests that require creating Agent Definition, Agent Run, Suggestion, executable code, or Agent Profile.
                - Alert or Agent activation time windows, such as "Attiva questo alert solo il weekend", because they are activation policy and not ServiceData predicates.
                - Passenger count, train color, or other required constraints absent from the catalog.
                """;
    }

    private String requirementCoverageSection() {
        return """
                Requirement coverage:
                - requirementCoverage is mandatory for every response.
                - requirements must contain every binding condition requested by the user.
                - required=true for every constraint that is part of the Alert.
                - mappable=true only when the constraint is representable with one or more catalog fields.
                - mappedBy must contain only field paths present in the ServiceData Capability Catalog.
                - For a resolved current-stop location matched through payload.stopPointJourney.stopPoint.id or payload.ongroundServiceEvent.stopPoint.id, mappedBy must contain exactly the field used by the condition.
                - For a platform requirement, mappedBy must contain the exact platform description field used by the condition, for example payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDeparturePlatform.dsc.
                - For a platform change requirement, mappedBy must include payload.ongroundServiceEvent.eventsType and the exact structural platform description fields used by the field-to-field comparison.
                - For a movement requirement such as "spostato dal binario X al binario Y", mappedBy must include payload.ongroundServiceEvent.eventsType and the exact previous and actual platform description fields used by the condition.
                - Excluded locations remain mappable=true when their resolved stopPoint ids are represented with NOT_IN.
                - reason must explain why a required constraint is not mappable.
                - allRequiredRequirementsMapped=false when at least one required requirement has mappable=false.
                - If allRequiredRequirementsMapped=false, decision must be REJECTED.
                - If decision=VERIFIED, allRequiredRequirementsMapped must be true.
                """;
    }

    private String examplesSection(String defaultTemporalZone) {
        return """
                Few-shot examples:

                Positive example - weekend on origin departure:
                Prompt: "Avvertimi quando una corsa che parte da Genova P.P il weekend"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":{"all":[
                  {"field":"timetabledCallStart.stopPoint.nameLong","operator":"EQUALS_NORMALIZED","value":"Genova P.P"},
                  {"field":"timetabledCallStart.departureTime","operator":"LOCAL_DAY_OF_WEEK_IN","value":{"days":["SATURDAY","SUNDAY"],"timezone":"%s"}}
                ]}}}
                Decision: VERIFIED

                Positive example - not weekend plus local time range on origin departure:
                Prompt: "Avvertimi quando una corsa parte da Genova P.P tra le 11:20 e le 11:25 non il weekend"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":{"all":[
                  {"field":"timetabledCallStart.stopPoint.nameLong","operator":"EQUALS_NORMALIZED","value":"Genova P.P"},
                  {"field":"timetabledCallStart.departureTime","operator":"LOCAL_TIME_BETWEEN","value":{"start":"11:20:00","end":"11:25:00","timezone":"%s"}},
                  {"field":"timetabledCallStart.departureTime","operator":"LOCAL_DAY_OF_WEEK_NOT_IN","value":{"days":["SATURDAY","SUNDAY"],"timezone":"%s"}}
                ]}}}
                Decision: VERIFIED

                Positive example - transit stop with weekday on passingTime:
                Prompt: "Avvertimi quando una corsa che parte da Genova P.P e transitera a Genova Nervi il martedi"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":{"all":[
                  {"field":"timetabledCallStart.stopPoint.nameLong","operator":"EQUALS_NORMALIZED","value":"Genova P.P"},
                  {"anyElement":{"path":"nextTransitCalls[]","conditions":{"all":[
                    {"field":"stopPoint.nameLong","operator":"CONTAINS_NORMALIZED","value":"Genova Nervi"},
                    {"field":"passingTime","operator":"LOCAL_DAY_OF_WEEK_IN","value":{"days":["TUESDAY"],"timezone":"%s"}}
                  ]}}}
                ]}}}
                Decision: VERIFIED

                Positive example - weekday on origin departure plus transit stop without passingTime:
                Prompt: "Avvertimi quando una corsa che parte da Genova P.P nei feriali e transitera a Genova Nervi"
                Expected condition:
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "anyElement": {
                    "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                    "conditions": {
                      "all": [
                        {"field":"timetabledCallStart.stopPoint.nameLong","operator":"EQUALS_NORMALIZED","value":"Genova P.P"},
                        {"field":"timetabledCallStart.departureTime","operator":"LOCAL_DAY_OF_WEEK_NOT_IN","value":{"days":["SATURDAY","SUNDAY"],"timezone":"%s"}},
                        {"anyElement":{"path":"nextTransitCalls[]","conditions":{"all":[
                          {"field":"stopPoint.nameLong","operator":"CONTAINS_NORMALIZED","value":"Genova Nervi"}
                        ]}}}
                      ]
                    }
                  }
                }
                Decision: VERIFIED

                Positive example - change flag represented by ServiceData changes enum:
                Prompt: "Avvertimi quando una corsa cambia origine"
                Expected condition:
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "anyElement": {
                    "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                    "conditions": {
                      "all": [
                        {"field":"changes","operator":"CONTAINS","value":"CHANGED_ORIGIN"}
                      ]
                    }
                  }
                }
                Decision: VERIFIED

                Positive example - replacement departure stop point on weekdays:
                Prompt: "Avvertimi quando una corsa sostitutiva ha una fermata sostitutiva in partenza nei feriali"
                Expected condition:
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "anyElement": {
                    "path": "payload.stopPointJourney.stopPointsJourneyDetails[]",
                    "conditions": {
                      "all": [
                        {"field":"isReplacementOf","operator":"NOT_EMPTY"},
                        {"anyElement":{"path":"replacement.stopPointReplacements[]","conditions":{"all":[
                          {"field":"replacementType","operator":"IN","values":["DEPARTURE","ARRIVALDEPARTURE"]},
                          {"field":"departureTime","operator":"LOCAL_DAY_OF_WEEK_NOT_IN","value":{"days":["SATURDAY","SUNDAY"],"timezone":"%s"}}
                        ]}}}
                      ]
                    }
                  }
                }
                Decision: VERIFIED

                Positive example - arrival platform correlated with resolved location:
                Prompt: "Avvertimi quando un treno arriva a Garibaldi sul binario 1"
                Meaning: location Garibaldi plus arrival platform 1.
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.stopPointJourney.stopPoint.id","operator":"IN","values":["<resolvedGaribaldiStopPointIds>"]},
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"ARRIVED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"timetabledArrivalPlatform.dsc","operator":"EQUAL_PLATFORM","value":"1"}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - departure platforms correlated with resolved locations:
                Prompt: "Avvertimi quando una corsa a Buonarroti o Malpensa si verifica in partenza sul binario 1 o sul binario 4"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.stopPointJourney.stopPoint.id","operator":"IN","values":["<resolvedBuonarrotiStopPointId>","<resolvedMalpensaT1StopPointId>","<resolvedMalpensaT2StopPointId>"]},
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"timetabledDeparturePlatform.dsc","operator":"IN_PLATFORMS","values":["1","4"]}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - excluded departure locations and platforms:
                Prompt: "Avvertimi quando un treno e in partenza da una localita diversa da Cairoli, Cascina, San Donato e che non sia in partenza ne dal binario 1 ne dal binario 12"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.stopPointJourney.stopPoint.id","operator":"NOT_IN","values":["<resolvedCairoliStopPointId>","<resolvedCascinaStopPointId>","<resolvedSanDonatoStopPointId>"]},
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"timetabledDeparturePlatform.dsc","operator":"NOT_IN_PLATFORMS","values":["1","12"]}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - departure platform change at a resolved current stop:
                Prompt: "Avvertimi quando una corsa a Lampugnano subisce un cambio di binario in partenza"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.stopPointJourney.stopPoint.id","operator":"EQUALS","value":"<resolvedLampugnanoStopPointId>"},
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTURE_PLATFORM_CHANGED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"timetabledDeparturePlatform.dsc","operator":"PLATFORM_NOT_EQUALS_FIELD","otherField":"actualDeparturePlatform.platform.dsc"}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - moved platform with unspecified direction:
                Prompt: "Avvertimi quando un treno viene spostato dal binario 5 al binario 7 o 8 a Genova P.P."
                Meaning: keep the resolved Genova P.P. current stop at top level, use current eventsType CONTAINS_ANY for the two PLATFORM_CHANGED values, and use an any condition with a departure branch and an arrival branch for previous-to-actual platform evidence.
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.stopPointJourney.stopPoint.id","operator":"EQUALS","value":"<resolvedGenovaPpStopPointId>"},
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS_ANY","values":["DEPARTURE_PLATFORM_CHANGED","ARRIVAL_PLATFORM_CHANGED"]},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":{"any":[
                    {"all":[
                      {"field":"previousDeparturePlatform.platform.dsc","operator":"EQUAL_PLATFORM","value":"5"},
                      {"field":"actualDeparturePlatform.platform.dsc","operator":"IN_PLATFORMS","values":["7","8"]}
                    ]},
                    {"all":[
                      {"field":"previousArrivalPlatform.platform.dsc","operator":"EQUAL_PLATFORM","value":"5"},
                      {"field":"actualArrivalPlatform.platform.dsc","operator":"IN_PLATFORMS","values":["7","8"]}
                    ]}
                  ]}}}
                ]}
                Decision: VERIFIED

                Negative example - do not add timestamp existence for transit stop:
                Do not generate:
                {"field":"passingTime","operator":"EXISTS"}
                Explanation: EXISTS on passingTime is not needed and not allowed. anyElement on nextTransitCalls[] with the requested stopPoint already represents existence of a matching transit call.

                Negative example - invalid IN shape:
                Do not generate IN without values:
                {"field":"replacementType","operator":"IN"}
                Do not generate IN with empty values:
                {"field":"replacementType","operator":"IN","values":[]}
                Do not generate IN with value instead of values:
                {"field":"replacementType","operator":"IN","value":"DEPARTURE"}

                Negative example - do not reject catalog-backed changes:
                Prompt: "Avvertimi quando una corsa cambia origine"
                Do not reject as stateful comparison when changes CHANGED_ORIGIN is available in the catalog.

                Negative example - activation policy:
                Prompt: "Attiva questo alert solo il weekend"
                Decision: REJECTED
                rejectedReason: "Activation time windows are not supported in the current Alert Verify MVP. Only stateless temporal predicates evaluated on ServiceData event timestamps are supported."

                Negative example - absence of events:
                Prompt: "Avvisami se nel weekend non passano corse a Genova Nervi"
                Decision: REJECTED
                rejectedReason: "The request requires absence-of-events evaluation, state or an observation window, which is not evaluable on a single ServiceData event."

                Negative example - unsupported constraint:
                Prompt: "Avvisami quando il treno 1253 parte da Genova e ha almeno 10 passeggeri"
                Decision: REJECTED
                rejectedReason: "Passenger count is not available in the ServiceData Capability Catalog."

                Negative example - unsupported attribute:
                Prompt: "Avvisami quando il treno 1253 parte da Genova ed e rosso"
                Decision: REJECTED
                rejectedReason: "Train color is not available in the ServiceData Capability Catalog."
                """.formatted(
                defaultTemporalZone,
                defaultTemporalZone,
                defaultTemporalZone,
                defaultTemporalZone,
                defaultTemporalZone,
                defaultTemporalZone);
    }

    private String responseSkeletonSection() {
        return """
                Response JSON contract:
                {
                  "decision": "VERIFIED",
                  "summary": "The alert can be evaluated on realtime ServiceData events.",
                  "rejectedReason": null,
                  "confidence": 0.86,
                  "requiredSources": ["SERVICE_DATA"],
                  "interpreterType": "EVENT_INTERPRETER",
                  "triggerType": "EVENT",
                  "evaluationMode": "STATELESS_EVENT_MATCH",
                  "inputModel": "ServiceDataV2",
                  "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                  "targetTypes": ["SERVICE_DATA_JOURNEY"],
                  "interpretedEventNames": ["SERVICE_DATA_FIELD_MATCH"],
                  "requirementCoverage": {
                    "requirements": [
                      {
                        "text": "required user constraint",
                        "required": true,
                        "mappable": true,
                        "mappedBy": ["payload.some.allowed.field"],
                        "reason": null
                      }
                    ],
                    "allRequiredRequirementsMapped": true
                  },
                  "technicalSpecification": {
                    "schemaVersion": "iia.alert.technical-specification/v2",
                    "source": "SERVICE_DATA",
                    "inputModel": "ServiceDataV2",
                    "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                    "triggerType": "EVENT",
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "condition": {"type": "SERVICE_DATA_FIELD_MATCH"},
                    "deduplicationKeyTemplate": "SERVICE_DATA:${journeyId}:${stopPointId}:${conditionHash}"
                  },
                  "agentBlueprintPreview": {
                    "schemaVersion": "iia.agent.blueprint/v1",
                    "agentName": "ServiceDataFieldMatchAlertAgent",
                    "description": "Detects matching ServiceData events using the verified stateless condition.",
                    "triggerType": "EVENT",
                    "requiredSources": ["SERVICE_DATA"],
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "targetTypes": ["SERVICE_DATA_JOURNEY"],
                    "parameters": {
                      "conditionType": "SERVICE_DATA_FIELD_MATCH",
                      "condition": {"type": "SERVICE_DATA_FIELD_MATCH"}
                    },
                    "stateRequirements": {"requiresState": false},
                    "output": {"type": "CANDIDATE_SUGGESTION"}
                  },
                  "warnings": [],
                  "safetyChecks": [
                    "No executable code generated.",
                    "No Agent Definition created.",
                    "No Agent Run created.",
                    "No Suggestion created."
                  ]
                }
                """;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String defaultTemporalZone() {
        if (temporalConfiguration == null
                || temporalConfiguration.defaultZone() == null
                || temporalConfiguration.defaultZone().isBlank()) {
            return "Europe/Rome";
        }
        return temporalConfiguration.defaultZone();
    }
}
