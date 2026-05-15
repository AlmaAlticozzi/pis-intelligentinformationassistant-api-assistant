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
public class AlertRequiredToolId implements java.io.Serializable {
    private static final long serialVersionUID = -9135664686319912261L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_alert", nullable = false, length = 50)
    private String codAlert;

    @Size(max = 120)
    @NotNull
    @Column(name = "dsc_toolname", nullable = false, length = 120)
    private String dscToolname;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AlertRequiredToolId entity = (AlertRequiredToolId) o;
        return Objects.equals(this.codAlert, entity.codAlert) &&
                Objects.equals(this.dscToolname, entity.dscToolname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codAlert, dscToolname);
    }

}