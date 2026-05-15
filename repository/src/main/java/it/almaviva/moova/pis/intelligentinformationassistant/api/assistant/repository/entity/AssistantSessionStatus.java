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
@Table(name = "assistant_session_status", schema = "pis_intelligentinformationassistant")
public class AssistantSessionStatus {
    @Id
    @Size(max = 30)
    @Column(name = "sgl_status", nullable = false, length = 30)
    private String sglStatus;

    @Size(max = 255)
    @NotNull
    @Column(name = "dsc_status", nullable = false)
    private String dscStatus;

    @NotNull
    @Column(name = "num_sortorder", nullable = false)
    private Integer numSortorder;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "flg_active", nullable = false)
    private Boolean flgActive = true;

}