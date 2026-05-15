package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "assistant_session", schema = "pis_intelligentinformationassistant")
public class AssistantSession {
    @Id
    @Size(max = 50)
    @CodGeneratedId("ASSS")
    @ColumnDefault("generate_cod_id('ASSS')")
    @Column(name = "cod_session", nullable = false, length = 50)
    private String codSession;

    @Size(max = 100)
    @Column(name = "cod_profile", length = 100)
    private String codProfile;

    @Size(max = 200)
    @Column(name = "dsc_title", length = 200)
    private String dscTitle;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("'ACTIVE'")
    @JoinColumn(name = "sgl_status", nullable = false)
    private AssistantSessionStatus sglStatus;

    @Size(max = 120)
    @Column(name = "dsc_lastintent", length = 120)
    private String dscLastintent;

    @Column(name = "jsn_lastentities")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnLastentities;

    @Column(name = "jsn_uicontext")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnUicontext;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

}
