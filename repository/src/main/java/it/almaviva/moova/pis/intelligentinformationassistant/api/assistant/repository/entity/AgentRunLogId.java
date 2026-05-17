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
public class AgentRunLogId implements java.io.Serializable {
    private static final long serialVersionUID = -1082604066036023922L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_agentrun", nullable = false, length = 50)
    private String codAgentrun;

    @NotNull
    @Column(name = "num_logsequence", nullable = false)
    private Long numLogsequence;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AgentRunLogId entity = (AgentRunLogId) o;
        return Objects.equals(this.numLogsequence, entity.numLogsequence) &&
                Objects.equals(this.codAgentrun, entity.codAgentrun);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numLogsequence, codAgentrun);
    }

}