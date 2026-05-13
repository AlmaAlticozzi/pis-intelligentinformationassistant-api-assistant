package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

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
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "operator_question")
public class OperatorQuestion {
    @Id
    @Column(name = "cod_id", nullable = false)
    private String id;

    @Size(max = 100)
    @Column(name = "cod_operatoruser", length = 100)
    private String codOperatoruser;

    @Size(max = 255)
    @Column(name = "dsc_operatordisplayname")
    private String dscOperatordisplayname;

    @NotNull
    @Column(name = "dsc_questiontext", nullable = false, length = Integer.MAX_VALUE)
    private String dscQuestiontext;

    @Size(max = 100)
    @Column(name = "slg_detectedintent", length = 100)
    private String slgDetectedintent;

    @Column(name = "jsn_resolvedentities")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnResolvedentities;

    @Column(name = "jsn_usedtools")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnUsedtools;

    @Column(name = "jsn_toolresultssummary")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnToolresultssummary;

    @Column(name = "dsc_answertext", length = Integer.MAX_VALUE)
    private String dscAnswertext;

    @Column(name = "jsn_answeritems")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnAnsweritems;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "flg_interpretedbyllm", nullable = false)
    private Boolean flgInterpretedbyllm = false;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "flg_answeredbyllm", nullable = false)
    private Boolean flgAnsweredbyllm = false;

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

    @Column(name = "jsn_rawcontext")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnRawcontext;

    @Column(name = "dsc_errormessage", length = Integer.MAX_VALUE)
    private String dscErrormessage;

    @NotNull
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

    @Column(name = "dt_answeredat")
    private OffsetDateTime dtAnsweredat;
    @NotNull
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

}