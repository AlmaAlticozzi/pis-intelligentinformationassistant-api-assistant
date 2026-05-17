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
import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_output", schema = "pis_intelligentinformationassistant")
public class AgentOutput {
    @Id
    @Size(max = 50)
    @CodGeneratedId("AGOU")
    @ColumnDefault("generate_cod_id('AGOU')")
    @Column(name = "cod_agentoutput", nullable = false, length = 50)
    private String codAgentoutput;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_agentrun", nullable = false)
    private AgentRun codAgentrun;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_agentdefinition", nullable = false)
    private AgentDefinition codAgentdefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_alert")
    private Alert codAlert;

    @Column(name = "num_alertversion")
    @jakarta.validation.constraints.Min(1)
    private Integer numAlertversion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'RECEIVED'")
    @JoinColumn(name = "sgl_status", nullable = false)
    private AgentOutputStatus sglStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_outputtype", nullable = false)
    private AgentOutputType sglOutputtype;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_targettype")
    private SuggestionTargetType sglTargettype;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_severity")
    private SuggestionSeverity sglSeverity;

    @Column(name = "num_confidence", precision = 5, scale = 4)
    @jakarta.validation.constraints.DecimalMin("0.0")
    @jakarta.validation.constraints.DecimalMax("1.0")
    private BigDecimal numConfidence;

    @Size(max = 255)
    @Column(name = "dsc_targettitle")
    private String dscTargettitle;

    @Size(max = 255)
    @Column(name = "dsc_targetsubtitle")
    private String dscTargetsubtitle;

    @Column(name = "dsc_reason", length = Integer.MAX_VALUE)
    private String dscReason;

    @Column(name = "dsc_operatoradvice", length = Integer.MAX_VALUE)
    private String dscOperatoradvice;

    @Column(name = "dsc_passengermessageproposal", length = Integer.MAX_VALUE)
    private String dscPassengermessageproposal;

    @Size(max = 500)
    @Column(name = "cod_deduplicationkey", length = 500)
    private String codDeduplicationkey;

    @Size(max = 120)
    @Column(name = "dsc_validationerrorcode", length = 120)
    private String dscValidationerrorcode;

    @Column(name = "dsc_validationerrormessage", length = Integer.MAX_VALUE)
    private String dscValidationerrormessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_suggestion")
    private Suggestion codSuggestion;

    @Column(name = "jsn_target")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnTarget;

    @Column(name = "jsn_rawoutput")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnRawoutput;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_generatedat", nullable = false)
    private OffsetDateTime dtGeneratedat;

    @Column(name = "dt_processedat")
    private OffsetDateTime dtProcessedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

}
