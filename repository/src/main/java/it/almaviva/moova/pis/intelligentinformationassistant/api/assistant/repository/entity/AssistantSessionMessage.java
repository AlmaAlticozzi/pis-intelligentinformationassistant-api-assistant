package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@org.hibernate.annotations.DynamicInsert
@Entity
@Table(name = "assistant_session_message", schema = "pis_intelligentinformationassistant")
public class AssistantSessionMessage {
    @EmbeddedId
    private AssistantSessionMessageId id;

    @MapsId("codSession")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cod_session", nullable = false)
    private AssistantSession codSession;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sgl_role", nullable = false)
    private AssistantMessageRole sglRole;

    @NotNull
    @Column(name = "dsc_content", nullable = false, length = Integer.MAX_VALUE)
    private String dscContent;

    @Column(name = "jsn_structuredcontent")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> jsnStructuredcontent;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dt_createdat", nullable = false)
    private OffsetDateTime dtCreatedat;

}