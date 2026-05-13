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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "suggestion")
public class Suggestion {
    @Id
    @Column(name = "cod_id", nullable = false)
    private String id;

    @Size(max = 100)
    @NotNull
    @Column(name = "sgl_sourcesystem", nullable = false, length = 100)
    private String sglSourcesystem;

    @Size(max = 255)
    @Column(name = "dsc_sourcetopic")
    private String dscSourcetopic;

    @Column(name = "int_sourcepartition")
    private Integer intSourcepartition;

    @Column(name = "num_sourceoffset")
    private Long numSourceoffset;

    @Size(max = 255)
    @Column(name = "cod_sourceevent")
    private String codSourceevent;

    @Size(max = 100)
    @NotNull
    @Column(name = "sgl_sourceeventtype", nullable = false, length = 100)
    private String sglSourceeventtype;

    @Column(name = "dt_sourceeventgenerationtime")
    private OffsetDateTime dtSourceeventgenerationtime;

    @Column(name = "dt_sourcemessagetime")
    private OffsetDateTime dtSourcemessagetime;

    @Size(max = 100)
    @Column(name = "cod_infomobilitydataframe", length = 100)
    private String codInfomobilitydataframe;

    @Size(max = 100)
    @Column(name = "cod_infomobilityvehiclejourney", length = 100)
    private String codInfomobilityvehiclejourney;

    @Size(max = 255)
    @Column(name = "dsc_vehiclejourneyname")
    private String dscVehiclejourneyname;

    @Size(max = 100)
    @Column(name = "cod_line", length = 100)
    private String codLine;

    @Size(max = 255)
    @Column(name = "dsc_linename")
    private String dscLinename;

    @Size(max = 100)
    @Column(name = "cod_stoppoint", length = 100)
    private String codStoppoint;

    @Size(max = 255)
    @Column(name = "dsc_stoppointname")
    private String dscStoppointname;

    @Size(max = 100)
    @Column(name = "cod_origin", length = 100)
    private String codOrigin;

    @Size(max = 255)
    @Column(name = "dsc_originname")
    private String dscOriginname;

    @Size(max = 100)
    @Column(name = "cod_destination", length = 100)
    private String codDestination;

    @Size(max = 255)
    @Column(name = "dsc_destinationname")
    private String dscDestinationname;

    @Column(name = "dt_timetableddeparturetime")
    private OffsetDateTime dtTimetableddeparturetime;

    @Column(name = "dt_estimateddeparturetime")
    private OffsetDateTime dtEstimateddeparturetime;

    @Column(name = "int_departuredelayminutes")
    private Integer intDeparturedelayminutes;

    @Size(max = 100)
    @Column(name = "dsc_platform", length = 100)
    private String dscPlatform;

    @Size(max = 255)
    @NotNull
    @Column(name = "dsc_title", nullable = false)
    private String dscTitle;

    @Column(name = "dsc_description", length = Integer.MAX_VALUE)
    private String dscDescription;

    @Column(name = "dsc_suggestedmessage", length = Integer.MAX_VALUE)
    private String dscSuggestedmessage;

    @Column(name = "dsc_editedmessage", length = Integer.MAX_VALUE)
    private String dscEditedmessage;

    @Column(name = "dsc_finalmessage", length = Integer.MAX_VALUE)
    private String dscFinalmessage;

    @Column(name = "dsc_reason", length = Integer.MAX_VALUE)
    private String dscReason;

    @Column(name = "num_confidence", precision = 5, scale = 4)
    private BigDecimal numConfidence;

    @NotNull
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

    @Column(name = "jsn_context")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnContext;

    @Column(name = "jsn_uicontext")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnUicontext;

    @NotNull
    @Column(name = "flg_requiresoperatorapproval", nullable = false)
    private Boolean flgRequiresoperatorapproval = false;

    @Column(name = "dsc_operatornote", length = Integer.MAX_VALUE)
    private String dscOperatornote;

    @Size(max = 100)
    @Column(name = "cod_operatoruser", length = 100)
    private String codOperatoruser;
    @NotNull
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

    @Column(name = "dt_operatoractiontime")
    private OffsetDateTime dtOperatoractiontime;
    @NotNull
    @Column(name = "dt_updatedat", nullable = false)
    private OffsetDateTime dtUpdatedat;

    @Column(name = "sgl_type", columnDefinition = "suggestion_type not null")
    private String sglType;

    @Column(name = "sgl_status", columnDefinition = "suggestion_status not null")
    private String sglStatus;

    @Column(name = "sgl_severity", columnDefinition = "suggestion_severity not null")
    private String sglSeverity;

}