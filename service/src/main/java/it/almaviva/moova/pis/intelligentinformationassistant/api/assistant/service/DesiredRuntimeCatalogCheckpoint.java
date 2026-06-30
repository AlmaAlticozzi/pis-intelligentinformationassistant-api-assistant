package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.time.OffsetDateTime;

public record DesiredRuntimeCatalogCheckpoint(int version, long changeSequence, OffsetDateTime catalogAsOf) { }
