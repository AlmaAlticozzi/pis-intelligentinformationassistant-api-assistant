package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.view;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@EntityView(Alert.class)
public interface AlertSummaryView {

    @IdMapping("codAlert")
    String getId();

    @Mapping("dscName")
    String getName();

    @Mapping("sglStatus.sglStatus")
    String getStatus();

    @Mapping("flgEnabled")
    Boolean getEnabled();

    @Mapping("sglInterpretertype.sglInterpretertype")
    String getInterpreterType();

    @Mapping("numVerificationconfidence")
    BigDecimal getConfidence();

    @Mapping("dtCreatedat")
    OffsetDateTime getCreatedAt();

    @Mapping("dtUpdatedat")
    OffsetDateTime getUpdatedAt();

    @Mapping("numVersion")
    Integer getVersion();
}
