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
import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "alert_version_history", schema = "pis_intelligentinformationassistant")
public class AlertVersionHistory {
    @EmbeddedId
    private AlertVersionHistoryId id;

    @MapsId("codAlert")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_alert", nullable = false)
    private Alert codAlert;

    @Size(max = 120)
    @NotNull
    @Column(name = "dsc_name", nullable = false, length = 120)
    private String dscName;

    @Size(max = 1000)
    @Column(name = "dsc_description", length = 1000)
    private String dscDescription;

    @NotNull
    @Column(name = "dsc_prompt", nullable = false, length = Integer.MAX_VALUE)
    private String dscPrompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_verificationstatus")
    private AlertVerificationStatus sglVerificationstatus;

    @Column(name = "jsn_technicalspecification")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnTechnicalspecification;

    @Column(name = "jsn_agentblueprintpreview")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnAgentblueprintpreview;

    @Size(max = 100)
    @Column(name = "cod_createdby", length = 100)
    private String codCreatedby;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

}