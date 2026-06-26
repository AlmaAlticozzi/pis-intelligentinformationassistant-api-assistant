package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Immutable
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_runtime_package", schema = "pis_intelligentinformationassistant")
public class AgentRuntimePackage {

    @Id
    @Size(max = 50)
    @CodGeneratedId("RTPK")
    @ColumnDefault("generate_cod_id('RTPK')")
    @Column(name = "cod_runtimepackage", nullable = false, length = 50)
    private String codRuntimepackage;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_agentdefinition", nullable = false)
    private AgentDefinition codAgentdefinition;

    @NotNull
    @Column(name = "num_packageversion", nullable = false)
    private Long numPackageversion;

    @Size(max = 100)
    @NotNull
    @Column(name = "cod_submissionid", nullable = false, length = 100)
    private String codSubmissionid;

    @Size(max = 64)
    @NotNull
    @Column(name = "dsc_packagefingerprint", nullable = false, length = 64)
    private String dscPackagefingerprint;

    @Size(max = 255)
    @NotNull
    @Column(name = "dsc_artifacthash", nullable = false)
    private String dscArtifacthash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_agentcompilation")
    private AgentCompilation codAgentcompilation;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'iia.runtime-agent-submission/v1'")
    @Column(name = "dsc_packageschemaversion", nullable = false, length = 50)
    private String dscPackageschemaversion;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'RFC8785_JSON'")
    @Column(name = "dsc_canonicalization", nullable = false, length = 50)
    private String dscCanonicalization;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'SHA-256'")
    @Column(name = "dsc_hashalgorithm", nullable = false, length = 20)
    private String dscHashalgorithm;

    @NotNull
    @Column(name = "jsn_runtimepackage", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnRuntimepackage;

    @NotNull
    @Column(name = "dt_sourceupdatedat", nullable = false)
    private OffsetDateTime dtSourceupdatedat;

    @NotNull
    @Column(name = "dt_submittedat", nullable = false)
    private Instant dtSubmittedat;

    @Size(max = 100)
    @Column(name = "cod_createdby", length = 100)
    private String codCreatedby;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

    protected AgentRuntimePackage() {
    }

    public static AgentRuntimePackage create(
            AgentDefinition agentDefinition,
            long packageVersion,
            String submissionId,
            String packageFingerprint,
            String artifactHash,
            AgentCompilation agentCompilation,
            String packageSchemaVersion,
            String canonicalization,
            String hashAlgorithm,
            Map<String, Object> runtimePackage,
            OffsetDateTime sourceUpdatedAt,
            Instant submittedAt,
            String createdBy) {
        AgentRuntimePackage entity = new AgentRuntimePackage();
        entity.codAgentdefinition = agentDefinition;
        entity.numPackageversion = packageVersion;
        entity.codSubmissionid = submissionId;
        entity.dscPackagefingerprint = packageFingerprint;
        entity.dscArtifacthash = artifactHash;
        entity.codAgentcompilation = agentCompilation;
        entity.dscPackageschemaversion = packageSchemaVersion;
        entity.dscCanonicalization = canonicalization;
        entity.dscHashalgorithm = hashAlgorithm;
        entity.jsnRuntimepackage = runtimePackage == null ? null : new LinkedHashMap<>(runtimePackage);
        entity.dtSourceupdatedat = sourceUpdatedAt;
        entity.dtSubmittedat = submittedAt;
        entity.codCreatedby = createdBy;
        return entity;
    }

    public String getCodRuntimepackage() {
        return codRuntimepackage;
    }

    public AgentDefinition getCodAgentdefinition() {
        return codAgentdefinition;
    }

    public Long getNumPackageversion() {
        return numPackageversion;
    }

    public String getCodSubmissionid() {
        return codSubmissionid;
    }

    public String getDscPackagefingerprint() {
        return dscPackagefingerprint;
    }

    public String getDscArtifacthash() {
        return dscArtifacthash;
    }

    public AgentCompilation getCodAgentcompilation() {
        return codAgentcompilation;
    }

    public String getDscPackageschemaversion() {
        return dscPackageschemaversion;
    }

    public String getDscCanonicalization() {
        return dscCanonicalization;
    }

    public String getDscHashalgorithm() {
        return dscHashalgorithm;
    }

    public Map<String, Object> getJsnRuntimepackage() {
        return jsnRuntimepackage;
    }

    public OffsetDateTime getDtSourceupdatedat() {
        return dtSourceupdatedat;
    }

    public Instant getDtSubmittedat() {
        return dtSubmittedat;
    }

    public String getCodCreatedby() {
        return codCreatedby;
    }

    public OffsetDateTime getDtCreatedat() {
        return dtCreatedat;
    }
}
