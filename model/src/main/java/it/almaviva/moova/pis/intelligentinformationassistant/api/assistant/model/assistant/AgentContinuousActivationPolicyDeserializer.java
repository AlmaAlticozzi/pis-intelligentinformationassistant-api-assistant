package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRulesException;

public class AgentContinuousActivationPolicyDeserializer extends StdDeserializer<AgentContinuousActivationPolicy> {

    private static final String OFFSET_SHAPE = "OFFSET";
    private static final String LOCAL_SHAPE = "LOCAL";

    public AgentContinuousActivationPolicyDeserializer() {
        super(AgentContinuousActivationPolicy.class);
    }

    @Override
    public AgentContinuousActivationPolicy deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        String timezone = text(node, "timezone");

        ParsedDateTime validFrom = parseDateTime(node.get("validFrom"), timezone, "validFrom", context);
        ParsedDateTime validTo = parseDateTime(node.get("validTo"), timezone, "validTo", context);
        System.out.println("[IIA][AGENT_DEFINITION_CREATE][ACTIVATION_POLICY_PARSE] type=CONTINUOUS"
                + " timezone=" + timezone
                + " validFromInputShape=" + validFrom.shape()
                + " validToInputShape=" + validTo.shape()
                + " action=parsed");

        return new AgentContinuousActivationPolicy()
                .type(AgentActivationPolicy.TypeEnum.CONTINUOUS)
                .timezone(timezone)
                .validFrom(validFrom.value())
                .validTo(validTo.value());
    }

    private ParsedDateTime parseDateTime(
            JsonNode value,
            String timezone,
            String field,
            DeserializationContext context) throws JsonMappingException {
        if (value == null || value.isNull() || (value.isTextual() && value.asText().isBlank())) {
            return new ParsedDateTime(null, null);
        }
        if (value.isNumber()) {
            return new ParsedDateTime(fromJacksonTimestamp(value.decimalValue()), OFFSET_SHAPE);
        }
        String text = value.asText().trim();
        try {
            if (hasOffset(text)) {
                return new ParsedDateTime(OffsetDateTime.parse(text), OFFSET_SHAPE);
            }
            if (timezone == null || timezone.isBlank()) {
                throw invalid(field, context, null);
            }
            ZoneId zoneId = ZoneId.of(timezone);
            return new ParsedDateTime(LocalDateTime.parse(text).atZone(zoneId).toOffsetDateTime(), LOCAL_SHAPE);
        } catch (DateTimeParseException | ZoneRulesException ex) {
            System.out.println("[IIA][AGENT_DEFINITION_CREATE][ACTIVATION_POLICY_PARSE] type=CONTINUOUS"
                    + " timezone=" + timezone
                    + " field=" + field
                    + " action=rejected");
            throw invalid(field, context, ex);
        }
    }

    private JsonMappingException invalid(String field, DeserializationContext context, Exception cause) {
        String message = field + " must be a valid date-time with offset or a local date-time interpreted using activationPolicy.timezone.";
        return JsonMappingException.from(context.getParser(), message, cause);
    }

    private boolean hasOffset(String value) {
        return value.endsWith("Z") || value.matches(".*[+-]\\d{2}:\\d{2}$");
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private OffsetDateTime fromJacksonTimestamp(BigDecimal timestamp) {
        long seconds = timestamp.longValue();
        int nanos = timestamp.subtract(BigDecimal.valueOf(seconds)).movePointRight(9).intValue();
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), ZoneOffset.UTC);
    }

    private record ParsedDateTime(OffsetDateTime value, String shape) {
    }
}
