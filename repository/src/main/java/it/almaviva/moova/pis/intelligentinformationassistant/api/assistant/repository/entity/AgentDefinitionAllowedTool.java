package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_definition_allowed_tool", schema = "pis_intelligentinformationassistant")
public class AgentDefinitionAllowedTool {
    @EmbeddedId
    private AgentDefinitionAllowedToolId id;

    @MapsId("codAgentdefinition")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_agentdefinition", nullable = false)
    private AgentDefinition codAgentdefinition;

    @Column(name = "jsn_operations")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnOperations;

}