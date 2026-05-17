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
public class AgentCompilationStepId implements java.io.Serializable {
    private static final long serialVersionUID = 1797027178953907265L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_agentcompilation", nullable = false, length = 50)
    private String codAgentcompilation;

    @NotNull
    @Column(name = "num_steporder", nullable = false)
    private Integer numSteporder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AgentCompilationStepId entity = (AgentCompilationStepId) o;
        return Objects.equals(this.numSteporder, entity.numSteporder) &&
                Objects.equals(this.codAgentcompilation, entity.codAgentcompilation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numSteporder, codAgentcompilation);
    }

}