package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "alert_execution_status", schema = "pis_intelligentinformationassistant")
public class AlertExecutionStatus {
    @Id
    @Size(max = 30)
    @Column(name = "sgl_executionstatus", nullable = false, length = 30)
    private String sglExecutionstatus;

    @Size(max = 255)
    @NotNull
    @Column(name = "dsc_executionstatus", nullable = false)
    private String dscExecutionstatus;

    @NotNull
    @Column(name = "num_sortorder", nullable = false)
    private Integer numSortorder;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "flg_active", nullable = false)
    private Boolean flgActive = true;

}