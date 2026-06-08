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
@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "alert", schema = "pis_intelligentinformationassistant")
public class Alert {
    @Id
    @Size(max = 50)
    @CodGeneratedId("ALRT")
    @ColumnDefault("generate_cod_id('ALRT')")
    @Column(name = "cod_alert", nullable = false, length = 50)
    private String codAlert;

    @Size(max = 100)
    @Column(name = "cod_profile", length = 100)
    private String codProfile;

    @Size(max = 120)
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @Column(name = "dsc_name", nullable = false, length = 120)
    private String dscName;

    @Size(max = 1000)
    @Column(name = "dsc_description", length = 1000)
    private String dscDescription;

    @NotNull
    @jakarta.validation.constraints.NotBlank
    @Column(name = "dsc_prompt", nullable = false, length = Integer.MAX_VALUE)
    private String dscPrompt;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "num_version", nullable = false)
    @jakarta.validation.constraints.Min(1)
    private Integer numVersion = 1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'VERIFYING'")
    @JoinColumn(name = "sgl_status", nullable = false)
    private AlertStatus sglStatus;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "flg_enabled", nullable = false)
    private Boolean flgEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @ColumnDefault("'PENDING'")
    @JoinColumn(name = "sgl_verificationstatus")
    private AlertVerificationStatus sglVerificationstatus;

    @Column(name = "dsc_verificationsummary", length = Integer.MAX_VALUE)
    private String dscVerificationsummary;

    @Column(name = "dsc_rejectedreason", length = Integer.MAX_VALUE)
    private String dscRejectedreason;

    @Column(name = "num_verificationconfidence", precision = 5, scale = 4)
    @jakarta.validation.constraints.DecimalMin("0.0")
    @jakarta.validation.constraints.DecimalMax("1.0")
    private BigDecimal numVerificationconfidence;

    @Column(name = "dt_verifiedat")
    private OffsetDateTime dtVerifiedat;

    @Size(max = 100)
    @Column(name = "cod_prompt", length = 100)
    private String codPrompt;

    @Size(max = 50)
    @Column(name = "dsc_promptversion", length = 50)
    private String dscPromptversion;

    @Size(max = 100)
    @Column(name = "dsc_llmprovider", length = 100)
    private String dscLlmprovider;

    @Size(max = 100)
    @Column(name = "dsc_llmmodel", length = 100)
    private String dscLlmmodel;

    @Column(name = "jsn_verificationwarnings")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object jsnVerificationwarnings;

    @Column(name = "jsn_interpretedeventnames")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object jsnInterpretedeventnames;

    @Column(name = "jsn_safetychecks")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object jsnSafetychecks;

    @Column(name = "jsn_technicalspecification")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object jsnTechnicalspecification;

    @Column(name = "jsn_agentblueprintpreview")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object jsnAgentblueprintpreview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_interpretertype")
    private AlertInterpreterType sglInterpretertype;

    @Size(max = 255)
    @Column(name = "dsc_interpreterclassname")
    private String dscInterpreterclassname;

    @Size(max = 50)
    @Column(name = "dsc_contractversion", length = 50)
    private String dscContractversion;

    @Size(max = 255)
    @Column(name = "cod_coderef")
    private String codCoderef;

    @Column(name = "dsc_implementationsummary", length = Integer.MAX_VALUE)
    private String dscImplementationsummary;

    @Size(max = 255)
    @Column(name = "dsc_inputmodel")
    private String dscInputmodel;

    @Size(max = 255)
    @Column(name = "dsc_outputmodel")
    private String dscOutputmodel;

    @Column(name = "num_frequencyseconds")
    @jakarta.validation.constraints.Min(30)
    @jakarta.validation.constraints.Max(86400)
    private Integer numFrequencyseconds;

    @Column(name = "num_timewindowminutes")
    @jakarta.validation.constraints.Min(1)
    @jakarta.validation.constraints.Max(1440)
    private Integer numTimewindowminutes;

    @ColumnDefault("false")
    @Column(name = "flg_enabledonlyinservicehours")
    private Boolean flgEnabledonlyinservicehours;

    @Size(max = 255)
    @Column(name = "dsc_cronexpression")
    private String dscCronexpression;

    @ManyToOne(fetch = FetchType.LAZY)
    @ColumnDefault("'NOT_DEPLOYED'")
    @JoinColumn(name = "sgl_deploymentstatus")
    private AlertDeploymentStatus sglDeploymentstatus;

    @Column(name = "dt_lastexecutionat")
    private OffsetDateTime dtLastexecutionat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_lastexecutionstatus")
    private AlertExecutionStatus sglLastexecutionstatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_lastgeneratedsuggestion")
    private Suggestion codLastgeneratedsuggestion;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "num_executioncount", nullable = false)
    @jakarta.validation.constraints.Min(0)
    private Long numExecutioncount = 0L;

    @Column(name = "dsc_runtimeerrormessage", length = Integer.MAX_VALUE)
    private String dscRuntimeerrormessage;

    @Size(max = 100)
    @Column(name = "cod_createdby", length = 100)
    private String codCreatedby;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

    @Column(name = "dt_deletedat")
    private OffsetDateTime dtDeletedat;

    @ColumnDefault("false")
    @Column(name = "flg_technicalspecificationedited", nullable = false)
    private Boolean flgTechnicalspecificationedited = false;
}
