package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "alert_required_tool", schema = "pis_intelligentinformationassistant")
public class AlertRequiredTool {
    @EmbeddedId
    private AlertRequiredToolId id;

    @MapsId("codAlert")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_alert", nullable = false)
    private Alert codAlert;

    @Column(name = "jsn_operations")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object jsnOperations;

}
