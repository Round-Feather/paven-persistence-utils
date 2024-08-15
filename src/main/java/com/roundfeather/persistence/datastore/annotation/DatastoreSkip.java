package com.roundfeather.persistence.datastore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field of a class annotated with {@link DatastoreEntity} to be skipped when serializing/deserializing
 *
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastoreSkip {
}
