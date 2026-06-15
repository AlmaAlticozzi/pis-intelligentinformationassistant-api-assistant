package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentActivationType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactSignatureStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionAllowedTool;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionAllowedToolId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionDayOfWeek;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionDayOfWeekId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionRequiredSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionRequiredSourceId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.DataSourceCategory;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.DayOfWeek;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AgentDefinitionRepository implements PanacheRepositoryBase<AgentDefinition, String> {

    @Inject
    EntityManager entityManager;

    public Optional<Alert> findAlert(String alertId) {
        return entityManager.createQuery("from Alert where codAlert = :alertId", Alert.class)
                .setParameter("alertId", alertId)
                .getResultStream()
                .findFirst();
    }

    public Optional<AgentDefinition> findByDefinitionId(String agentDefinitionId) {
        return entityManager.createQuery("""
                        select d
                        from AgentDefinition d
                        join fetch d.codAlert
                        join fetch d.codAgentprofile
                        join fetch d.sglStatus
                        join fetch d.sglGenerationmode
                        join fetch d.sglActivationtype
                        join fetch d.sglArtifacttype
                        left join fetch d.sglSignaturestatus
                        left join fetch d.codLatestcompilation lc
                        left join fetch lc.sglStatus
                        left join fetch d.sglLatestcompilationstatus
                        left join fetch d.codLatestrun lr
                        left join fetch lr.sglStatus
                        left join fetch lr.sglHealthstatus
                        left join fetch lr.sglQualitystatus
                        where d.codAgentdefinition = :agentDefinitionId
                        """, AgentDefinition.class)
                .setParameter("agentDefinitionId", agentDefinitionId)
                .getResultStream()
                .findFirst();
    }

    public List<AgentDefinition> search(
            String status,
            String alertId,
            String generationMode,
            String profileId,
            String text) {
        StringBuilder jpql = new StringBuilder("""
                select distinct d
                from AgentDefinition d
                join fetch d.codAlert
                join fetch d.codAgentprofile
                join fetch d.sglStatus
                join fetch d.sglGenerationmode
                join fetch d.sglActivationtype
                join fetch d.sglArtifacttype
                left join fetch d.sglSignaturestatus
                left join fetch d.codLatestcompilation lc
                left join fetch lc.sglStatus
                left join fetch d.sglLatestcompilationstatus
                left join fetch d.codLatestrun lr
                left join fetch lr.sglStatus
                left join fetch lr.sglHealthstatus
                left join fetch lr.sglQualitystatus
                where 1 = 1
                """);
        if (status != null) {
            jpql.append(" and d.sglStatus.sglStatus = :status");
        }
        if (alertId != null) {
            jpql.append(" and d.codAlert.codAlert = :alertId");
        }
        if (generationMode != null) {
            jpql.append(" and d.sglGenerationmode.sglGenerationmode = :generationMode");
        }
        if (profileId != null) {
            jpql.append(" and d.codAgentprofile.codAgentprofile = :profileId");
        }
        if (text != null) {
            jpql.append("""
                     and (
                        lower(d.dscName) like :textPattern
                        or lower(coalesce(d.dscDescription, '')) like :textPattern
                        or lower(d.codAlert.dscName) like :textPattern
                     )
                    """);
        }
        jpql.append(" order by d.dtCreatedat desc, d.codAgentdefinition desc");

        var query = entityManager.createQuery(jpql.toString(), AgentDefinition.class);
        if (status != null) {
            query.setParameter("status", status);
        }
        if (alertId != null) {
            query.setParameter("alertId", alertId);
        }
        if (generationMode != null) {
            query.setParameter("generationMode", generationMode);
        }
        if (profileId != null) {
            query.setParameter("profileId", profileId);
        }
        if (text != null) {
            query.setParameter("textPattern", "%" + text.toLowerCase() + "%");
        }
        return query.getResultList();
    }

    @Transactional
    public AgentDefinition create(
            AgentDefinition definition,
            List<String> requiredSources,
            List<String> dayOfWeekValues,
            List<String> allowedToolNames) {
        persist(definition);
        flush();

        for (String requiredSource : requiredSources) {
            AgentDefinitionRequiredSourceId id = new AgentDefinitionRequiredSourceId();
            id.setCodAgentdefinition(definition.getCodAgentdefinition());
            id.setSglCategory(requiredSource);

            AgentDefinitionRequiredSource source = new AgentDefinitionRequiredSource();
            source.setId(id);
            source.setCodAgentdefinition(definition);
            source.setSglCategory(entityManager.getReference(DataSourceCategory.class, requiredSource));
            source.setFlgRequired(true);
            source.setDscDescription("Required by Agent Definition technical specification.");
            entityManager.persist(source);
        }

        for (String day : dayOfWeekValues) {
            AgentDefinitionDayOfWeekId id = new AgentDefinitionDayOfWeekId();
            id.setCodAgentdefinition(definition.getCodAgentdefinition());
            id.setSglDayofweek(day);

            AgentDefinitionDayOfWeek rel = new AgentDefinitionDayOfWeek();
            rel.setId(id);
            rel.setCodAgentdefinition(definition);
            rel.setSglDayofweek(entityManager.getReference(DayOfWeek.class, day));
            entityManager.persist(rel);
        }

        for (String toolName : allowedToolNames) {
            AgentDefinitionAllowedToolId id = new AgentDefinitionAllowedToolId();
            id.setCodAgentdefinition(definition.getCodAgentdefinition());
            id.setDscToolname(toolName);

            AgentDefinitionAllowedTool tool = new AgentDefinitionAllowedTool();
            tool.setId(id);
            tool.setCodAgentdefinition(definition);
            tool.setJsnOperations(Map.of("operations", List.of(toolName)));
            entityManager.persist(tool);
        }

        flush();
        return definition;
    }

    public AgentDefinitionStatus statusReference(String status) {
        return entityManager.getReference(AgentDefinitionStatus.class, status);
    }

    public AgentGenerationMode generationModeReference(String generationMode) {
        return entityManager.getReference(AgentGenerationMode.class, generationMode);
    }

    public AgentActivationType activationTypeReference(String activationType) {
        return entityManager.getReference(AgentActivationType.class, activationType);
    }

    public AgentArtifactType artifactTypeReference(String artifactType) {
        return entityManager.getReference(AgentArtifactType.class, artifactType);
    }

    public AgentArtifactSignatureStatus signatureStatusReference(String signatureStatus) {
        return entityManager.getReference(AgentArtifactSignatureStatus.class, signatureStatus);
    }

    public AgentComplexity complexityReference(String complexity) {
        return entityManager.getReference(AgentComplexity.class, complexity);
    }

    @Transactional
    public void markCompilationReady(
            String agentDefinitionId,
            String compilationId,
            String artifactUri,
            String artifactHash,
            String runtimeImage,
            String sdkVersion,
            String implementationSummary,
            Map<String, Object> dslArtifact,
            OffsetDateTime completedAt) {
        System.out.println("[IIA][AGENT_DEFINITION][REPOSITORY] mark READY agentDefinitionId="
                + agentDefinitionId + " compilationId=" + compilationId);
        AgentDefinition definition = entityManager.find(AgentDefinition.class, agentDefinitionId);
        if (definition == null) {
            return;
        }
        definition.setSglStatus(statusReference("READY"));
        definition.setSglArtifacttype(artifactTypeReference("DSL"));
        definition.setDscArtifacturi(artifactUri);
        definition.setDscArtifacthash(artifactHash);
        definition.setSglSignaturestatus(signatureStatusReference("SIGNED"));
        definition.setDscRuntimeimage(runtimeImage);
        definition.setDscSdkversion(sdkVersion);
        definition.setDscImplementationsummary(implementationSummary);
        definition.setCodLatestcompilation(entityManager.getReference(AgentCompilation.class, compilationId));
        definition.setSglLatestcompilationstatus(entityManager.getReference(AgentCompilationStatus.class, "READY"));
        definition.setDscLatestcompilationstep("READY");
        definition.setDtLatestcompilationcompletedat(completedAt);
        definition.setJsnDslpreview(dslArtifact);
        definition.setDtUpdatedat(OffsetDateTime.now());
        entityManager.flush();
    }

    @Transactional
    public void markCompilationRejected(
            String agentDefinitionId,
            String compilationId,
            String latestCompilationStep,
            String implementationSummary,
            OffsetDateTime completedAt) {
        System.out.println("[IIA][AGENT_DEFINITION][REPOSITORY] mark REJECTED agentDefinitionId="
                + agentDefinitionId + " compilationId=" + compilationId + " step=" + latestCompilationStep);
        AgentDefinition definition = entityManager.find(AgentDefinition.class, agentDefinitionId);
        if (definition == null) {
            return;
        }
        definition.setSglStatus(statusReference("REJECTED"));
        definition.setSglArtifacttype(artifactTypeReference("NONE"));
        definition.setDscArtifacturi(null);
        definition.setDscArtifacthash(null);
        definition.setSglSignaturestatus(signatureStatusReference("NOT_SIGNED"));
        definition.setDscRuntimeimage(null);
        definition.setDscSdkversion(null);
        definition.setDscImplementationsummary(implementationSummary);
        definition.setCodLatestcompilation(entityManager.getReference(AgentCompilation.class, compilationId));
        definition.setSglLatestcompilationstatus(entityManager.getReference(AgentCompilationStatus.class, "REJECTED"));
        definition.setDscLatestcompilationstep(latestCompilationStep);
        definition.setDtLatestcompilationcompletedat(completedAt);
        definition.setDtUpdatedat(OffsetDateTime.now());
        entityManager.flush();
    }
}
