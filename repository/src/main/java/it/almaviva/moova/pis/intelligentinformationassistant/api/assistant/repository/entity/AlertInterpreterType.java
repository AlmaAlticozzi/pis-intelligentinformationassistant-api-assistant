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
@Table(name = "alert_interpreter_type", schema = "pis_intelligentinformationassistant")
public class AlertInterpreterType {
    @Id
    @Size(max = 40)
    @Column(name = "sgl_interpretertype", nullable = false, length = 40)
    private String sglInterpretertype;

    @Size(max = 255)
    @NotNull
    @Column(name = "dsc_interpretertype", nullable = false)
    private String dscInterpretertype;

    @NotNull
    @Column(name = "num_sortorder", nullable = false)
    private Integer numSortorder;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "flg_active", nullable = false)
    private Boolean flgActive = true;

}