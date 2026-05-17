package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_definition", schema = "pis_intelligentinformationassistant")
public class AgentDefinition {
    @Id
    @Size(max = 50)
    @CodGeneratedId("AGDF")
    @ColumnDefault("generate_cod_id('AGDF')")
    @Column(name = "cod_agentdefinition", nullable = false, length = 50)
    private String codAgentdefinition;

    @Size(max = 100)
    @Column(name = "cod_profile", length = 100)
    private String codProfile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_alert", nullable = false)
    private Alert codAlert;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "num_alertversion", nullable = false)
    @jakarta.validation.constraints.Min(1)
    private Integer numAlertversion = 1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_agentprofile", nullable = false)
    private AgentProfile codAgentprofile;

    @Size(max = 120)
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @Column(name = "dsc_name", nullable = false, length = 120)
    private String dscName;

    @Size(max = 1000)
    @Column(name = "dsc_description", length = 1000)
    private String dscDescription;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'DRAFT'")
    @JoinColumn(name = "sgl_status", nullable = false)
    private AgentDefinitionStatus sglStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'AUTO'")
    @JoinColumn(name = "sgl_generationmode", nullable = false)
    private AgentGenerationMode sglGenerationmode;

    @Column(name = "jsn_blueprint")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnBlueprint;

    @Column(name = "jsn_dslpreview")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnDslpreview;

    @Column(name = "jsn_validationplan")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnValidationplan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_complexity")
    private AgentComplexity sglComplexity;

    @NotNull
    @ColumnDefault("'[]'")
    @Column(name = "jsn_requiredsources", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> jsnRequiredsources = new ArrayList<>();

    @NotNull
    @ColumnDefault("'[]'")
    @Column(name = "jsn_requiredpermissions", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> jsnRequiredpermissions = new ArrayList<>();

    @NotNull
    @ColumnDefault("'[]'")
    @Column(name = "jsn_generationwarnings", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> jsnGenerationwarnings = new ArrayList<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_activationtype", nullable = false)
    private AgentActivationType sglActivationtype;

    @Size(max = 100)
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @Column(name = "dsc_timezone", nullable = false, length = 100)
    private String dscTimezone;

    @Column(name = "dt_validfrom")
    private OffsetDateTime dtValidfrom;

    @Column(name = "dt_validto")
    private OffsetDateTime dtValidto;

    @Column(name = "d_validfromdate")
    private LocalDate dValidfromdate;

    @Column(name = "d_validtodate")
    private LocalDate dValidtodate;

    @Column(name = "t_dailystarttime")
    private LocalTime tDailystarttime;

    @Column(name = "t_dailyendtime")
    private LocalTime tDailyendtime;

    @NotNull
    @ColumnDefault("'{}'")
    @Column(name = "jsn_activationpolicy", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnActivationpolicy = new HashMap<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'NONE'")
    @JoinColumn(name = "sgl_artifacttype", nullable = false)
    private AgentArtifactType sglArtifacttype;

    @Column(name = "dsc_artifacturi", length = Integer.MAX_VALUE)
    private String dscArtifacturi;

    @Size(max = 255)
    @Column(name = "dsc_artifacthash")
    private String dscArtifacthash;

    @ManyToOne(fetch = FetchType.LAZY)
    @ColumnDefault("'NOT_SIGNED'")
    @JoinColumn(name = "sgl_signaturestatus")
    private AgentArtifactSignatureStatus sglSignaturestatus;

    @Size(max = 255)
    @Column(name = "dsc_runtimeimage")
    private String dscRuntimeimage;

    @Size(max = 50)
    @Column(name = "dsc_sdkversion", length = 50)
    private String dscSdkversion;

    @Column(name = "dsc_implementationsummary", length = Integer.MAX_VALUE)
    private String dscImplementationsummary;

    @Size(max = 255)
    @Column(name = "dsc_inputmodel")
    private String dscInputmodel;

    @Size(max = 255)
    @Column(name = "dsc_outputmodel")
    private String dscOutputmodel;

    @NotNull
    @ColumnDefault("'[]'")
    @Column(name = "jsn_allowedtools", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> jsnAllowedtools = new ArrayList<>();

    @Size(max = 120)
    @ColumnDefault("'TOOL_GATEWAY_ONLY'")
    @Column(name = "dsc_networkpolicy", length = 120)
    private String dscNetworkpolicy = "TOOL_GATEWAY_ONLY";

    @NotNull
    @ColumnDefault("'{}'")
    @Column(name = "jsn_runtimecontract", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnRuntimecontract = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_latestcompilation")
    private AgentCompilation codLatestcompilation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_latestcompilationstatus")
    private AgentCompilationStatus sglLatestcompilationstatus;

    @Size(max = 120)
    @Column(name = "dsc_latestcompilationstep", length = 120)
    private String dscLatestcompilationstep;

    @Column(name = "dt_latestcompilationcompletedat")
    private OffsetDateTime dtLatestcompilationcompletedat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_latestrun")
    private AgentRun codLatestrun;

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

    @Column(name = "dt_archivedat")
    private OffsetDateTime dtArchivedat;

}
