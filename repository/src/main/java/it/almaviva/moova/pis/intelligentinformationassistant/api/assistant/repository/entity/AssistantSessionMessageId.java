package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class AssistantSessionMessageId implements java.io.Serializable {
    private static final long serialVersionUID = -7379568403727878625L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_session", nullable = false, length = 50)
    private String codSession;

    @Size(max = 50)
    @NotNull
    @CodGeneratedId("MSG")
    @ColumnDefault("generate_cod_id('MSG')")
    @Column(name = "cod_message", nullable = false, length = 50)
    private String codMessage;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AssistantSessionMessageId entity = (AssistantSessionMessageId) o;
        return Objects.equals(this.codMessage, entity.codMessage) &&
                Objects.equals(this.codSession, entity.codSession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codMessage, codSession);
    }

}