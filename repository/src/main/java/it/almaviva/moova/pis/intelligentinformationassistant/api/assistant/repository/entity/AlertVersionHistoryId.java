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
public class AlertVersionHistoryId implements java.io.Serializable {
    private static final long serialVersionUID = 1654285929066642369L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_alert", nullable = false, length = 50)
    private String codAlert;

    @NotNull
    @Column(name = "num_version", nullable = false)
    private Integer numVersion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AlertVersionHistoryId entity = (AlertVersionHistoryId) o;
        return Objects.equals(this.numVersion, entity.numVersion) &&
                Objects.equals(this.codAlert, entity.codAlert);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numVersion, codAlert);
    }

}