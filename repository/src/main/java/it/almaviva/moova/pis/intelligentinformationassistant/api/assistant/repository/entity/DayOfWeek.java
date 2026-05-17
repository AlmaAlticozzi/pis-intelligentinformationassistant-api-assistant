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
@Table(name = "day_of_week", schema = "pis_intelligentinformationassistant")
public class DayOfWeek {
    @Id
    @Size(max = 20)
    @Column(name = "sgl_dayofweek", nullable = false, length = 20)
    private String sglDayofweek;

    @Size(max = 255)
    @NotNull
    @Column(name = "dsc_dayofweek", nullable = false)
    private String dscDayofweek;

    @NotNull
    @Column(name = "num_sortorder", nullable = false)
    private Integer numSortorder;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "flg_active", nullable = false)
    private Boolean flgActive = true;

}