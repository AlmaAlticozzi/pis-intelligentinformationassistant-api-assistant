package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api.controller;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSummaryListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AlertService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertSearchApiTest {

    @Test
    void searchAlertsReturnsPromptInSummaryItems() {
        AlertService service = mock(AlertService.class);
        String prompt = "Avvertimi quando una corsa arriva a Garibaldi";
        AlertSummaryListResponse response = new AlertSummaryListResponse()
                .items(List.of(new AlertSummary()
                        .id("ALRT1")
                        .name("Arrivo Garibaldi")
                        .prompt(prompt)
                        .status(AlertStatus.DRAFT)
                        .enabled(false)
                        .createdAt(OffsetDateTime.parse("2026-06-18T10:15:30Z"))));
        when(service.searchAlerts(argThat(criteria -> criteria != null
                && criteria.getStatus() == null
                && criteria.getEnabled() == null
                && criteria.getInterpreterType() == null
                && "Garibaldi".equals(criteria.getText()))))
                .thenReturn(response);
        AssistantV1Api api = api(service);

        AlertSummaryListResponse result = api.searchAlerts(null, null, null, "Garibaldi");

        assertThat(result.getItems()).hasSize(1);
        AlertSummary summary = result.getItems().getFirst();
        assertThat(summary.getPrompt()).isEqualTo(prompt);
        assertThat(summary.getId()).isEqualTo("ALRT1");
        assertThat(summary.getName()).isEqualTo("Arrivo Garibaldi");
        assertThat(summary.getStatus()).isEqualTo(AlertStatus.DRAFT);
        assertThat(summary.getEnabled()).isFalse();
    }

    private AssistantV1Api api(AlertService service) {
        AssistantV1Api api = new AssistantV1Api();
        api.alertService = service;
        return api;
    }
}
