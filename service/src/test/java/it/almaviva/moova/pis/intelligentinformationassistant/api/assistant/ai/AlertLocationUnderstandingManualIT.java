package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertLocationUnderstandingManualIT {

    private static final String PREFIX = "[IIA][ALERT_LOCATION_UNDERSTANDING][MANUAL_TEST]";

    private static final List<String> PROMPTS = List.of(
            "Dimmi quando una corsa parte da Garibaldi e passerà da Venezia",
            "Avvisa quando una corsa parte da Milano Centrale ed è soppressa a Grosio",
            "Avvisa quando un treno è in arrivo a Valdisotto o Monza o Arcore ed ha origine Genova Sestri Ponente e destino LECCO MAGGIANICO",
            "Avvisa quando un treno subisce un ritardo in arrivo di almeno 20 minuti su treno che passerà in transito da DUOMO e SAN BABILA",
            "Avvisa quando il treno 775 è in arrivo o in partenza da Garibaldi FS e ha come via Monza",
            "Segnala quando un bus parte da una località di origine Malpensa T1",
            "Segnala quando un bus arriva a destino",
            "Crea un suggerimento quando sul binario 1 arriva la corsa 1278 con un ritardo di 14 min a Pescara. La corsa non deve avere come destinazione Bologna",
            "Tell me when train 775 arrives at Garibaldi FS and then passes through Monza",
            "Warn me when a bus departs from Malpensa T1 and has destination Lecco Maggianico");

    @Test
    @EnabledIfSystemProperty(named = "iia.alert-location-understanding.manual", matches = "true")
    void printLocationUnderstandingDiagnostics() {
        AlertLocationUnderstandingService service = service(new ManualDiagnosticGateway());
        for (int index = 0; index < PROMPTS.size(); index++) {
            String prompt = PROMPTS.get(index);
            AlertLocationUnderstandingResult result = service.understandLocations(prompt, "MANUAL-" + (index + 1));
            print(prompt, result);
        }
    }

    private void print(String prompt, AlertLocationUnderstandingResult result) {
        System.out.println(PREFIX + " prompt=" + prompt);
        System.out.println(PREFIX + " hasLocations=" + result.hasLocations());
        System.out.println(PREFIX + " language=" + result.language());
        System.out.println(PREFIX + " mainEvent.eventIntent=" + result.mainEvent().eventIntent());
        System.out.println(PREFIX + " mainEvent.confidence=" + result.mainEvent().confidence());
        for (int index = 0; index < result.locations().size(); index++) {
            AlertLocationUnderstandingLocation location = result.locations().get(index);
            System.out.println(PREFIX + " location[" + index + "].rawText=" + location.rawText());
            System.out.println(PREFIX + " location[" + index + "].normalizedText=" + location.normalizedText());
            System.out.println(PREFIX + " location[" + index + "].role=" + location.role());
            System.out.println(PREFIX + " location[" + index + "].relationToMainEvent=" + location.relationToMainEvent());
            System.out.println(PREFIX + " location[" + index + "].requiredCoverage=" + location.requiredCoverage());
            System.out.println(PREFIX + " location[" + index + "].polarity=" + location.polarity());
            System.out.println(PREFIX + " location[" + index + "].logicalGroup=" + location.logicalGroup());
            System.out.println(PREFIX + " location[" + index + "].confidence=" + location.confidence());
        }
        System.out.println(PREFIX + " nonLocationConstraints=" + result.nonLocationConstraints());
        System.out.println(PREFIX + " warnings=" + result.warnings());
    }

    @SuppressWarnings("unchecked")
    private AlertLocationUnderstandingService service(LlmGateway gateway) {
        Instance<LlmGateway> instance = mock(Instance.class);
        when(instance.isUnsatisfied()).thenReturn(false);
        when(instance.get()).thenReturn(gateway);
        return new AlertLocationUnderstandingService(
                new AlertLocationUnderstandingPromptBuilder(),
                new AlertLocationUnderstandingLlmResponseParser(),
                instance);
    }

    private static class ManualDiagnosticGateway implements LlmGateway {

        @Override
        public LlmResponse generateText(LlmRequest request) {
            return new LlmResponse(responseFor(request.correlationId()), "MANUAL_DIAGNOSTIC", request.model(), null, null, null);
        }

        private String responseFor(String correlationId) {
            return switch (correlationId) {
                case "MANUAL-1" -> """
                        {"hasLocations":true,"language":"it","mainEvent":{"eventIntent":"DEPARTURE","confidence":0.90},"locations":[
                          {"rawText":"Garibaldi","normalizedText":"Garibaldi","role":"MAIN_EVENT_LOCATION","relationToMainEvent":"EVENT_STOP_POINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.90},
                          {"rawText":"Venezia","normalizedText":"Venezia","role":"ROUTE_OR_NEXT_CALL_LOCATION","relationToMainEvent":"FUTURE_ROUTE_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G2","confidence":0.86}
                        ],"nonLocationConstraints":[],"warnings":["Manual diagnostic gateway output."]}
                        """;
                case "MANUAL-2" -> """
                        {"hasLocations":true,"language":"it","mainEvent":{"eventIntent":"DEPARTURE","confidence":0.86},"locations":[
                          {"rawText":"Milano Centrale","normalizedText":"Milano Centrale","role":"MAIN_EVENT_LOCATION","relationToMainEvent":"EVENT_STOP_POINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.86},
                          {"rawText":"Grosio","normalizedText":"Grosio","role":"CANCELLED_CALL_LOCATION","relationToMainEvent":"CANCELLED_CALL_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G2","confidence":0.84}
                        ],"nonLocationConstraints":[],"warnings":["Manual diagnostic gateway output."]}
                        """;
                case "MANUAL-3" -> """
                        {"hasLocations":true,"language":"it","mainEvent":{"eventIntent":"ARRIVAL","confidence":0.88},"locations":[
                          {"rawText":"Valdisotto","normalizedText":"Valdisotto","role":"MAIN_EVENT_LOCATION","relationToMainEvent":"EVENT_STOP_POINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.84},
                          {"rawText":"Monza","normalizedText":"Monza","role":"MAIN_EVENT_LOCATION","relationToMainEvent":"EVENT_STOP_POINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.84},
                          {"rawText":"Arcore","normalizedText":"Arcore","role":"MAIN_EVENT_LOCATION","relationToMainEvent":"EVENT_STOP_POINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.84},
                          {"rawText":"Genova Sestri Ponente","normalizedText":"Genova Sestri Ponente","role":"ORIGIN_LOCATION","relationToMainEvent":"ORIGIN_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G2","confidence":0.86},
                          {"rawText":"LECCO MAGGIANICO","normalizedText":"Lecco Maggianico","role":"DESTINATION_LOCATION","relationToMainEvent":"DESTINATION_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G3","confidence":0.86}
                        ],"nonLocationConstraints":[],"warnings":["Manual diagnostic gateway output."]}
                        """;
                case "MANUAL-4" -> """
                        {"hasLocations":true,"language":"it","mainEvent":{"eventIntent":"DELAY","confidence":0.85},"locations":[
                          {"rawText":"DUOMO","normalizedText":"Duomo","role":"TRANSIT_LOCATION","relationToMainEvent":"TRANSIT_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.84},
                          {"rawText":"SAN BABILA","normalizedText":"San Babila","role":"TRANSIT_LOCATION","relationToMainEvent":"TRANSIT_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.84}
                        ],"nonLocationConstraints":[{"type":"DELAY","rawText":"ritardo in arrivo di almeno 20 minuti"}],"warnings":["Manual diagnostic gateway output."]}
                        """;
                case "MANUAL-5" -> """
                        {"hasLocations":true,"language":"it","mainEvent":{"eventIntent":"DEPARTURE_OR_ARRIVAL","confidence":0.88},"locations":[
                          {"rawText":"Garibaldi FS","normalizedText":"Garibaldi FS","role":"MAIN_EVENT_LOCATION","relationToMainEvent":"EVENT_STOP_POINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.88},
                          {"rawText":"Monza","normalizedText":"Monza","role":"ROUTE_OR_NEXT_CALL_LOCATION","relationToMainEvent":"FUTURE_ROUTE_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G2","confidence":0.86}
                        ],"nonLocationConstraints":[{"type":"VEHICLE_JOURNEY","rawText":"treno 775"}],"warnings":["Manual diagnostic gateway output."]}
                        """;
                case "MANUAL-6" -> """
                        {"hasLocations":true,"language":"it","mainEvent":{"eventIntent":"DEPARTURE","confidence":0.88},"locations":[
                          {"rawText":"Malpensa T1","normalizedText":"Malpensa T1","role":"ORIGIN_LOCATION","relationToMainEvent":"ORIGIN_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.88}
                        ],"nonLocationConstraints":[],"warnings":["Manual diagnostic gateway output."]}
                        """;
                case "MANUAL-7" -> """
                        {"hasLocations":false,"language":"it","mainEvent":{"eventIntent":"ARRIVAL","confidence":0.80},"locations":[],"nonLocationConstraints":[],"warnings":["Destination passing type requested without a named location.","Manual diagnostic gateway output."]}
                        """;
                case "MANUAL-8" -> """
                        {"hasLocations":true,"language":"it","mainEvent":{"eventIntent":"ARRIVAL","confidence":0.88},"locations":[
                          {"rawText":"Pescara","normalizedText":"Pescara","role":"MAIN_EVENT_LOCATION","relationToMainEvent":"EVENT_STOP_POINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.88},
                          {"rawText":"Bologna","normalizedText":"Bologna","role":"DESTINATION_LOCATION","relationToMainEvent":"DESTINATION_CONSTRAINT","requiredCoverage":true,"polarity":"EXCLUDE","logicalGroup":"G2","confidence":0.86}
                        ],"nonLocationConstraints":[{"type":"PLATFORM","rawText":"binario 1"},{"type":"VEHICLE_JOURNEY","rawText":"corsa 1278"},{"type":"DELAY","rawText":"ritardo di 14 min"}],"warnings":["Manual diagnostic gateway output."]}
                        """;
                case "MANUAL-9" -> """
                        {"hasLocations":true,"language":"en","mainEvent":{"eventIntent":"ARRIVAL","confidence":0.90},"locations":[
                          {"rawText":"Garibaldi FS","normalizedText":"Garibaldi FS","role":"MAIN_EVENT_LOCATION","relationToMainEvent":"EVENT_STOP_POINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.90},
                          {"rawText":"Monza","normalizedText":"Monza","role":"ROUTE_OR_NEXT_CALL_LOCATION","relationToMainEvent":"FUTURE_ROUTE_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G2","confidence":0.88}
                        ],"nonLocationConstraints":[{"type":"VEHICLE_JOURNEY","rawText":"train 775"}],"warnings":["Manual diagnostic gateway output."]}
                        """;
                case "MANUAL-10" -> """
                        {"hasLocations":true,"language":"en","mainEvent":{"eventIntent":"DEPARTURE","confidence":0.88},"locations":[
                          {"rawText":"Malpensa T1","normalizedText":"Malpensa T1","role":"MAIN_EVENT_LOCATION","relationToMainEvent":"EVENT_STOP_POINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G1","confidence":0.86},
                          {"rawText":"Lecco Maggianico","normalizedText":"Lecco Maggianico","role":"DESTINATION_LOCATION","relationToMainEvent":"DESTINATION_CONSTRAINT","requiredCoverage":true,"polarity":"INCLUDE","logicalGroup":"G2","confidence":0.88}
                        ],"nonLocationConstraints":[],"warnings":["Manual diagnostic gateway output."]}
                        """;
                default -> """
                        {"hasLocations":false,"language":"","mainEvent":{"eventIntent":"UNKNOWN","confidence":0.0},"locations":[],"nonLocationConstraints":[],"warnings":["No manual diagnostic response configured."]}
                        """;
            };
        }
    }
}
