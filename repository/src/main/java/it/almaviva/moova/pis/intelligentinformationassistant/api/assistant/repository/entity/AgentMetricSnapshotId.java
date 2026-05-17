package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.OffsetDateTime;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class AgentMetricSnapshotId implements java.io.Serializable {
    private static final long serialVersionUID = -910755421092023112L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_agentrun", nullable = false, length = 50)
    private String codAgentrun;

    @NotNull
    @Column(name = "dt_sampledat", nullable = false)
    private OffsetDateTime dtSampledat;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AgentMetricSnapshotId entity = (AgentMetricSnapshotId) o;
        return Objects.equals(this.dtSampledat, entity.dtSampledat) &&
                Objects.equals(this.codAgentrun, entity.codAgentrun);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dtSampledat, codAgentrun);
    }

}