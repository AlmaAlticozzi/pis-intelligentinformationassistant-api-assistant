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
@Table(name = "agent_run_log", schema = "pis_intelligentinformationassistant")
public class AgentRunLog {
    @EmbeddedId
    private AgentRunLogId id;

    @MapsId("codAgentrun")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_agentrun", nullable = false)
    private AgentRun codAgentrun;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_occurredat", nullable = false)
    private OffsetDateTime dtOccurredat;

    @Size(max = 30)
    @NotNull
    @Column(name = "sgl_level", nullable = false, length = 30)
    private String sglLevel;

    @NotNull
    @Column(name = "dsc_message", nullable = false, length = Integer.MAX_VALUE)
    private String dscMessage;

    @Column(name = "jsn_context")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnContext;

}