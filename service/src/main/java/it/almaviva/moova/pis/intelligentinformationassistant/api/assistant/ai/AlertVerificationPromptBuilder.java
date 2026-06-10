package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
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
                nonLocationConstraintsSection(alert),
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
                - originalPrompt: %s

                Metadata, not additional constraints:
                - name: %s
                - description: %s

                Use originalPrompt and backend-derived context as the authoritative sources of user intent.
                Metadata can help identify the alert but must not introduce extra technical requirements.
                Do not reject because metadata mentions technical expectations or because metadata names ServiceData fields.
                """.formatted(
                nullToEmpty(alert.alertId()),
                nullToEmpty(alert.prompt()),
                nullToEmpty(alert.name()),
                nullToEmpty(alert.description()));
    }

    private String locationResolutionSection(AlertVerificationPromptData alert) {
        return alert.locationResolutionContext().compactPromptSection();
    }

    private String nonLocationConstraintsSection(AlertVerificationPromptData alert) {
        StringBuilder section = new StringBuilder("Backend-derived non-location constraints:\n");
        if (alert.locationResolutionContext().nonLocationConstraints().isEmpty()) {
            section.append("- none\n");
        } else {
            for (AlertVerificationLocationContext.NonLocationConstraint constraint
                    : alert.locationResolutionContext().nonLocationConstraints()) {
                String displayRawText = nullToEmpty(constraint.rawText());
                if ("DELAY_EVENT_TYPE".equalsIgnoreCase(constraint.type())) {
                    String normalizedDelayEventType = AlertDelayEventTypeNormalizer.normalize(displayRawText);
                    if (normalizedDelayEventType == null) {
                        continue;
                    }
                    displayRawText = normalizedDelayEventType;
                }
                section.append("- ").append(nullToEmpty(constraint.type()))
                        .append("=").append(displayRawText).append("\n");
                if ("DELAY_THRESHOLD".equalsIgnoreCase(constraint.type())) {
                    appendDelayThresholdBreakdown(section, constraint.rawText());
                }
            }
        }
        section.append("""

                Rules:
                - These backend-derived constraints are authoritative.
                - They are already extracted from originalPrompt.
                - Do not reject because originalPrompt does not mention ServiceData field names.
                - The user is not expected to write payload.ongroundServiceEvent.eventsType, arrivalDelay.delay or departureDelay.delay.
                - For realtime ServiceData alerts, first identify the monitored stop/location, then the main realtime event, then journey state filters, then any additional filters such as delay/platform/origin/destination.
                - Journey state filters are evaluated inside payload.stopPointJourney.stopPointsJourneyDetails[] with anyElement and must not replace the main realtime event when a separate main event is present.
                - Natural language such as "ha piu di 15 minuti di ritardo" is enough to derive both event type and delay predicates when DELAY_EVENT_TYPE and DELAY_THRESHOLD are provided.
                - Map these constraints to the ServiceData Capability Catalog.
                - DELAY_EVENT_TYPE=BOTH plus DELAY_THRESHOLD means eventsType CONTAINS_ANY ["ARRIVAL_DELAY","DEPARTURE_DELAY"] and an OR over arrivalDelay.delay and departureDelay.delay with the threshold.
                - DELAY_EVENT_TYPE=DEPARTURE_DELAY plus DELAY_THRESHOLD means eventsType CONTAINS DEPARTURE_DELAY and departureDelay.delay with the threshold.
                - DELAY_EVENT_TYPE=ARRIVAL_DELAY plus DELAY_THRESHOLD means eventsType CONTAINS ARRIVAL_DELAY and arrivalDelay.delay with the threshold.
                - EXPECTED_MAIN_EVENT_TYPE, when present, is authoritative for the current event: use payload.ongroundServiceEvent.eventsType CONTAINS <EXPECTED_MAIN_EVENT_TYPE>.
                - DELAY_ROLE=ACCESSORY_DELAY_PREDICATE means the delay is only an extra predicate; use EXPECTED_MAIN_EVENT_TYPE for payload.ongroundServiceEvent.eventsType and add the coherent delay predicate.
                - If DELAY_ROLE=ACCESSORY_DELAY_PREDICATE, do not use ARRIVAL_DELAY or DEPARTURE_DELAY as the current event. Use EXPECTED_MAIN_EVENT_TYPE as the current event and use DELAY_EVENT_TYPE only to choose the coherent delay field.
                - For accessory delay predicates, DELAY_EVENT_TYPE=ARRIVAL_DELAY means arrivalDelay.delay; DELAY_EVENT_TYPE=DEPARTURE_DELAY means departureDelay.delay; DELAY_EVENT_TYPE=BOTH or GENERIC_DELAY means an OR over arrivalDelay.delay and departureDelay.delay.
                - DELAY_ROLE=PRIMARY_DELAY_EVENT means DELAY_EVENT_TYPE governs payload.ongroundServiceEvent.eventsType.
                - If DELAY_ROLE=PRIMARY_DELAY_EVENT, DELAY_EVENT_TYPE governs payload.ongroundServiceEvent.eventsType: ARRIVAL_DELAY -> eventsType CONTAINS ARRIVAL_DELAY; DEPARTURE_DELAY -> eventsType CONTAINS DEPARTURE_DELAY; BOTH/GENERIC_DELAY -> eventsType CONTAINS_ANY ["ARRIVAL_DELAY","DEPARTURE_DELAY"].
                - Examples: "service is arriving at X with more than N minutes delay" -> eventsType CONTAINS ARRIVING, current stop X, arrivalDelay.delay threshold.
                - Examples: "service arrived at X with more than N minutes delay" -> eventsType CONTAINS ARRIVED, current stop X, arrivalDelay.delay threshold.
                - Examples: "service is departing from X with more than N minutes delay" -> eventsType CONTAINS DEPARTING, current stop X, departureDelay.delay threshold.
                - Examples: "service departed from X with more than N minutes delay" -> eventsType CONTAINS DEPARTED, current stop X, departureDelay.delay threshold.
                - Examples: "service has more than N minutes arrival delay" -> eventsType CONTAINS ARRIVAL_DELAY and arrivalDelay.delay threshold.
                """);
        return section.toString();
    }

    private void appendDelayThresholdBreakdown(StringBuilder section, String rawText) {
        String operator = valueAfterKey(rawText, "operator");
        String value = valueAfterKey(rawText, "value");
        String unit = valueAfterKey(rawText, "unit");
        if (operator != null) {
            section.append("  operator: ").append(operator).append("\n");
        }
        if (value != null) {
            section.append("  value: ").append(value).append("\n");
        }
        if (unit != null) {
            section.append("  unit: ").append(unit).append("\n");
        }
    }

    private String valueAfterKey(String rawText, String key) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        String prefix = key + "=";
        for (String part : rawText.split(";")) {
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return trimmed.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    private String serviceDataCatalogSection() {
        return """
                ServiceData Capability Catalog:
                %s
                Platform catalog guidance:
                - For user-facing binario/platform/quay/banchina/marciapiede values, use only platform description fields ending in .dsc with the allowed PLATFORM operators.
                - For platform description field-to-field comparisons, use PLATFORM_EQUALS_FIELD or PLATFORM_NOT_EQUALS_FIELD with "otherField".
                - Never use platform technical id fields for user-facing platform matching.
                - Do not invent fields that are absent from the ServiceData Capability Catalog.
                """.formatted(ServiceDataCapabilityCatalog.compactPromptCatalog());
    }

    private String semanticInterpretationSection(String defaultTemporalZone) {
        return """
                Semantic interpretation rules:
                - Interpret natural language semantically before choosing DSL operators.
                - platform is not a location: binario, platform, quay, banchina and marciapiede constraints are separate from stop-point location constraints.
                - "arriva a Garibaldi sul binario 1" means location Garibaldi plus arrival platform 1. The platform boundary must not become part of the location.
                - Arrival wording such as "arriva", "arrivo" or "in arrivo" selects arrival event/status and arrival platform fields.
                - Departure wording such as "parte", "partenza" or "in partenza" selects departure event/status and departure platform fields.
                - Distinguish primary delay events from movement events with accessory delay predicates.
                - Primary departure delay semantics: "has a departure delay", "departure delay", "rounded departure delay", "ritardo di partenza", "ritardo in partenza" and "ritardo arrotondato in partenza" map payload.ongroundServiceEvent.eventsType to DEPARTURE_DELAY.
                - Primary arrival delay semantics: "has an arrival delay", "arrival delay", "rounded arrival delay", "ritardo di arrivo", "ritardo in arrivo" and "ritardo arrotondato in arrivo" map payload.ongroundServiceEvent.eventsType to ARRIVAL_DELAY.
                - Movement with accessory delay semantics: "is departing with delay", "parte da X con ritardo", "e in partenza con ritardo" keep DEPARTING/DEPARTED according to event phase and add departureDelay.* as an additional predicate.
                - Movement with accessory delay semantics: "is arriving with delay", "arriva a X con ritardo", "e in arrivo con ritardo" keep ARRIVING/ARRIVED according to event phase and add arrivalDelay.* as an additional predicate.
                - Rounded delay selects roundedDelay instead of delay: rounded departure delay -> departureDelay.roundedDelay; rounded arrival delay -> arrivalDelay.roundedDelay; generic rounded delay -> OR over arrivalDelay.roundedDelay and departureDelay.roundedDelay.
                - Normal delay selects delay instead of roundedDelay: departure delay -> departureDelay.delay; arrival delay -> arrivalDelay.delay; generic delay -> OR over arrivalDelay.delay and departureDelay.delay.
                - Do not include arrival delay fields or ARRIVAL_DELAY when the user explicitly asks for departure delay.
                - Do not include departure delay fields or DEPARTURE_DELAY when the user explicitly asks for arrival delay.
                - Delay direction has priority over event phase: "ritardo in arrivo", "ritardo di arrivo", "arrival delay" and "delay on arrival" map to arrivalDelay.delay or arrivalDelay.roundedDelay and optional eventsType CONTAINS ARRIVAL_DELAY. They do not create a location named "in arrivo" and do not by themselves require ARRIVING.
                - Delay direction has priority over event phase: "ritardo in partenza", "ritardo di partenza" and "departure delay" map to departureDelay.delay or departureDelay.roundedDelay and optional eventsType CONTAINS DEPARTURE_DELAY. They do not create a location named "in partenza".
                - DELAY_EVENT_TYPE is authoritative only for delay-primary alerts. If backend-derived constraints include DELAY_ROLE=ACCESSORY_DELAY_PREDICATE, use EXPECTED_MAIN_EVENT_TYPE for payload.ongroundServiceEvent.eventsType and treat delay as an additional predicate.
                - If Location Understanding provides DELAY_EVENT_TYPE=DEPARTURE_DELAY or DELAY_EVENT_TYPE=ARRIVAL_DELAY for a delay-primary alert, that value is authoritative for payload.ongroundServiceEvent.eventsType.
                - If Location Understanding provides DELAY_EVENT_TYPE=BOTH or GENERIC_DELAY for a delay-primary alert, bind payload.ongroundServiceEvent.eventsType with operator CONTAINS_ANY and values ["ARRIVAL_DELAY","DEPARTURE_DELAY"].
                - For generic delay with no arrival/departure direction, pair the generic delay event binding with an OR over arrivalDelay.delay and departureDelay.delay inside stopPointsJourneyDetails[].
                - If the user specifies a numeric delay threshold, VERIFIED output must include the numeric delay predicate. The eventsType binding alone is never sufficient for threshold-based delay alerts.
                - This is a mapping obligation for the verifier, not something the user must explicitly write as technical fields. Do not reject solely because the user did not specify arrivalDelay.delay/departureDelay.delay field names.
                - Do not replace DEPARTING/ARRIVING/DEPARTED/ARRIVED with DEPARTURE_DELAY/ARRIVAL_DELAY when the user says "is departing from X with delay", "is arriving at X with delay", "e in partenza da X con ritardo" or "e in arrivo a X con ritardo".
                - Use DEPARTURE_DELAY/ARRIVAL_DELAY only when the grammatical focus is the delay on departure/arrival. For "service in departure/arrival with delay", keep DEPARTING/ARRIVING and add the coherent delay predicate.
                - Never create stopPoint.nameLong/nameShort textual fallback values from functional words alone: "in arrivo", "in partenza", "arrivo", "partenza", "destinazione", "destino", "origine", "transito", "arrival", "departure", "destination", "origin", "transit".
                - "arriva a destinazione", "arriva a destino", "at destination" and "at final destination" without a proper stop name mean passingType EQUALS DESTINATION inside stopPointsJourneyDetails[], plus coherent arrival event if requested.
                - "parte dall'origine" without a proper stop name means passingType EQUALS ORIGIN inside stopPointsJourneyDetails[], plus coherent departure event if requested.
                - "passa in transito" without a proper stop name means passingType EQUALS TRANSIT, not a textual location named "transito".
                - If Location Understanding provides nonLocationConstraints MAIN_EVENT_INTENT=ARRIVAL, use arrival semantics: ARRIVED/ARRIVING events, arrival platform fields and arrivalDelay fields. Do not generate DEPARTING/DEPARTED or departure platform fields.
                - If Location Understanding provides nonLocationConstraints MAIN_EVENT_INTENT=DEPARTURE, use departure semantics: DEPARTED/DEPARTING events, departure platform fields and departureDelay fields. Do not generate ARRIVING/ARRIVED or arrival platform fields.
                - Authoritative main event constraints:
                  When the backend provides MAIN_EVENT_INTENT, MAIN_EVENT_PHASE and EXPECTED_MAIN_EVENT_TYPE, you must bind the current ServiceData event to EXPECTED_MAIN_EVENT_TYPE on field payload.ongroundServiceEvent.eventsType. This binding is mandatory for VERIFIED responses.
                - If Location Understanding provides nonLocationConstraints MAIN_EVENT_PHASE=PROGRESSIVE, use the progressive current event value: DEPARTURE -> DEPARTING, ARRIVAL -> ARRIVING.
                - If Location Understanding provides nonLocationConstraints MAIN_EVENT_PHASE=COMPLETED, use the completed current event value: DEPARTURE -> DEPARTED, ARRIVAL -> ARRIVED.
                - EXPECTED_MAIN_EVENT_TYPE is authoritative. If Location Understanding provides EXPECTED_MAIN_EVENT_TYPE, the technicalSpecification.condition must bind payload.ongroundServiceEvent.eventsType to that exact event type.
                - For a single EXPECTED_MAIN_EVENT_TYPE use {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"<EXPECTED_MAIN_EVENT_TYPE>"}.
                - Do not reinterpret a PROGRESSIVE phase as completed: PROGRESSIVE DEPARTURE -> DEPARTING and PROGRESSIVE ARRIVAL -> ARRIVING.
                - Do not reinterpret a COMPLETED phase as progressive: COMPLETED DEPARTURE -> DEPARTED and COMPLETED ARRIVAL -> ARRIVED.
                - Few-shot examples are illustrative and must not override EXPECTED_MAIN_EVENT_TYPE.
                - EXPECTED_MAIN_EVENT_TYPE=DEPARTING -> use DEPARTING, never DEPARTED.
                - EXPECTED_MAIN_EVENT_TYPE=DEPARTED -> use DEPARTED, never DEPARTING.
                - EXPECTED_MAIN_EVENT_TYPE=ARRIVING -> use ARRIVING, never ARRIVED.
                - EXPECTED_MAIN_EVENT_TYPE=ARRIVED -> use ARRIVED, never ARRIVING.
                - For precise completed arrival wording such as "arriva", prefer payload.ongroundServiceEvent.eventsType CONTAINS ARRIVED. For "in arrivo" or progressive arrival wording, prefer ARRIVING. If truly ambiguous between progress and completion, use ARRIVING/ARRIVED only, never departure events.
                - For precise completed departure wording such as "parte", prefer payload.ongroundServiceEvent.eventsType CONTAINS DEPARTED. For "in partenza" or progressive departure wording, prefer DEPARTING. If truly ambiguous between progress and completion, use DEPARTING/DEPARTED only, never arrival events.
                - If the user specifies a platform without arrival or departure wording, do not invent one direction: construct an any condition with one arrival branch and one departure branch.
                - Platform numeric/property predicates always require a top-level payload.ongroundServiceEvent.eventsType binding: "in partenza", "si verifica in partenza" and "sta partendo" -> CONTAINS DEPARTING; "parte", "e partita" and "partita" -> CONTAINS DEPARTED; "in arrivo" and "sta arrivando" -> CONTAINS ARRIVING; "arriva", "e arrivata" and "arrivata" -> CONTAINS ARRIVED.
                - English current-event mapping for platform numeric/property predicates is precise: "departs", "has departed" and "departed" -> CONTAINS DEPARTED; "is departing", "departing" and "about to depart" -> CONTAINS DEPARTING; "arrives", "has arrived" and "arrived" -> CONTAINS ARRIVED; "is arriving", "arriving" and "about to arrive" -> CONTAINS ARRIVING.
                - Use CONTAINS_ANY for numeric/property platform predicates only when the prompt is truly ambiguous between progress and completion, uses generic wording such as "departure event" or "arrival event", or omits arrival/departure direction. Do not widen precise verbs such as "departs" or "arrives".
                - If a platform numeric/property prompt omits arrival/departure direction, use payload.ongroundServiceEvent.eventsType CONTAINS_ANY ["DEPARTING","DEPARTED","ARRIVING","ARRIVED"] and construct an any condition with actualDeparturePlatform.platform.dsc and actualArrivalPlatform.platform.dsc branches.
                - Platform numeric mappings: "maggiore di N" and "superiore a N" -> PLATFORM_NUMBER_GREATER_THAN; "almeno N", "maggiore o uguale a N" and "da N in su" -> PLATFORM_NUMBER_GREATER_OR_EQUAL.
                - Platform numeric mappings: "minore di N" and "inferiore a N" -> PLATFORM_NUMBER_LESS_THAN; "al massimo N", "minore o uguale a N" and "fino a N" -> PLATFORM_NUMBER_LESS_OR_EQUAL.
                - Platform numeric mappings: "tra X e Y" and "compreso tra X e Y" -> PLATFORM_NUMBER_BETWEEN with value {"min":X,"max":Y}; "multiplo di N" -> PLATFORM_NUMBER_MULTIPLE_OF.
                - Platform property mappings: "pari" -> PLATFORM_NUMBER_EVEN; "dispari" -> PLATFORM_NUMBER_ODD; "a doppia cifra" -> PLATFORM_NUMBER_DOUBLE_DIGIT; "con una lettera", "con suffisso lettera" and "tipo 3A" -> PLATFORM_HAS_LETTER_SUFFIX.
                - For numeric platform operators, a description such as "3A" uses main number 3. The suffix remains discriminating for EQUAL_PLATFORM and is detected by PLATFORM_HAS_LETTER_SUFFIX.
                - For platform numeric/property predicates, use actualDeparturePlatform.platform.dsc for departure and actualArrivalPlatform.platform.dsc for arrival by default because the predicate describes the effective realtime platform of the current event.
                - For platform numeric/property predicates, use timetabledDeparturePlatform.dsc or timetabledArrivalPlatform.dsc only when the user explicitly says previsto, programmato, da orario, pianificato, timetabled or scheduled.
                - If the user explicitly asks for a real, confirmed, effective or monitored platform, use the coherent actual arrival/departure platform description field.
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
                - Realtime cancellation workflow is language-independent: identify monitored stop/location; identify the main realtime event; identify requested journey state filters; compose top-level event/location predicates with correlated stopPointsJourneyDetails[] predicates.
                - Generic cancellation / suppressed journey / cancelled journey at the monitored stop means payload.ongroundServiceEvent.eventsType CONTAINS_ANY ["CANCELLATION","ARRIVAL_CANCELLATION","DEPARTURE_CANCELLATION"] plus anyElement on payload.stopPointJourney.stopPointsJourneyDetails[] with an OR of: arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION AND departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION; OR arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION AND passingType EQUALS DESTINATION; OR departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION AND passingType EQUALS ORIGIN.
                - Arrival cancellation / suppressed on arrival is a journey state filter, not an ARRIVING/ARRIVED event: use eventsType CONTAINS_ANY ["ARRIVAL_CANCELLATION","CANCELLATION"] when cancellation is the main event, and anyElement stopPointsJourneyDetails[] with arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION. Do not add departureStatuses[].status NOT_CONTAINS DEPARTURE_CANCELLATION for non-exclusive arrival cancellation.
                - Exclusive arrival cancellation / only suppressed on arrival means anyElement stopPointsJourneyDetails[] with arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION AND departureStatuses[].status NOT_CONTAINS DEPARTURE_CANCELLATION. Use eventsType CONTAINS ARRIVAL_CANCELLATION, or CONTAINS_ANY with only compatible arrival-cancellation event values when the catalog shape requires an array.
                - Departure cancellation / suppressed on departure is a journey state filter, not a DEPARTING/DEPARTED event: use eventsType CONTAINS_ANY ["DEPARTURE_CANCELLATION","CANCELLATION"] when cancellation is the main event, and anyElement stopPointsJourneyDetails[] with departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION. Do not add arrivalStatuses[].status NOT_CONTAINS ARRIVAL_CANCELLATION for non-exclusive departure cancellation.
                - Exclusive departure cancellation / only suppressed on departure means anyElement stopPointsJourneyDetails[] with departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION AND arrivalStatuses[].status NOT_CONTAINS ARRIVAL_CANCELLATION. Use eventsType CONTAINS DEPARTURE_CANCELLATION, or CONTAINS_ANY with only compatible departure-cancellation event values when the catalog shape requires an array.
                - If the main realtime event is different from cancellation, keep payload.ongroundServiceEvent.eventsType bound to that main event and represent cancellation only as a journey state filter inside stopPointsJourneyDetails[]. Example semantics: current departure event plus suppressed on arrival plus delay means eventsType CONTAINS DEPARTING, arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION and the coherent delay threshold; do not replace DEPARTING with ARRIVAL_CANCELLATION.
                - "corsa cancellata", "soppressione" and "cancellazione" may also map to changes CONTAINS CANCELLATION only when the prompt is about ServiceData journey changes rather than current stop cancellation state.
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
                - The "type" property belongs only on the root technicalSpecification.condition. Never put "type" inside condition.all[], condition.any[] or anyElement.conditions.
                - Use only fields/operators from ServiceDataCapabilityCatalog.compactPromptCatalog().
                - Operators must be allowed for that exact field or relative field.
                - Do not infer operators not listed in the catalog.
                - Do not use NOT_EQUAL or NOT_EQUALS on stopPoint.nameLong/nameShort unless that exact operator is listed in the catalog for that exact field.
                - For a RESOLVED location with exactly one selected stopPoint id, use EQUALS with value. Use IN only when there are multiple selected ids.
                - For polarity=EXCLUDE and UNRESOLVED locations, use NOT_CONTAINS_NORMALIZED on the correct nameLong/nameShort field when the catalog supports it. Lower confidence and add a warning.
                - For DESTINATION_LOCATION with polarity=EXCLUDE and status=UNRESOLVED, prefer the single canonical fallback field timetabledCallEnd.stopPoint.nameLong with NOT_CONTAINS_NORMALIZED. Use callEnd.stopPoint.nameLong only when the user explicitly asks for actual/effective/real destination.
                - Do not create any/OR branches across multiple negative textual fallback fields such as timetabledCallEnd/callEnd/nameLong/nameShort. A negative fallback must be one canonical field for this MVP.
                - Use NOT_EQUALS_NORMALIZED only when the user wording requires exact normalized inequality and the catalog supports it.
                - If the catalog does not support any negative normalized textual operator for the required field, return REJECTED. Do not silently ignore the excluded location.
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
                - PLATFORM_NUMBER_GREATER_THAN, PLATFORM_NUMBER_GREATER_OR_EQUAL, PLATFORM_NUMBER_LESS_THAN, PLATFORM_NUMBER_LESS_OR_EQUAL and PLATFORM_NUMBER_MULTIPLE_OF require numeric "value". PLATFORM_NUMBER_MULTIPLE_OF requires value greater than 0.
                - PLATFORM_NUMBER_BETWEEN requires "value":{"min":N,"max":N} with numeric min less than or equal to max.
                - PLATFORM_NUMBER_EVEN, PLATFORM_NUMBER_ODD, PLATFORM_NUMBER_DOUBLE_DIGIT and PLATFORM_HAS_LETTER_SUFFIX do not use value.
                - Every numeric/property platform predicate must be accompanied by a top-level payload.ongroundServiceEvent.eventsType condition using CONTAINS or CONTAINS_ANY with DEPARTING, DEPARTED, ARRIVING or ARRIVED.
                - For a current platform change event, use payload.ongroundServiceEvent.eventsType with CONTAINS DEPARTURE_PLATFORM_CHANGED, CONTAINS ARRIVAL_PLATFORM_CHANGED or CONTAINS_ANY ["DEPARTURE_PLATFORM_CHANGED","ARRIVAL_PLATFORM_CHANGED"] according to the requested direction.
                - Do not use departureStatuses[].status or arrivalStatuses[].status as the principal signal for a current platform change or movement prompt.
                - For equality platform operators, if the user says only binario/platform/quay/banchina/marciapiede plus a human value, use timetabledArrivalPlatform.dsc for arrival and timetabledDeparturePlatform.dsc for departure.
                - If MAIN_EVENT_INTENT=ARRIVAL and the platform constraint is a simple equality such as "binario 1", use timetabledArrivalPlatform.dsc with EQUAL_PLATFORM unless the user explicitly asks for real/current/effective/updated platform.
                - If MAIN_EVENT_INTENT=DEPARTURE and the platform constraint is a simple equality such as "binario 1", use timetabledDeparturePlatform.dsc with EQUAL_PLATFORM unless the user explicitly asks for real/current/effective/updated platform.
                - For numeric/property platform operators, use actualArrivalPlatform.platform.dsc for arrival and actualDeparturePlatform.platform.dsc for departure by default. Use timetabled* only for explicit previsto, programmato, da orario, pianificato, timetabled or scheduled wording.
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
                - Negative destination wording such as "non ha destinazione X", "destination is not X", "not destination X" and "destino diverso da X" always maps to timetabledCallEnd/callEnd stopPoint fields. Never map an excluded destination to payload.stopPointJourney.stopPoint.* or payload.ongroundServiceEvent.stopPoint.*.
                - Keep platform constraints inside anyElement on payload.stopPointJourney.stopPointsJourneyDetails[]: timetabledArrivalPlatform.dsc for default arrival platform matching and timetabledDeparturePlatform.dsc for default departure platform matching.
                - When multiple requested constraints use fields under payload.stopPointJourney.stopPointsJourneyDetails[], put them in one anyElement with path payload.stopPointJourney.stopPointsJourneyDetails[] and conditions.all so vehicleJourneyName, delay, platform and origin/destination constraints stay correlated to the same journey detail.
                - Keep cancellation state filters inside that same stopPointsJourneyDetails[] anyElement when they refer to the same journey detail as delay, platform, origin or destination filters.
                - Generic cancelled/suppressed journey correlation uses an OR inside the same stopPointsJourneyDetails[] element: arrival cancellation plus departure cancellation; OR arrival cancellation plus passingType DESTINATION; OR departure cancellation plus passingType ORIGIN.
                - Exception for numeric/property platform operators: keep the constraint inside anyElement but default to actualArrivalPlatform.platform.dsc or actualDeparturePlatform.platform.dsc and add the top-level current payload.ongroundServiceEvent.eventsType binding.
                - Keep current platform-change event evidence at top level on payload.ongroundServiceEvent.eventsType. Keep structural platform comparisons inside anyElement on payload.stopPointJourney.stopPointsJourneyDetails[].
                - Do not use departureStatuses[].status or arrivalStatuses[].status as the principal signal for a current platform change or movement prompt.
                - A top-level current stop location and a platform constraint inside stopPointsJourneyDetails[] do not need to be inside the same anyElement.
                - For a future stop described within the received journey payload, use anyElement on nextCalls[] or nextTransitCalls[] as appropriate.
                - Route-only prompts such as "will pass through X", "will pass through X and then Y", "via X" or "passera da X" are fully mappable on nextCalls[]/nextTransitCalls[] and do not require payload.ongroundServiceEvent.eventsType.
                - If the LocationContext has no MAIN_EVENT_LOCATION, do not invent payload.stopPointJourney.stopPoint.* or payload.ongroundServiceEvent.stopPoint.* conditions for route/transit locations.
                - TRANSIT_LOCATION preferred mapping is nextTransitCalls[].stopPoint.*. Do not add passingType inside nextTransitCalls[]; that array already means transit.
                - Alternative TRANSIT_LOCATION mapping is nextCalls[].stopPoint.* plus nextCalls[].passingType EQUALS TRANSIT when the catalog allows it.
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
                - "binario tronco" is not supported. Reject with reason: "Bay/terminal/dead-end platform is not available in the ServiceData Capability Catalog."
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
                - mappedBy must list only the exact fields actually used in technicalSpecification; do not list alternative fields that were not emitted and do not list fields absent from the catalog.
                - For a resolved current-stop location matched through payload.stopPointJourney.stopPoint.id or payload.ongroundServiceEvent.stopPoint.id, mappedBy must contain exactly the field used by the condition.
                - For a platform requirement, mappedBy must contain the exact platform description field used by the condition, for example payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDeparturePlatform.dsc.
                - For a platform numeric/property requirement, mappedBy must include payload.ongroundServiceEvent.eventsType and the exact actual* or explicit timetabled* platform description field used by the condition.
                - Do not invent a location requirement when the prompt contains only a platform constraint. Phrases such as "un binario", "a platform" or "una plataforma" are not locations.
                - If a meaningful resolved location follows a platform predicate, include its current stop-point field in requirementCoverage. For example, "binario pari a Lunigiana" includes the resolved Lunigiana stop-point requirement, while "platform with a letter" alone does not include any stopPoint requirement.
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

                Positive example - generic delay threshold with no location:
                Prompt: "Avvisami quando una corsa ha piu di N minuti di ritardo"
                Backend-derived constraints:
                - DELAY_EVENT_TYPE=BOTH
                - DELAY_THRESHOLD=operator=GREATER_THAN;value=<threshold>;unit=SECONDS
                Expected condition:
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS_ANY","values":["ARRIVAL_DELAY","DEPARTURE_DELAY"]},
                    {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":{"any":[
                      {"field":"arrivalDelay.delay","operator":"GREATER_THAN","value":"<threshold>"},
                      {"field":"departureDelay.delay","operator":"GREATER_THAN","value":"<threshold>"}
                    ]}}}
                  ]
                }
                Decision: VERIFIED

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

                Positive example - progressive current departure stop plus future route location:
                Prompt: "Dimmi quando una corsa e in partenza da X e passera da Y"
                LocationContext:
                - X MAIN_EVENT_LOCATION
                - Y ROUTE_OR_NEXT_CALL_LOCATION
                Recognized non-location constraints:
                - MAIN_EVENT_INTENT DEPARTURE
                - MAIN_EVENT_PHASE PROGRESSIVE
                - EXPECTED_MAIN_EVENT_TYPE DEPARTING
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTING"},
                  {"field":"payload.stopPointJourney.stopPoint.id","operator":"IN","values":["<resolvedXStopPointIds>"]},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"anyElement":{"path":"nextCalls[]","conditions":
                      {"field":"stopPoint.id","operator":"IN","values":["<resolvedYStopPointIds>"]}
                    }}
                  }}
                ]}
                Do not put X on callStart.stopPoint.id or timetabledCallStart.stopPoint.id.
                Decision: VERIFIED

                Positive example - completed current departure stop plus future route location:
                Prompt: "Dimmi quando una corsa parte da X e passera da Y"
                Recognized non-location constraints:
                - MAIN_EVENT_INTENT DEPARTURE
                - MAIN_EVENT_PHASE COMPLETED
                - EXPECTED_MAIN_EVENT_TYPE DEPARTED
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTED"},
                  {"field":"payload.stopPointJourney.stopPoint.id","operator":"IN","values":["<resolvedXStopPointIds>"]},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"anyElement":{"path":"nextCalls[]","conditions":
                      {"field":"stopPoint.id","operator":"IN","values":["<resolvedYStopPointIds>"]}
                    }}
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

                Positive example - departure platform number greater than 5:
                Prompt: "Avvertimi quando un treno e in partenza da binario maggiore di 5"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTING"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"actualDeparturePlatform.platform.dsc","operator":"PLATFORM_NUMBER_GREATER_THAN","value":5}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - arrival platform number between 3 and 8:
                Prompt: "Avvertimi quando una corsa arriva a un binario compreso tra 3 e 8"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"ARRIVED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"actualArrivalPlatform.platform.dsc","operator":"PLATFORM_NUMBER_BETWEEN","value":{"min":3,"max":8}}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - even departure platform:
                Prompt: "Avvertimi quando una corsa parte da un binario pari"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"actualDeparturePlatform.platform.dsc","operator":"PLATFORM_NUMBER_EVEN"}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - departure platform with letter suffix:
                Prompt: "Avvertimi quando una corsa parte da un binario con una lettera"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"actualDeparturePlatform.platform.dsc","operator":"PLATFORM_HAS_LETTER_SUFFIX"}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - English precise completed departure platform event:
                Prompt: "Notify me when a train departs from a platform with a letter"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"actualDeparturePlatform.platform.dsc","operator":"PLATFORM_HAS_LETTER_SUFFIX"}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - resolved location plus even departure platform:
                Prompt: "Avvertimi quando una corsa parte da un binario pari a Lunigiana"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.stopPointJourney.stopPoint.id","operator":"IN","values":["<resolvedLunigianaStopPointIds>"]},
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"actualDeparturePlatform.platform.dsc","operator":"PLATFORM_NUMBER_EVEN"}
                  }}
                ]}
                Decision: VERIFIED

                Positive example - explicitly timetabled departure platform:
                Prompt: "Avvertimi quando una corsa parte da binario previsto maggiore di 5"
                Expected condition:
                {"type":"SERVICE_DATA_FIELD_MATCH","all":[
                  {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTED"},
                  {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[]","conditions":
                    {"field":"timetabledDeparturePlatform.dsc","operator":"PLATFORM_NUMBER_GREATER_THAN","value":5}
                  }}
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
