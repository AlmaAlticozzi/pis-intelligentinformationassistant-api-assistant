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
@Table(name = "assistant_question", schema = "pis_intelligentinformationassistant")
public class AssistantQuestion {
    @Id
    @Size(max = 50)
    @CodGeneratedId("QSTN")
    @ColumnDefault("generate_cod_id('QSTN')")
    @Column(name = "cod_question", nullable = false, length = 50)
    private String codQuestion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_session", nullable = false)
    private AssistantSession codSession;

    @Size(max = 100)
    @Column(name = "cod_profile", length = 100)
    private String codProfile;

    @Size(max = 2000)
    @NotNull
    @Column(name = "dsc_question", nullable = false, length = 2000)
    private String dscQuestion;

    @Size(max = 30)
    @Column(name = "dsc_preferredlanguage", length = 30)
    private String dscPreferredlanguage;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "flg_includeitems", nullable = false)
    private Boolean flgIncludeitems = true;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "flg_includetoolexecutions", nullable = false)
    private Boolean flgIncludetoolexecutions = false;

    @Column(name = "jsn_uicontext")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnUicontext;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_status")
    private AssistantQuestionStatus sglStatus;

    @Size(max = 120)
    @Column(name = "dsc_detectedintent", length = 120)
    private String dscDetectedintent;

    @Column(name = "dsc_answer", length = Integer.MAX_VALUE)
    private String dscAnswer;

    @Column(name = "jsn_entities")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnEntities;

    @Column(name = "jsn_clarification")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnClarification;

    @Column(name = "jsn_warnings")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnWarnings;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

    @Column(name = "dt_answeredat")
    private OffsetDateTime dtAnsweredat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

}
