package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(CodIdGenerator.class)
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface CodGeneratedId {
    String value();
}
