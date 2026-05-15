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
@Table(name = "suggestion_target", schema = "pis_intelligentinformationassistant")
public class SuggestionTarget {
    @Id
    @Size(max = 50)
    @Column(name = "cod_suggestion", nullable = false, length = 50)
    private String codSuggestion;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_suggestion", nullable = false)
    private Suggestion suggestion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_targettype", nullable = false)
    private SuggestionTargetType sglTargettype;

    @Size(max = 255)
    @Column(name = "dsc_title")
    private String dscTitle;

    @Column(name = "dsc_description", length = Integer.MAX_VALUE)
    private String dscDescription;

    @Column(name = "dsc_criteria", length = Integer.MAX_VALUE)
    private String dscCriteria;

    @Column(name = "num_totalitems")
    @jakarta.validation.constraints.Min(0)
    private Integer numTotalitems;

    @Size(max = 255)
    @Column(name = "cod_servicedataref")
    private String codServicedataref;

    @Size(max = 255)
    @Column(name = "dsc_journeyname")
    private String dscJourneyname;

    @Size(max = 255)
    @Column(name = "cod_journeyref")
    private String codJourneyref;

    @Size(max = 255)
    @Column(name = "dsc_linename")
    private String dscLinename;

    @Size(max = 255)
    @Column(name = "dsc_originname")
    private String dscOriginname;

    @Size(max = 255)
    @Column(name = "dsc_destinationname")
    private String dscDestinationname;

    @Size(max = 255)
    @Column(name = "cod_stoppoint")
    private String codStoppoint;

    @Size(max = 255)
    @Column(name = "dsc_stoppointname")
    private String dscStoppointname;

    @Size(max = 100)
    @Column(name = "dsc_stoppointplatform", length = 100)
    private String dscStoppointplatform;

    @Column(name = "dt_planneddeparturetime")
    private OffsetDateTime dtPlanneddeparturetime;

    @Column(name = "dt_estimateddeparturetime")
    private OffsetDateTime dtEstimateddeparturetime;

    @Column(name = "int_delayminutes")
    private Integer intDelayminutes;

    @Column(name = "flg_cancelled")
    private Boolean flgCancelled;

    @Size(max = 100)
    @Column(name = "dsc_platform", length = 100)
    private String dscPlatform;

    @Size(max = 255)
    @Column(name = "cod_audiomessageref")
    private String codAudiomessageref;

    @Size(max = 255)
    @Column(name = "dsc_audiomessagename")
    private String dscAudiomessagename;

    @Column(name = "dsc_messagetext", length = Integer.MAX_VALUE)
    private String dscMessagetext;

    @Size(max = 30)
    @Column(name = "dsc_language", length = 30)
    private String dscLanguage;

    @Size(max = 100)
    @Column(name = "dsc_channel", length = 100)
    private String dscChannel;

    @Size(max = 100)
    @Column(name = "dsc_audiostatus", length = 100)
    private String dscAudiostatus;

    @Column(name = "dt_audiogeneratedat")
    private OffsetDateTime dtAudiogeneratedat;

    @Column(name = "dt_broadcastat")
    private OffsetDateTime dtBroadcastat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sgl_genericsuggestionseverity")
    private SuggestionSeverity sglGenericsuggestionseverity;

    @NotNull
    @ColumnDefault("'{}'")
    @Column(name = "jsn_targetdetail", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnTargetdetail = new java.util.HashMap<>();

}
