package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_runtime_event", schema = "pis_intelligentinformationassistant")
public class AgentRuntimeEvent {
    @Id
    @Size(max = 50)
    @CodGeneratedId("AGRE")
    @ColumnDefault("generate_cod_id('AGRE')")
    @Column(name = "cod_agentruntimeevent", nullable = false, length = 50)
    private String codAgentruntimeevent;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_agentrun", nullable = false)
    private AgentRun codAgentrun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_agentdefinition")
    private AgentDefinition codAgentdefinition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_eventtype", nullable = false)
    private AgentRuntimeEventType sglEventtype;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'INFO'")
    @JoinColumn(name = "sgl_severity", nullable = false)
    private AgentRuntimeEventSeverity sglSeverity;

    @Column(name = "dsc_reason", length = Integer.MAX_VALUE)
    private String dscReason;

    @Column(name = "jsn_details")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnDetails;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_occurredat", nullable = false)
    private OffsetDateTime dtOccurredat;

}