package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;

public class AlertSearchCriteria {

    private final AlertStatus status;
    private final Boolean enabled;
    private final AlertInterpreterType interpreterType;
    private final String text;

    public AlertSearchCriteria(AlertStatus status, Boolean enabled, AlertInterpreterType interpreterType, String text) {
        this.status = status;
        this.enabled = enabled;
        this.interpreterType = interpreterType;
        this.text = text;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public AlertInterpreterType getInterpreterType() {
        return interpreterType;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "AlertSearchCriteria{" +
                "status=" + status +
                ", enabled=" + enabled +
                ", interpreterType=" + interpreterType +
                ", text='" + text + '\'' +
                '}';
    }
}
