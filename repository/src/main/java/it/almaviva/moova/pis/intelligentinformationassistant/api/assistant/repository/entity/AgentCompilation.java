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
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_compilation", schema = "pis_intelligentinformationassistant")
public class AgentCompilation {
    @Id
    @Size(max = 50)
    @CodGeneratedId("AGCP")
    @ColumnDefault("generate_cod_id('AGCP')")
    @Column(name = "cod_agentcompilation", nullable = false, length = 50)
    private String codAgentcompilation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_agentdefinition", nullable = false)
    private AgentDefinition codAgentdefinition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'PENDING'")
    @JoinColumn(name = "sgl_status", nullable = false)
    private AgentCompilationStatus sglStatus;

    @Size(max = 120)
    @Column(name = "dsc_currentstep", length = 120)
    private String dscCurrentstep;

    @Size(max = 30)
    @Column(name = "dsc_requestedmode", length = 30)
    private String dscRequestedmode;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "flg_force", nullable = false)
    private Boolean flgForce = false;

    @NotNull
    @ColumnDefault("'{}'")
    @Column(name = "jsn_request", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnRequest = new HashMap<>();

    @Column(name = "jsn_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnResult;

    @Column(name = "dsc_errormessage", length = Integer.MAX_VALUE)
    private String dscErrormessage;

    @Size(max = 100)
    @Column(name = "cod_requestedby", length = 100)
    private String codRequestedby;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_requestedat", nullable = false)
    private OffsetDateTime dtRequestedat;

    @Column(name = "dt_startedat")
    private OffsetDateTime dtStartedat;

    @Column(name = "dt_completedat")
    private OffsetDateTime dtCompletedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

}
