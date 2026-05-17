package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
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
@Table(name = "agent_compilation_step", schema = "pis_intelligentinformationassistant")
public class AgentCompilationStep {
    @EmbeddedId
    private AgentCompilationStepId id;

    @MapsId("codAgentcompilation")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_agentcompilation", nullable = false)
    private AgentCompilation codAgentcompilation;

    @Size(max = 120)
    @NotNull
    @Column(name = "dsc_stepname", nullable = false, length = 120)
    private String dscStepname;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_status", nullable = false)
    private AgentCompilationStatus sglStatus;

    @Column(name = "dsc_message", length = Integer.MAX_VALUE)
    private String dscMessage;

    @Column(name = "jsn_details")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnDetails;

    @Column(name = "dt_startedat")
    private OffsetDateTime dtStartedat;

    @Column(name = "dt_completedat")
    private OffsetDateTime dtCompletedat;

}