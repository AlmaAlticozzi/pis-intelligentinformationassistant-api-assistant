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
@Table(name = "tool_execution", schema = "pis_intelligentinformationassistant")
public class ToolExecution {
    @Id
    @Size(max = 50)
    @CodGeneratedId("TEXE")
    @ColumnDefault("generate_cod_id('TEXE')")
    @Column(name = "cod_toolexecution", nullable = false, length = 50)
    private String codToolexecution;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_question", nullable = false)
    private AssistantQuestion codQuestion;

    @NotNull
    @Column(name = "num_executionorder", nullable = false)
    @jakarta.validation.constraints.Min(0)
    private Integer numExecutionorder;

    @Size(max = 120)
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @Column(name = "dsc_toolname", nullable = false, length = 120)
    private String dscToolname;

    @Size(max = 120)
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @Column(name = "dsc_operationname", nullable = false, length = 120)
    private String dscOperationname;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_status", nullable = false)
    private ToolExecutionStatus sglStatus;

    @Column(name = "num_durationms")
    @jakarta.validation.constraints.Min(0)
    private Integer numDurationms;

    @Column(name = "jsn_input")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnInput;

    @Column(name = "jsn_output")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnOutput;

    @Column(name = "dsc_errormessage", length = Integer.MAX_VALUE)
    private String dscErrormessage;

    @Column(name = "dt_startedat")
    private OffsetDateTime dtStartedat;

    @Column(name = "dt_endedat")
    private OffsetDateTime dtEndedat;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

}
