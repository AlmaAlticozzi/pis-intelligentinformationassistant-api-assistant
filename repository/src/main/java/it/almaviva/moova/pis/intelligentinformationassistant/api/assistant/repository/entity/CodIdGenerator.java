package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.AnnotationBasedGenerator;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.IdentifierGenerator;

import java.lang.reflect.Member;
import java.util.Properties;

public class CodIdGenerator implements IdentifierGenerator, AnnotationBasedGenerator<CodGeneratedId> {
    public static final String PREFIX_PARAM = "prefix";

    private String prefix;

    @Override
    public void initialize(CodGeneratedId annotation, Member member, GeneratorCreationContext context) {
        prefix = annotation.value();
    }

    @Override
    public void configure(GeneratorCreationContext creationContext, Properties parameters) throws MappingException {
        String configuredPrefix = parameters.getProperty(PREFIX_PARAM);
        if (configuredPrefix != null) {
            prefix = configuredPrefix;
        }
        if (prefix == null || prefix.isBlank()) {
            throw new MappingException("Missing cod id prefix");
        }
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return session
                .createNativeQuery(
                        "select pis_intelligentinformationassistant.generate_cod_id(:prefix)",
                        String.class
                )
                .setParameter(PREFIX_PARAM, prefix)
                .getSingleResult();
    }
}
