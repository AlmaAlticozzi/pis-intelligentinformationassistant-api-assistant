package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogPage;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogRemovalItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogRemovalReason;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogUpsertItem;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.DesiredRuntimeCatalogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

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
        String mode = request.mode().name();
        System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][" + mode + "][REQUEST] mode=" + request.mode()
                + " cursorPresent=" + cursorPresent + " requestedLimit=" + request.limit()
                + " effectiveLimit=" + limit + " changedAfterPresent=" + (request.changedAfter() != null)
                + " checkpointPresent=" + (request.checkpoint() != null)
                + " targetAgentCount=" + (request.agentDefinitionIds() == null ? 0 : request.agentDefinitionIds().size()));
        try {
            if (request.mode() == DesiredRuntimeCatalogMode.TARGETED) {
                return getTargeted(request, limit);
            }
            DesiredRuntimeCatalogCursor cursor = cursorPresent ? cursorCodec.decode(request.cursor()) : null;
            long upperSequence = cursor == null ? repository.findCurrentUpperSequence() : cursor.catalogUpperSequence();
            OffsetDateTime catalogAsOf = cursor == null ? now() : cursor.catalogAsOf();
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL][SNAPSHOT_QUERY] operation=MAX_CHANGE_SEQUENCE"
                    + " status=COMPLETED catalogUpperSequence=" + upperSequence + " catalogAsOf=" + catalogAsOf);
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL][CURSOR] present=" + cursorPresent
                    + (cursor == null ? " catalogUpperSequence=" + upperSequence
                    : " version=" + cursor.version() + " mode=" + cursor.mode()
                    + " catalogUpperSequence=" + cursor.catalogUpperSequence()
                    + " lastSourceUpdatedAt=" + cursor.lastSourceUpdatedAt()
                    + " lastAgentDefinitionId=" + cursor.lastAgentDefinitionId() + " valid=true"));
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL][PAGE_QUERY] status=STARTED"
                    + " catalogUpperSequence=" + upperSequence + " cursorPresent=" + cursorPresent
                    + " fetchLimit=" + (limit + 1)
                    + " lastSourceUpdatedAtPresent=" + (cursor != null && cursor.lastSourceUpdatedAt() != null)
                    + " lastAgentDefinitionIdPresent=" + (cursor != null && cursor.lastAgentDefinitionId() != null));
            var rows = repository.findFullSnapshotPage(upperSequence,
                    cursor == null ? null : cursor.lastSourceUpdatedAt(),
                    cursor == null ? null : cursor.lastAgentDefinitionId(), limit + 1);
            boolean hasMore = rows.size() > limit;
            var returnedRows = rows.subList(0, Math.min(limit, rows.size()));
            List<DesiredRuntimeCatalogUpsertItem> upserts = returnedRows.stream().map(row -> {
                System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL][ITEM_MAPPING] agentDefinitionId="
                        + row.agentDefinitionId() + " catalogChangeSequence=" + row.catalogChangeSequence()
                        + " runtimePackageId=" + row.runtimePackageId() + " status=STARTED");
                DesiredRuntimeCatalogUpsertItem item = mapper.map(row);
                System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL][ITEM_MAPPING] agentDefinitionId="
                        + row.agentDefinitionId() + " catalogChangeSequence=" + row.catalogChangeSequence()
                        + " runtimePackageId=" + row.runtimePackageId() + " status=COMPLETED");
                return item;
            }).toList();
            String nextCursor = null;
            if (hasMore) {
                var last = returnedRows.getLast();
                nextCursor = cursorCodec.encode(new DesiredRuntimeCatalogCursor(1, "FULL", upperSequence,
                        catalogAsOf, last.sourceUpdatedAt(), last.agentDefinitionId(), null));
            }
            String checkpoint = hasMore ? null : checkpointCodec.encode(
                    new DesiredRuntimeCatalogCheckpoint(1, upperSequence, catalogAsOf));
            DesiredRuntimeCatalogResponse response = new DesiredRuntimeCatalogResponse()
                    .catalogVersion(CATALOG_VERSION).mode(DesiredRuntimeCatalogMode.FULL)
                    .generatedAt(now()).catalogAsOf(catalogAsOf).sourceCheckpoint(null)
                    .nextCheckpoint(checkpoint).items(asCatalogItems(upserts))
                    .page(new DesiredRuntimeCatalogPage().limit(limit).returned(upserts.size())
                            .hasMore(hasMore).nextCursor(nextCursor));
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL][RESPONSE] returned=" + upserts.size()
                    + " hasMore=" + hasMore + " nextCursorPresent=" + (nextCursor != null)
                    + " nextCheckpointPresent=" + (checkpoint != null)
                    + " catalogUpperSequence=" + upperSequence);
            return response;
        } catch (DesiredRuntimeCatalogInvalidRequestException | DesiredRuntimeCatalogConsistencyException ex) {
            throw ex;
        } catch (PersistenceException ex) {
            if (dependencyUnavailable(ex)) {
                throw new DesiredRuntimeCatalogUnavailableException(
                        "Desired Runtime Catalog persistence is unavailable.", ex);
            }
            throw ex;
        }
    }

    private DesiredRuntimeCatalogResponse getTargeted(DesiredRuntimeCatalogRequest request, int limit) {
        List<String> sortedIds = DesiredRuntimeCatalogTargetFilter.sorted(request.agentDefinitionIds());
        String fingerprint = DesiredRuntimeCatalogTargetFilter.fingerprint(sortedIds);
        boolean cursorPresent = request.cursor() != null;
        DesiredRuntimeCatalogCursor cursor = cursorPresent
                ? cursorCodec.decodeTargeted(request.cursor(), fingerprint) : null;
        long upperSequence = cursor == null ? repository.findCurrentUpperSequence() : cursor.catalogUpperSequence();
        OffsetDateTime catalogAsOf = cursor == null ? now() : cursor.catalogAsOf();
        OffsetDateTime evaluatedAt = now();
        int start = 0;
        if (cursor != null) {
            int position = java.util.Collections.binarySearch(sortedIds, cursor.lastAgentDefinitionId());
            if (position < 0) throw new DesiredRuntimeCatalogInvalidRequestException(
                    "cursor", "The TARGETED cursor position is invalid.");
            start = position + 1;
        }
        int end = Math.min(start + limit, sortedIds.size());
        List<String> pageIds = sortedIds.subList(start, end);
        boolean hasMore = end < sortedIds.size();
        Set<String> pageIdSet = new HashSet<>(pageIds);
        var changes = repository.findLatestCatalogChangesForAgentIds(pageIdSet, upperSequence);
        var definitions = repository.findAgentDefinitionsByIds(pageIdSet);
        List<Object> outcomes = new ArrayList<>();
        int upserts = 0, notActive = 0, notFound = 0;
        for (String id : pageIds) {
            var change = changes.get(id);
            if (change != null && "UPSERT".equals(change.action())) {
                outcomes.add(mapper.map(change));
                upserts++;
                System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][TARGETED][ITEM] agentDefinitionId=" + id
                        + " outcome=UPSERT removalReason=null");
                continue;
            }
            var definition = definitions.get(id);
            boolean found = change != null || definition != null;
            String status = change != null ? change.sourceAgentStatus()
                    : definition == null ? null : definition.status();
            OffsetDateTime sourceUpdatedAt = change != null ? change.sourceUpdatedAt()
                    : definition == null ? null : definition.updatedAt();
            DesiredRuntimeCatalogRemovalReason reason = found
                    ? DesiredRuntimeCatalogRemovalReason.NOT_ACTIVE : DesiredRuntimeCatalogRemovalReason.NOT_FOUND;
            DesiredRuntimeCatalogRemovalItem removal = new DesiredRuntimeCatalogRemovalItem()
                    .action(DesiredRuntimeCatalogRemovalItem.ActionEnum.REMOVE).agentDefinitionId(id)
                    .removalReason(reason).evaluatedAt(evaluatedAt).sourceUpdatedAt(sourceUpdatedAt);
            if (status != null) removal.sourceStatus(AgentDefinitionStatus.fromValue(status));
            outcomes.add(removal);
            if (found) notActive++; else notFound++;
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][TARGETED][ITEM] agentDefinitionId=" + id
                    + " outcome=REMOVE removalReason=" + reason);
        }
        String nextCursor = hasMore ? cursorCodec.encode(new DesiredRuntimeCatalogCursor(1, "TARGETED",
                upperSequence, catalogAsOf, null, pageIds.getLast(), fingerprint)) : null;
        System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][TARGETED][SNAPSHOT] catalogUpperSequence="
                + upperSequence + " catalogAsOf=" + catalogAsOf + " filterFingerprintPrefix="
                + fingerprint.substring(0, 12));
        System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][TARGETED][RESOLUTION] requested=" + pageIds.size()
                + " catalogChangesFound=" + changes.size() + " definitionsFound=" + definitions.size()
                + " runtimePackagesRequired=" + upserts);
        System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][TARGETED][RESULT] upsertCount=" + upserts
                + " notActiveCount=" + notActive + " notFoundCount=" + notFound + " returned=" + outcomes.size()
                + " hasMore=" + hasMore + " nextCursorPresent=" + (nextCursor != null));
        return new DesiredRuntimeCatalogResponse().catalogVersion(CATALOG_VERSION)
                .mode(DesiredRuntimeCatalogMode.TARGETED).generatedAt(now()).catalogAsOf(catalogAsOf)
                .sourceCheckpoint(null).nextCheckpoint(null).items(asCatalogItemsMixed(outcomes))
                .page(new DesiredRuntimeCatalogPage().limit(limit).returned(outcomes.size())
                        .hasMore(hasMore).nextCursor(nextCursor));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<DesiredRuntimeCatalogItem> asCatalogItems(List<DesiredRuntimeCatalogUpsertItem> upserts) {
        // Generated oneOf classes are sibling types; preserve the runtime UPSERT subtype for Jackson.
        return (List) new ArrayList<>(upserts);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<DesiredRuntimeCatalogItem> asCatalogItemsMixed(List<?> items) {
        return (List) new ArrayList<>(items);
    }

    private OffsetDateTime now() { return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }

    private boolean dependencyUnavailable(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof SQLTransientConnectionException) return true;
            if (current instanceof SQLException sql && sql.getSQLState() != null
                    && sql.getSQLState().startsWith("08")) return true;
            String type = current.getClass().getName();
            if (type.endsWith("JDBCConnectionException") || type.endsWith("ConnectionAcquisitionException")) {
                return true;
            }
        }
        return false;
    }
}
