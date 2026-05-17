package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "agent_definition_required_source", schema = "pis_intelligentinformationassistant")
public class AgentDefinitionRequiredSource {
    @EmbeddedId
    private AgentDefinitionRequiredSourceId id;

    @MapsId("codAgentdefinition")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_agentdefinition", nullable = false)
    private AgentDefinition codAgentdefinition;

    @MapsId("sglCategory")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_category", nullable = false)
    private DataSourceCategory sglCategory;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "flg_required", nullable = false)
    private Boolean flgRequired = true;

    @Column(name = "dsc_description", length = Integer.MAX_VALUE)
    private String dscDescription;

}