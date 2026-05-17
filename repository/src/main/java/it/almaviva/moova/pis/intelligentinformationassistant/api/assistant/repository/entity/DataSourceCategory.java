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
@Table(name = "data_source_category", schema = "pis_intelligentinformationassistant")
public class DataSourceCategory {
    @Id
    @Size(max = 60)
    @Column(name = "sgl_category", nullable = false, length = 60)
    private String sglCategory;

    @Size(max = 255)
    @NotNull
    @Column(name = "dsc_category", nullable = false)
    private String dscCategory;

    @NotNull
    @Column(name = "num_sortorder", nullable = false)
    private Integer numSortorder;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "flg_active", nullable = false)
    private Boolean flgActive = true;

}