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
public class AgentDefinitionRequiredSourceId implements java.io.Serializable {
    private static final long serialVersionUID = 8132581317228488819L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_agentdefinition", nullable = false, length = 50)
    private String codAgentdefinition;

    @Size(max = 60)
    @NotNull
    @Column(name = "sgl_category", nullable = false, length = 60)
    private String sglCategory;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AgentDefinitionRequiredSourceId entity = (AgentDefinitionRequiredSourceId) o;
        return Objects.equals(this.sglCategory, entity.sglCategory) &&
                Objects.equals(this.codAgentdefinition, entity.codAgentdefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sglCategory, codAgentdefinition);
    }

}