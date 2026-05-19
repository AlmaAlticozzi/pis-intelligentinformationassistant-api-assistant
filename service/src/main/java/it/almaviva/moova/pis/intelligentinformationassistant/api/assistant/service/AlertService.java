package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSummaryListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class AlertService {

    @Inject
    AlertRepository alertRepository;

    public AlertSummaryListResponse searchAlerts(AlertSearchCriteria criteria) {
        System.out.println("AlertService.searchAlerts: criteria=" + criteria);
        return new AlertSummaryListResponse()
                .items(alertRepository.searchAlerts(criteria));
    }

    public Optional<AlertDetail> getAlert(String alertId) {
        System.out.println("AlertService.getAlert: alertId=" + alertId);
        return alertRepository.getAlert(alertId);
    }

    @Transactional
    public AlertDetail createDraftAlert(AlertCreateRequest request) {
        System.out.println("AlertService.createDraftAlert: request=" + request);
        return alertRepository.createDraftAlert(request);
    }

    public boolean existsActiveAlertWithNormalizedName(String name) {
        return alertRepository.existsActiveAlertWithNormalizedName(name);
    }
}
