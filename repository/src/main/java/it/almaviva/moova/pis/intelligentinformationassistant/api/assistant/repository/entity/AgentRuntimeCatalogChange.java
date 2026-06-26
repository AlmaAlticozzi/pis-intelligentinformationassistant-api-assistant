package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Immutable
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_runtime_catalog_change", schema = "pis_intelligentinformationassistant")
public class AgentRuntimeCatalogChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "num_changesequence", nullable = false)
    private Long numChangesequence;

    @Size(max = 50)
    @NotNull
    @CodGeneratedId("RTCH")
    @ColumnDefault("generate_cod_id('RTCH')")
    @Column(name = "cod_catalogchange", nullable = false, length = 50)
    private String codCatalogchange;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_agentdefinition", nullable = false)
    private AgentDefinition codAgentdefinition;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sgl_action", nullable = false, length = 20)
    private RuntimeCatalogAction sglAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_runtimepackage")
    private AgentRuntimePackage codRuntimepackage;

    @Column(name = "num_packageversion")
    private Long numPackageversion;

    @Size(max = 64)
    @Column(name = "dsc_packagefingerprint", length = 64)
    private String dscPackagefingerprint;

    @Size(max = 30)
    @Column(name = "sgl_sourceagentstatus", length = 30)
    private String sglSourceagentstatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "sgl_removalreason", length = 30)
    private RuntimeCatalogRemovalReason sglRemovalreason;

    @Size(max = 255)
    @NotNull
    @Column(name = "dsc_deduplicationkey", nullable = false)
    private String dscDeduplicationkey;

    @NotNull
    @Column(name = "dt_sourceupdatedat", nullable = false)
    private OffsetDateTime dtSourceupdatedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_effectiveat", nullable = false)
    private OffsetDateTime dtEffectiveat;

    @NotNull
    @ColumnDefault("'{}'")
    @Column(name = "jsn_details", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnDetails;

    protected AgentRuntimeCatalogChange() {
    }

    public static AgentRuntimeCatalogChange upsert(
            AgentDefinition agentDefinition,
            AgentRuntimePackage runtimePackage,
            String deduplicationKey,
            OffsetDateTime sourceUpdatedAt,
            OffsetDateTime effectiveAt,
            Map<String, Object> details) {
        AgentRuntimeCatalogChange entity = new AgentRuntimeCatalogChange();
        entity.codAgentdefinition = agentDefinition;
        entity.sglAction = RuntimeCatalogAction.UPSERT;
        entity.codRuntimepackage = runtimePackage;
        entity.numPackageversion = runtimePackage.getNumPackageversion();
        entity.dscPackagefingerprint = runtimePackage.getDscPackagefingerprint();
        entity.sglSourceagentstatus = "ACTIVE";
        entity.dscDeduplicationkey = deduplicationKey;
        entity.dtSourceupdatedat = sourceUpdatedAt;
        entity.dtEffectiveat = effectiveAt;
        entity.jsnDetails = details == null ? new LinkedHashMap<>() : new LinkedHashMap<>(details);
        return entity;
    }

    public static AgentRuntimeCatalogChange remove(
            AgentDefinition agentDefinition,
            String sourceAgentStatus,
            RuntimeCatalogRemovalReason removalReason,
            String deduplicationKey,
            OffsetDateTime sourceUpdatedAt,
            OffsetDateTime effectiveAt,
            Map<String, Object> details) {
        AgentRuntimeCatalogChange entity = new AgentRuntimeCatalogChange();
        entity.codAgentdefinition = agentDefinition;
        entity.sglAction = RuntimeCatalogAction.REMOVE;
        entity.sglSourceagentstatus = sourceAgentStatus;
        entity.sglRemovalreason = removalReason;
        entity.dscDeduplicationkey = deduplicationKey;
        entity.dtSourceupdatedat = sourceUpdatedAt;
        entity.dtEffectiveat = effectiveAt;
        entity.jsnDetails = details == null ? new LinkedHashMap<>() : new LinkedHashMap<>(details);
        return entity;
    }

    public Long getNumChangesequence() {
        return numChangesequence;
    }

    public String getCodCatalogchange() {
        return codCatalogchange;
    }

    public AgentDefinition getCodAgentdefinition() {
        return codAgentdefinition;
    }

    public RuntimeCatalogAction getSglAction() {
        return sglAction;
    }

    public AgentRuntimePackage getCodRuntimepackage() {
        return codRuntimepackage;
    }

    public Long getNumPackageversion() {
        return numPackageversion;
    }

    public String getDscPackagefingerprint() {
        return dscPackagefingerprint;
    }

    public String getSglSourceagentstatus() {
        return sglSourceagentstatus;
    }

    public RuntimeCatalogRemovalReason getSglRemovalreason() {
        return sglRemovalreason;
    }

    public String getDscDeduplicationkey() {
        return dscDeduplicationkey;
    }

    public OffsetDateTime getDtSourceupdatedat() {
        return dtSourceupdatedat;
    }

    public OffsetDateTime getDtEffectiveat() {
        return dtEffectiveat;
    }

    public Map<String, Object> getJsnDetails() {
        return jsnDetails;
    }
}
