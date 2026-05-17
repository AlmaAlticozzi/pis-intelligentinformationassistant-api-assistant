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
public class AgentDefinitionDayOfWeekId implements java.io.Serializable {
    private static final long serialVersionUID = 5588169124800979050L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_agentdefinition", nullable = false, length = 50)
    private String codAgentdefinition;

    @Size(max = 20)
    @NotNull
    @Column(name = "sgl_dayofweek", nullable = false, length = 20)
    private String sglDayofweek;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AgentDefinitionDayOfWeekId entity = (AgentDefinitionDayOfWeekId) o;
        return Objects.equals(this.sglDayofweek, entity.sglDayofweek) &&
                Objects.equals(this.codAgentdefinition, entity.codAgentdefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sglDayofweek, codAgentdefinition);
    }

}