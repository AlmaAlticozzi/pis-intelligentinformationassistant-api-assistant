package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class AgentDefinitionAllowedToolId implements java.io.Serializable {
    private static final long serialVersionUID = 4789407591735696198L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_agentdefinition", nullable = false, length = 50)
    private String codAgentdefinition;

    @Size(max = 120)
    @NotNull
    @Column(name = "dsc_toolname", nullable = false, length = 120)
    private String dscToolname;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AgentDefinitionAllowedToolId entity = (AgentDefinitionAllowedToolId) o;
        return Objects.equals(this.dscToolname, entity.dscToolname) &&
                Objects.equals(this.codAgentdefinition, entity.codAgentdefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dscToolname, codAgentdefinition);
    }

}