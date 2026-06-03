package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import it.almaviva.fnd.core.lib.quarkuscommon.config.FNDCommonProperties;
import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import jakarta.enterprise.context.RequestScoped;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FndHibernateTenantResolverTest {

    @Test
    void fndHibernateTenantResolverIsRequestScopedPersistenceUnitExtension() throws Exception {
        Class<?> resolverClass = Class.forName(
                "it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.HibernateTenantResolver");

        assertThat(resolverClass.getAnnotation(RequestScoped.class)).isNotNull();
        assertThat(resolverClass.getAnnotation(PersistenceUnitExtension.class)).isNotNull();
        assertThat(TenantResolver.class.isAssignableFrom(resolverClass)).isTrue();
    }

    @Test
    void fndHibernateTenantResolverReadsTenantContextThenRoutingContextThenDefaultSchema() throws Exception {
        TenantContext tenantContext = mock(TenantContext.class);
        RoutingContext routingContext = mock(RoutingContext.class);
        FNDCommonProperties properties = mock(FNDCommonProperties.class);
        when(properties.multiTenant()).thenReturn(true);
        when(properties.getDefaultSchema()).thenReturn("pis_intelligentinformationassistant");
        TenantResolver resolver = newResolver(tenantContext, routingContext, properties);

        when(tenantContext.getTenantId()).thenReturn("tenant-from-context");
        assertThat(resolver.resolveTenantId()).isEqualTo("tenant-from-context");

        when(tenantContext.getTenantId()).thenReturn(null);
        when(routingContext.get("tenant-id")).thenReturn("tenant-from-routing-context");
        assertThat(resolver.resolveTenantId()).isEqualTo("tenant-from-routing-context");

        when(routingContext.get("tenant-id")).thenReturn(null);
        assertThat(resolver.resolveTenantId()).isEqualTo("pis_intelligentinformationassistant");
    }

    private TenantResolver newResolver(
            TenantContext tenantContext,
            RoutingContext routingContext,
            FNDCommonProperties properties) throws Exception {
        Class<?> resolverClass = Class.forName(
                "it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.HibernateTenantResolver");
        Constructor<?> constructor = resolverClass.getDeclaredConstructor(
                TenantContext.class,
                RoutingContext.class,
                FNDCommonProperties.class);
        constructor.setAccessible(true);
        return (TenantResolver) constructor.newInstance(tenantContext, routingContext, properties);
    }
}
