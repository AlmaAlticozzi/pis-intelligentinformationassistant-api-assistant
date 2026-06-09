package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgentProfileMapperTest {

    private final AgentProfileMapper mapper = new AgentProfileMapper();

    @Test
    void mapsPersistedFieldsAndRuntimeGovernanceDefaults() {
        AgentProfile entity = new AgentProfile();
        entity.setCodAgentprofile("MEDIUM");
        entity.setDscName("Medium Agent");
        entity.setDscDescription("Default profile");
        entity.setFlgEnabled(false);
        entity.setJsnRecommendedfor(List.of("Scheduled checks", "Moderate event correlation"));
        entity.setNumCpurequestmillicores(250);
        entity.setNumCpulimitmillicores(700);
        entity.setNumMemoryrequestmib(256);
        entity.setNumMemorylimitmib(768);
        entity.setDscNetworkpolicy("TOOL_GATEWAY_ONLY");
        entity.setNumMaxruntimeconcurrency(2);

        var dto = mapper.toDto(entity);

        assertThat(dto.getId()).isEqualTo("MEDIUM");
        assertThat(dto.getName()).isEqualTo("Medium Agent");
        assertThat(dto.getDescription()).isEqualTo("Default profile");
        assertThat(dto.getEnabled()).isFalse();
        assertThat(dto.getRecommendedFor()).containsExactly("Scheduled checks", "Moderate event correlation");
        assertThat(dto.getCpuRequestMillicores()).isEqualTo(250);
        assertThat(dto.getCpuLimitMillicores()).isEqualTo(700);
        assertThat(dto.getMemoryRequestMiB()).isEqualTo(256);
        assertThat(dto.getMemoryLimitMiB()).isEqualTo(768);
        assertThat(dto.getNetworkPolicy()).isEqualTo("TOOL_GATEWAY_ONLY");
        assertThat(dto.getMaxRuntimeConcurrency()).isEqualTo(2);
        assertThat(dto.getSupportedInterpreterTypes())
                .containsExactly(AlertInterpreterType.EVENT_INTERPRETER, AlertInterpreterType.SCHEDULED_INTERPRETER);
        assertThat(dto.getSupportedTriggerTypes()).containsExactly("EVENT", "SCHEDULE");
        assertThat(dto.getSupportsStatefulExecution()).isFalse();
        assertThat(dto.getRuntimeClass()).isEqualTo("STANDARD_DSL_RUNTIME");
        assertThat(dto.getMinScheduleIntervalSeconds()).isNull();
        assertThat(dto.getMaxServiceDataStopPoints()).isNull();
        assertThat(dto.getMaxConditionNodes()).isNull();
    }
}
