package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "assistant_answer_item", schema = "pis_intelligentinformationassistant")
public class AssistantAnswerItem {
    @EmbeddedId
    private AssistantAnswerItemId id;

    @MapsId("codQuestion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_question", nullable = false)
    private AssistantQuestion codQuestion;

    @Size(max = 100)
    @Column(name = "dsc_type", length = 100)
    private String dscType;

    @Size(max = 255)
    @Column(name = "dsc_title")
    private String dscTitle;

    @Size(max = 255)
    @Column(name = "dsc_subtitle")
    private String dscSubtitle;

    @Column(name = "jsn_journey")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnJourney;

    @Column(name = "jsn_audiomessage")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnAudiomessage;

    @Column(name = "jsn_metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnMetadata;

}