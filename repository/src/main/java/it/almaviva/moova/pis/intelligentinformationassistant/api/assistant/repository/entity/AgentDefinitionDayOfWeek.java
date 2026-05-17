package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_definition_day_of_week", schema = "pis_intelligentinformationassistant")
public class AgentDefinitionDayOfWeek {
    @EmbeddedId
    private AgentDefinitionDayOfWeekId id;

    @MapsId("codAgentdefinition")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_agentdefinition", nullable = false)
    private AgentDefinition codAgentdefinition;

    @MapsId("sglDayofweek")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_dayofweek", nullable = false)
    private DayOfWeek sglDayofweek;

}