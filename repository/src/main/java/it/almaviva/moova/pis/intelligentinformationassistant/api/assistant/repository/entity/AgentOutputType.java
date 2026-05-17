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
@Table(name = "agent_output_type", schema = "pis_intelligentinformationassistant")
public class AgentOutputType {
    @Id
    @Size(max = 40)
    @Column(name = "sgl_outputtype", nullable = false, length = 40)
    private String sglOutputtype;

    @Size(max = 255)
    @NotNull
    @Column(name = "dsc_outputtype", nullable = false)
    private String dscOutputtype;

    @NotNull
    @Column(name = "num_sortorder", nullable = false)
    private Integer numSortorder;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "flg_active", nullable = false)
    private Boolean flgActive = true;

}