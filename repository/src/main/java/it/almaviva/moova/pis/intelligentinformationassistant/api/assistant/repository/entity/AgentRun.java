package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_run", schema = "pis_intelligentinformationassistant")
public class AgentRun {
    @Id
    @Size(max = 50)
    @CodGeneratedId("AGRN")
    @ColumnDefault("generate_cod_id('AGRN')")
    @Column(name = "cod_agentrun", nullable = false, length = 50)
    private String codAgentrun;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_agentdefinition", nullable = false)
    private AgentDefinition codAgentdefinition;

    @Size(max = 100)
    @Column(name = "cod_profile", length = 100)
    private String codProfile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'PENDING'")
    @JoinColumn(name = "sgl_status", nullable = false)
    private AgentRunStatus sglStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'UNKNOWN'")
    @JoinColumn(name = "sgl_healthstatus", nullable = false)
    private AgentHealthStatus sglHealthstatus;

    @Column(name = "num_healthscore")
    @jakarta.validation.constraints.Min(0)
    @jakarta.validation.constraints.Max(100)
    private Integer numHealthscore;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'UNKNOWN'")
    @JoinColumn(name = "sgl_qualitystatus", nullable = false)
    private AgentQualityStatus sglQualitystatus;

    @Column(name = "num_qualityscore")
    @jakarta.validation.constraints.Min(0)
    @jakarta.validation.constraints.Max(100)
    private Integer numQualityscore;

    @NotNull
    @ColumnDefault("'[]'")
    @Column(name = "jsn_mainissues", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> jsnMainissues = new ArrayList<>();

    @Size(max = 120)
    @Column(name = "dsc_k8snamespace", length = 120)
    private String dscK8snamespace;

    @Size(max = 30)
    @Column(name = "dsc_k8sresourcekind", length = 30)
    private String dscK8sresourcekind;

    @Size(max = 255)
    @Column(name = "dsc_k8sresourcename")
    private String dscK8sresourcename;

    @Size(max = 255)
    @Column(name = "dsc_k8spodname")
    private String dscK8spodname;

    @Size(max = 255)
    @Column(name = "dsc_containerimage")
    private String dscContainerimage;

    @Size(max = 120)
    @Column(name = "dsc_podphase", length = 120)
    private String dscPodphase;

    @Size(max = 120)
    @Column(name = "dsc_containerstate", length = 120)
    private String dscContainerstate;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_restartcount", nullable = false)
    @jakarta.validation.constraints.Min(0)
    private Integer numRestartcount = 0;

    @Size(max = 255)
    @Column(name = "dsc_nodename")
    private String dscNodename;

    @Column(name = "num_cpumillicores", precision = 12, scale = 3)
    private BigDecimal numCpumillicores;

    @Column(name = "num_cpurequestmillicores")
    private Integer numCpurequestmillicores;

    @Column(name = "num_cpulimitmillicores")
    private Integer numCpulimitmillicores;

    @Column(name = "num_cpuusagepercent", precision = 7, scale = 3)
    private BigDecimal numCpuusagepercent;

    @Column(name = "num_memorybytes")
    private Long numMemorybytes;

    @Column(name = "num_memorylimitbytes")
    private Long numMemorylimitbytes;

    @Column(name = "num_memoryusagepercent", precision = 7, scale = 3)
    private BigDecimal numMemoryusagepercent;

    @Column(name = "num_networkrxbytes")
    private Long numNetworkrxbytes;

    @Column(name = "num_networktxbytes")
    private Long numNetworktxbytes;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_processedevents", nullable = false)
    private Long numProcessedevents = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_candidateoutputs", nullable = false)
    private Long numCandidateoutputs = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_validoutputs", nullable = false)
    private Long numValidoutputs = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_discardedoutputs", nullable = false)
    private Long numDiscardedoutputs = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_deduplicatedoutputs", nullable = false)
    private Long numDeduplicatedoutputs = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_createdsuggestions", nullable = false)
    private Long numCreatedsuggestions = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_approvedsuggestions", nullable = false)
    private Long numApprovedsuggestions = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_rejectedsuggestions", nullable = false)
    private Long numRejectedsuggestions = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_modifiedsuggestions", nullable = false)
    private Long numModifiedsuggestions = 0L;

    @Column(name = "num_averageconfidence", precision = 5, scale = 4)
    @jakarta.validation.constraints.DecimalMin("0.0")
    @jakarta.validation.constraints.DecimalMax("1.0")
    private BigDecimal numAverageconfidence;

    @Column(name = "num_approvalrate", precision = 7, scale = 4)
    @jakarta.validation.constraints.DecimalMin("0.0")
    private BigDecimal numApprovalrate;

    @Column(name = "num_rejectionrate", precision = 7, scale = 4)
    @jakarta.validation.constraints.DecimalMin("0.0")
    private BigDecimal numRejectionrate;

    @Column(name = "dt_startedat")
    private OffsetDateTime dtStartedat;

    @Column(name = "dt_stoppedat")
    private OffsetDateTime dtStoppedat;

    @Column(name = "dt_lastheartbeatat")
    private OffsetDateTime dtLastheartbeatat;

    @Column(name = "dt_lasterrorat")
    private OffsetDateTime dtLasterrorat;

    @Size(max = 120)
    @Column(name = "dsc_lasterrorcode", length = 120)
    private String dscLasterrorcode;

    @Column(name = "dsc_lasterrormessage", length = Integer.MAX_VALUE)
    private String dscLasterrormessage;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

}
