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
@Table(name = "suggestion", schema = "pis_intelligentinformationassistant")
public class Suggestion {
    @Id
    @Size(max = 50)
    @CodGeneratedId("SGGS")
    @ColumnDefault("generate_cod_id('SGGS')")
    @Column(name = "cod_suggestion", nullable = false, length = 50)
    private String codSuggestion;

    @Size(max = 100)
    @Column(name = "cod_profile", length = 100)
    private String codProfile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_alert", nullable = false)
    private Alert codAlert;

    @Column(name = "num_alertversion")
    @jakarta.validation.constraints.Min(1)
    private Integer numAlertversion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_agentdefinition")
    private AgentDefinition codAgentdefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_agentrun")
    private AgentRun codAgentrun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_agentoutput")
    private AgentOutput codAgentoutput;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'TO_REVIEW'")
    @JoinColumn(name = "sgl_status", nullable = false)
    private SuggestionStatus sglStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_targettype", nullable = false)
    private SuggestionTargetType sglTargettype;

    @Size(max = 255)
    @Column(name = "dsc_targettitle")
    private String dscTargettitle;

    @Size(max = 255)
    @Column(name = "dsc_targetsubtitle")
    private String dscTargetsubtitle;

    @Size(max = 120)
    @Column(name = "dsc_eventname", length = 120)
    private String dscEventname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_sourcesystem")
    private SuggestionSourceSystem sglSourcesystem;

    @Size(max = 255)
    @Column(name = "cod_sourceevent")
    private String codSourceevent;

    @Size(max = 255)
    @Column(name = "dsc_sourceeventname")
    private String dscSourceeventname;

    @Column(name = "dt_sourceeventtime")
    private OffsetDateTime dtSourceeventtime;

    @Size(max = 500)
    @Column(name = "cod_correlationkey", length = 500)
    private String codCorrelationkey;

    @Size(max = 1000)
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @Column(name = "dsc_reason", nullable = false, length = 1000)
    private String dscReason;

    @Column(name = "num_confidence", precision = 5, scale = 4)
    @jakarta.validation.constraints.DecimalMin("0.0")
    @jakarta.validation.constraints.DecimalMax("1.0")
    private BigDecimal numConfidence;

    @Size(max = 4000)
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @Column(name = "dsc_operatoradvice", nullable = false, length = 4000)
    private String dscOperatoradvice;

    @Column(name = "dsc_generatedoperatoradvice", length = Integer.MAX_VALUE)
    private String dscGeneratedoperatoradvice;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "flg_operatoradviceedited", nullable = false)
    private Boolean flgOperatoradviceedited = false;

    @Size(max = 4000)
    @Column(name = "dsc_passengermessage", length = 4000)
    private String dscPassengermessage;

    @Column(name = "dsc_generatedpassengermessage", length = Integer.MAX_VALUE)
    private String dscGeneratedpassengermessage;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "flg_passengermessageedited", nullable = false)
    private Boolean flgPassengermessageedited = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_interpretertype")
    private AlertInterpreterType sglInterpretertype;

    @Size(max = 255)
    @Column(name = "dsc_interpreterclassname")
    private String dscInterpreterclassname;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "flg_generatedbyllm", nullable = false)
    private Boolean flgGeneratedbyllm = false;

    @Size(max = 100)
    @Column(name = "dsc_llmprovider", length = 100)
    private String dscLlmprovider;

    @Size(max = 100)
    @Column(name = "dsc_llmmodel", length = 100)
    private String dscLlmmodel;

    @Size(max = 100)
    @Column(name = "cod_prompt", length = 100)
    private String codPrompt;

    @Size(max = 50)
    @Column(name = "dsc_promptversion", length = 50)
    private String dscPromptversion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_artifacttype")
    private AgentArtifactType sglArtifacttype;

    @Size(max = 255)
    @Column(name = "dsc_artifacthash")
    private String dscArtifacthash;

    @Column(name = "jsn_generationwarnings")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnGenerationwarnings;

    @Column(name = "jsn_context")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnContext;

    @Size(max = 100)
    @Column(name = "cod_operatoruser", length = 100)
    private String codOperatoruser;

    @Column(name = "dt_operatoractiontime")
    private OffsetDateTime dtOperatoractiontime;

    @Size(max = 1000)
    @Column(name = "dsc_operatornote", length = 1000)
    private String dscOperatornote;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_generatedat", nullable = false)
    private OffsetDateTime dtGeneratedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

}
