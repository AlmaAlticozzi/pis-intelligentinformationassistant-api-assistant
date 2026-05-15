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
public class AssistantAnswerItemId implements java.io.Serializable {
    private static final long serialVersionUID = -2282253473328332957L;
    @Size(max = 50)
    @NotNull
    @Column(name = "cod_question", nullable = false, length = 50)
    private String codQuestion;

    @NotNull
    @Column(name = "num_itemposition", nullable = false)
    @jakarta.validation.constraints.Min(0)
    private Integer numItemposition;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AssistantAnswerItemId entity = (AssistantAnswerItemId) o;
        return Objects.equals(this.numItemposition, entity.numItemposition) &&
                Objects.equals(this.codQuestion, entity.codQuestion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numItemposition, codQuestion);
    }

}
