package com.roundfeather.persistence.datastore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field of a class annotated with {@link DatastoreEntity} with a name to use for the property.
 *
 * <p>
 *     The value of the annotation is the name of the property in Datastore
 * </p>
 *
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastorePropertyAs {
    /**
     * Sets the property name
     *
     * @return The name of the property
     *
     * @since 1.0
     */
    String value();
}
