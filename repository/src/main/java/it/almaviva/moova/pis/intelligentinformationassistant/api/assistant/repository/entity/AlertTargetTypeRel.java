package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "alert_target_type_rel", schema = "pis_intelligentinformationassistant")
public class AlertTargetTypeRel {
    @EmbeddedId
    private AlertTargetTypeRelId id;

    @MapsId("codAlert")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_alert", nullable = false)
    private Alert codAlert;

    @MapsId("sglTargettype")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_targettype", nullable = false)
    private SuggestionTargetType sglTargettype;

}