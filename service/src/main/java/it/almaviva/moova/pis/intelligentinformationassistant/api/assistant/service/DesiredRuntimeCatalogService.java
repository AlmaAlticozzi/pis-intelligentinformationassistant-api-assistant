package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogPage;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogUpsertItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DesiredRuntimeCatalogService {
    private static final String CATALOG_VERSION = "iia.desired-runtime-catalog/v1";

    @Inject DesiredRuntimeCatalogRequestValidator requestValidator;
    @Inject DesiredRuntimeCatalogCursorCodec cursorCodec;
    @Inject DesiredRuntimeCatalogCheckpointCodec checkpointCodec;
    @Inject DesiredRuntimeCatalogRepository repository;
    @Inject DesiredRuntimeCatalogMapper mapper;
    Clock clock = Clock.systemUTC();

    public DesiredRuntimeCatalogService() { }

    DesiredRuntimeCatalogService(DesiredRuntimeCatalogRequestValidator requestValidator,
            DesiredRuntimeCatalogCursorCodec cursorCodec, DesiredRuntimeCatalogCheckpointCodec checkpointCodec,
            DesiredRuntimeCatalogRepository repository, DesiredRuntimeCatalogMapper mapper, Clock clock) {
        this.requestValidator = requestValidator;
        this.cursorCodec = cursorCodec;
        this.checkpointCodec = checkpointCodec;
        this.repository = repository;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public DesiredRuntimeCatalogResponse get(DesiredRuntimeCatalogRequest request) {
        int limit = requestValidator.validate(request);
        boolean cursorPresent = request.cursor() != null;
        System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL] request cursorPresent=" + cursorPresent
                + " requestedLimit=" + request.limit() + " effectiveLimit=" + limit);
        try {
            DesiredRuntimeCatalogCursor cursor = cursorPresent ? cursorCodec.decode(request.cursor()) : null;
            long upperSequence = cursor == null ? repository.findCurrentUpperSequence() : cursor.catalogUpperSequence();
            OffsetDateTime catalogAsOf = cursor == null ? now() : cursor.catalogAsOf();
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL] snapshot catalogUpperSequence="
                    + upperSequence + " catalogAsOf=" + catalogAsOf);
            var rows = repository.findFullSnapshotPage(upperSequence,
                    cursor == null ? null : cursor.lastSourceUpdatedAt(),
                    cursor == null ? null : cursor.lastAgentDefinitionId(), limit + 1);
            boolean hasMore = rows.size() > limit;
            var returnedRows = rows.subList(0, Math.min(limit, rows.size()));
            List<DesiredRuntimeCatalogUpsertItem> upserts = returnedRows.stream().map(mapper::map).toList();
            String nextCursor = null;
            if (hasMore) {
                var last = returnedRows.getLast();
                nextCursor = cursorCodec.encode(new DesiredRuntimeCatalogCursor(1, "FULL", upperSequence,
                        catalogAsOf, last.sourceUpdatedAt(), last.agentDefinitionId()));
            }
            String checkpoint = hasMore ? null : checkpointCodec.encode(
                    new DesiredRuntimeCatalogCheckpoint(1, upperSequence, catalogAsOf));
            DesiredRuntimeCatalogResponse response = new DesiredRuntimeCatalogResponse()
                    .catalogVersion(CATALOG_VERSION).mode(DesiredRuntimeCatalogMode.FULL)
                    .generatedAt(now()).catalogAsOf(catalogAsOf).sourceCheckpoint(null)
                    .nextCheckpoint(checkpoint).items(asCatalogItems(upserts))
                    .page(new DesiredRuntimeCatalogPage().limit(limit).returned(upserts.size())
                            .hasMore(hasMore).nextCursor(nextCursor));
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL] result returned=" + upserts.size()
                    + " hasMore=" + hasMore + " nextCursorPresent=" + (nextCursor != null)
                    + " nextCheckpointPresent=" + (checkpoint != null));
            return response;
        } catch (DesiredRuntimeCatalogInvalidRequestException | DesiredRuntimeCatalogConsistencyException ex) {
            throw ex;
        } catch (PersistenceException ex) {
            throw new DesiredRuntimeCatalogUnavailableException("Desired Runtime Catalog persistence is unavailable.", ex);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<DesiredRuntimeCatalogItem> asCatalogItems(List<DesiredRuntimeCatalogUpsertItem> upserts) {
        // Generated oneOf classes are sibling types; preserve the runtime UPSERT subtype for Jackson.
        return (List) new ArrayList<>(upserts);
    }

    private OffsetDateTime now() { return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }
}
