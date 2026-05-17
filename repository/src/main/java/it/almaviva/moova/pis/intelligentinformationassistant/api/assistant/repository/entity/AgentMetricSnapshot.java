package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_metric_snapshot", schema = "pis_intelligentinformationassistant")
public class AgentMetricSnapshot {
    @EmbeddedId
    private AgentMetricSnapshotId id;

    @MapsId("codAgentrun")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_agentrun", nullable = false)
    private AgentRun codAgentrun;

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
    @Column(name = "num_generatedoutputs", nullable = false)
    private Long numGeneratedoutputs = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_invalidoutputs", nullable = false)
    private Long numInvalidoutputs = 0L;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_errorscount", nullable = false)
    private Long numErrorscount = 0L;

}
