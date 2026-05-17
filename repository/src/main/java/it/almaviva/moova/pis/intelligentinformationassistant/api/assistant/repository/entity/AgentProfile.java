package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_profile", schema = "pis_intelligentinformationassistant")
public class AgentProfile {
    @Id
    @Size(max = 50)
    @Column(name = "cod_agentprofile", nullable = false, length = 50)
    private String codAgentprofile;

    @Size(max = 120)
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @Column(name = "dsc_name", nullable = false, length = 120)
    private String dscName;

    @Size(max = 1000)
    @Column(name = "dsc_description", length = 1000)
    private String dscDescription;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "flg_enabled", nullable = false)
    private Boolean flgEnabled = true;

    @NotNull
    @ColumnDefault("'[]'")
    @Column(name = "jsn_recommendedfor", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> jsnRecommendedfor = new ArrayList<>();

    @NotNull
    @Column(name = "num_cpurequestmillicores", nullable = false)
    @jakarta.validation.constraints.Positive
    private Integer numCpurequestmillicores;

    @NotNull
    @Column(name = "num_cpulimitmillicores", nullable = false)
    @jakarta.validation.constraints.Positive
    private Integer numCpulimitmillicores;

    @NotNull
    @Column(name = "num_memoryrequestmib", nullable = false)
    @jakarta.validation.constraints.Positive
    private Integer numMemoryrequestmib;

    @NotNull
    @Column(name = "num_memorylimitmib", nullable = false)
    @jakarta.validation.constraints.Positive
    private Integer numMemorylimitmib;

    @Size(max = 120)
    @NotNull
    @ColumnDefault("'TOOL_GATEWAY_ONLY'")
    @Column(name = "dsc_networkpolicy", nullable = false, length = 120)
    private String dscNetworkpolicy = "TOOL_GATEWAY_ONLY";

    @NotNull
    @ColumnDefault("1")
    @Column(name = "num_maxruntimeconcurrency", nullable = false)
    @jakarta.validation.constraints.Min(1)
    private Integer numMaxruntimeconcurrency = 1;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

}
